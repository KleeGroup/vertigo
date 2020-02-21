/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.database.impl.sql;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.vertigo.database.impl.sql.mapper.SqlMapper;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.database.sql.vendor.SqlMapping;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.BeanUtil;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * This class handles the real communication with the database, it is used to
 * 		1) create the sql statements
 * 		2) assign values to statements
 * 		3) retrieve typed values from resultsets
 *
 * Supported datatypes (class) of parameters and result are of two types :
 * 		- primitive ones through a SqlMapping (Vendor specific, retrieved from connection)
 * 		- complex ones through custom adapters that converts an unhandled datatype (ex: Mail) to primitives
 *
 * @author  pchretien
 */
final class SqlStatementDriver {

	private static final int NO_GENERATED_KEY_ERROR_VENDOR_CODE = 100;

	private static final int TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE = 464;

	private static final int NULL_GENERATED_KEY_ERROR_VENDOR_CODE = -407;

	private static final int FETCH_SIZE = 150;

	private static final int GENERATED_KEYS_INDEX = 1;

	private final SqlMapper sqlMapper;

	SqlStatementDriver(final SqlMapper sqlMapper) {
		this.sqlMapper = sqlMapper;
	}

	//------------------ Statement creations ----------------------------------//

	PreparedStatement createStatement(final String sql, final SqlConnection connection) throws SQLException {
		//created PrepareStatement must be use into a try-with-resource in caller
		final PreparedStatement preparedStatement = connection.getJdbcConnection()
				.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
		//by experience 150 is a right value (Oracle is set by default at 10 : that's not sufficient)
		preparedStatement.setFetchSize(FETCH_SIZE);
		return preparedStatement;
	}

	PreparedStatement createStatement(
			final String sql,
			final GenerationMode generationMode,
			final String[] generatedColumns,
			final SqlConnection connection) throws SQLException {
		//created PrepareStatement must be use into a try-with-resource in caller
		final PreparedStatement preparedStatement;
		switch (generationMode) {
			case GENERATED_KEYS:
				preparedStatement = connection.getJdbcConnection()
						.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				break;
			case GENERATED_COLUMNS:
				preparedStatement = connection.getJdbcConnection()
						.prepareStatement(sql, generatedColumns);
				break;
			default:
				throw new IllegalStateException();
		}
		//by experience 150 is a right value (Oracle is set by default at 10 : that's not sufficient)
		preparedStatement.setFetchSize(FETCH_SIZE);
		return preparedStatement;
	}

	//------------------ Set values on statement ------------------------------//

	void setParameters(
			final PreparedStatement statement,
			final List<SqlParameter> parameters,
			final SqlConnection connection) throws SQLException {
		//-----
		for (int index = 0; index < parameters.size(); index++) {
			final SqlParameter parameter = parameters.get(index);
			final Class javaDataType = parameter.getDataType();
			final Class sqlDataType = sqlMapper.getSqlType(javaDataType);
			connection.getDataBase().getSqlMapping().setValueOnStatement(
					statement, index + 1, sqlDataType, sqlMapper.toSql(javaDataType, parameter.getValue()));
		}
	}

	//------------------ Retrieve values on resultSet -------------------------//

	/**
	 * Création du résultat issu d'un resultSet.
	 * @param domain Domain résultat
	 * @param mapping Mapping SQL
	 * @param resultSet ResultSet comprenant résultat et Metadonnées permettant le cas échéant de créer dynamiquement un type dynamiquement.
	 * @return Résultat de la requête.
	 * @throws SQLException Exception SQL
	 */
	<O> List<O> buildResult(
			final Class<O> dataType,
			final SqlMapping sqlMapping,
			final ResultSet resultSet,
			final Integer limit) throws SQLException {
		Assertion.checkNotNull(dataType);
		Assertion.checkNotNull(sqlMapping);
		Assertion.checkNotNull(resultSet);
		//-----
		return retrieveData(dataType, sqlMapping, resultSet, limit);
	}

	private <O> List<O> retrieveData(
			final Class<O> dataType,
			final SqlMapping sqlMapping,
			final ResultSet resultSet,
			final Integer limit) throws SQLException {
		final boolean isPrimitive = isPrimitive(dataType);

		final MyField[] fields = isPrimitive ? null : findFields(dataType, resultSet.getMetaData());
		//Dans le cas d'une collection on retourne toujours qqChose
		//Si la requête ne retourne aucune ligne, on retourne une collection vide.
		final List<O> list = new ArrayList<>();
		while (resultSet.next()) {
			if (limit != null && list.size() > limit) {
				throw createTooManyRowsException();
			}
			if (isPrimitive) {
				list.add(readPrimitive(sqlMapping, resultSet, dataType));
			} else {
				list.add(readRow(sqlMapping, resultSet, dataType, fields));
			}
		}
		return list;
	}

	private <O> O readPrimitive(
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Class<O> dataType) throws SQLException {
		final Class<?> sqlDataType = sqlMapper.getSqlType(dataType);
		return sqlMapper.toJava(dataType, mapping.getValueForResultSet(resultSet, 1, sqlDataType));
	}

	private <O> O readRow(
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Class<O> dataType,
			final MyField[] fields) throws SQLException {
		final O bean = ClassUtil.newInstance(dataType);
		Object value;
		for (int i = 0; i < fields.length; i++) {
			final Class<?> javaFieldDataType = fields[i].type;
			final Class<?> sqlFieldDataType = sqlMapper.getSqlType(javaFieldDataType);
			value = sqlMapper.toJava(javaFieldDataType, mapping.getValueForResultSet(resultSet, i + 1, sqlFieldDataType));
			fields[i].setValue(bean, value);
		}
		return bean;
	}

	/**
	 * Détermine les champs ramenés par un select.
	 * @param resultSetMetaData Metadonnées obtenues après exécution de la requête SQL.
	 * @return Tableau de codes de champ.
	 */
	private static MyField[] findFields(
			final Class dataType,
			final ResultSetMetaData resultSetMetaData) throws SQLException {
		final MyField[] fields = new MyField[resultSetMetaData.getColumnCount()];
		String columnLabel;
		for (int i = 0; i < fields.length; i++) {
			//getColumnLabel permet de récupérer le nom adapté lors du select (avec un select truc as machin from xxx)
			columnLabel = resultSetMetaData.getColumnLabel(i + 1);
			// toUpperCase nécessaire pour postgreSQL et SQLServer
			final String expectedFieldName = StringUtil.constToLowerCamelCase(columnLabel.toUpperCase(Locale.ENGLISH));
			try {
				final Method getter = dataType.getDeclaredMethod("get" + StringUtil.first2UpperCase(expectedFieldName));
				fields[i] = new MyField(expectedFieldName, getter.getReturnType());
			} catch (final NoSuchMethodException e) {
				throw WrappedException.wrap(e);
			}
		}
		return fields;
	}

	private static class MyField {
		protected final String name;
		protected final Class<?> type;

		MyField(final String name, final Class<?> type) {
			this.name = name;
			this.type = type;
		}

		void setValue(final Object bean, final Object value) {
			BeanUtil.setValue(bean, name, value);
		}

	}

	private static RuntimeException createTooManyRowsException() {
		return new IllegalStateException("load TooManyRows");
	}

	private static boolean isPrimitive(final Class dataType) {
		return Stream.of(
				Integer.class,
				Double.class,
				Boolean.class,
				String.class,
				Date.class,
				Instant.class,
				LocalDate.class,
				BigDecimal.class,
				Long.class)
				.anyMatch(primitiveClazz -> primitiveClazz.isAssignableFrom(dataType));
	}

	<O> O getGeneratedKey(
			final PreparedStatement statement,
			final String columnName,
			final Class<O> dataType,
			final SqlConnection connection) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(dataType);
		//-----
		// L'utilisation des generatedKeys permet d'avoir un seul appel réseau entre le
		// serveur d'application et la base de données pour un insert et la récupération de la
		// valeur de la clé primaire en respectant les standards jdbc et sql ansi.
		final SqlMapping sqlMapping = connection.getDataBase().getSqlMapping();
		try (final ResultSet rs = statement.getGeneratedKeys()) {
			final boolean next = rs.next();
			if (!next) {
				throw new SQLException("GeneratedKeys empty", "02000", NO_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			//ResultSet haven't correctly named columns so we fall back to get the first column, instead of looking for column index by name.
			final int pkRsCol = GENERATED_KEYS_INDEX;//attention le pkRsCol correspond au n° de column dans le RETURNING
			final Class<?> sqlDataType = sqlMapper.getSqlType(dataType);
			final O id = sqlMapper.toJava(dataType, sqlMapping.getValueForResultSet(
					rs, pkRsCol, sqlDataType));
			if (rs.wasNull()) {
				throw new SQLException("GeneratedKeys wasNull", "23502", NULL_GENERATED_KEY_ERROR_VENDOR_CODE);
			}

			if (rs.next()) {
				throw new SQLException("GeneratedKeys.size >1 ", "0100E", TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			return id;
		}
	}

}
