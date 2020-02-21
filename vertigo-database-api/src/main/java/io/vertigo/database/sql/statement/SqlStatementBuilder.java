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
package io.vertigo.database.sql.statement;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Tuple;
import io.vertigo.util.BeanUtil;

/**
 * SqlStatementBuilder.
 * Builder for SqlStatement
 *
 * @author mlaroche
 */
public final class SqlStatementBuilder implements Builder<SqlStatement> {
	private static final char SEPARATOR = '#';

	private final String rawSqlQuery;
	private final List<SqlNamedParam> sqlNamedParameters;
	private final List<Map<String, Tuple<Class, Object>>> sqlNamedParametersValues = new ArrayList<>();

	private int parameterLineIndex = 0;

	/**
	 * Constructor.
	 * @param sqlQuery a sql query
	 * @param connection Connexion
	 */
	SqlStatementBuilder(
			final String sqlQuery) {
		Assertion.checkArgNotEmpty(sqlQuery);
		//-----
		final Tuple<String, List<SqlNamedParam>> parsedQuery = parseQuery(sqlQuery);
		rawSqlQuery = parsedQuery.getVal1();
		sqlNamedParameters = parsedQuery.getVal2();
	}

	/**
	 * Add a value for a named parameter
	 * @param name the parameter name
	 * @param dataType the type of the parameter
	 * @param value the value of the parameter
	 * @return this builder
	 */
	public SqlStatementBuilder bind(final String name, final Class dataType, final Object value) {
		//---
		if (sqlNamedParametersValues.size() < parameterLineIndex + 1) {
			sqlNamedParametersValues.add(new HashMap<>());
		}
		final Map<String, Tuple<Class, Object>> lineNamedParameterValues = sqlNamedParametersValues.get(parameterLineIndex);
		lineNamedParameterValues.put(name, Tuple.of(dataType, value));
		return this;
	}

	/**
	 * Goes to next line of parameters (only useful for batch statements)
	 * @return this builder
	 */
	public SqlStatementBuilder nextLine() {
		parameterLineIndex++;
		return this;
	}

	private SqlParameter buildSqlParameter(final SqlNamedParam namedParam, final Map<String, Tuple<Class, Object>> params) {
		final Tuple<Class, Object> tuple = params.get(namedParam.getAttributeName());
		Assertion.checkNotNull(tuple, "no data found for param {0} in sql {1}", namedParam, rawSqlQuery);
		final Object rootHolder = tuple.getVal2();
		final Class rootType = tuple.getVal1();
		//---
		if (rootHolder != null) {
			if (namedParam.isObject() || namedParam.isList()) {
				final Object valueHolder;
				if (namedParam.isList()) {
					valueHolder = ((List) rootHolder).get(namedParam.getRowNumber());
				} else {
					// we are an object
					valueHolder = rootHolder;
				}
				if (namedParam.isPrimitive()) {
					return SqlParameter.of((Class) valueHolder.getClass(), valueHolder);
				}
				final String fieldName = namedParam.getFieldName();
				final Class valueHolderClass = valueHolder.getClass();
				final PropertyDescriptor propertyDescriptor = BeanUtil.getPropertyDescriptor(fieldName, valueHolderClass);
				return SqlParameter.of((Class) propertyDescriptor.getPropertyType(), BeanUtil.getValue(valueHolder, fieldName));
			}
		}

		// else primitive (or null same behaviour)
		return SqlParameter.of(rootType, rootHolder);
	}

	@Override
	public SqlStatement build() {
		final List<List<SqlParameter>> sqlParameters = sqlNamedParametersValues
				.stream()
				.map(namedParameterValues -> sqlNamedParameters.stream()
						.map(namedParameter -> buildSqlParameter(namedParameter, namedParameterValues))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());
		return new SqlStatement(rawSqlQuery, sqlParameters);
	}

	private static Tuple<String, List<SqlNamedParam>> parseQuery(final String query) {
		Assertion.checkArgNotEmpty(query);
		//-----
		//we add a space before and after to avoid side effects
		final String[] tokens = (" " + query + " ").split(String.valueOf(SEPARATOR));
		//...#p1#..... => 3 tokens
		//...#p1#.....#p2#... => 5 tokens
		Assertion.checkState(tokens.length % 2 == 1, "a tag is missing on query {0}", query);

		final List<SqlNamedParam> sqlNamedParams = new ArrayList<>();
		final StringBuilder sql = new StringBuilder();
		boolean param = false;
		//request = "select....#param1#... #param2#"
		for (final String token : tokens) {
			if (param) {
				if (token.isEmpty()) {
					//the separator character has been escaped and must be replaced by a single Separator
					sql.append(SEPARATOR);
				} else {
					sqlNamedParams.add(SqlNamedParam.of(token));
					Assertion.checkArgument(sql.charAt(sql.length() - 1) != '\'', "Param {0} is quoted, it will be ignored by jdbc driver. Query:{1}", token, query);
					sql.append('?');
				}
			} else {
				sql.append(token);
			}
			param = !param;
		}
		//we delete the added spaces...
		sql.delete(0, 1);
		sql.delete(sql.length() - 1, sql.length());

		return Tuple.of(sql.toString(), sqlNamedParams);
	}
}
