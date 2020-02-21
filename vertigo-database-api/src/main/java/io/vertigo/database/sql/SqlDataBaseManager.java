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
package io.vertigo.database.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

import io.vertigo.core.component.Manager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.lang.Tuple;

/**
 * Manages connections to database.
 * @author pchretien
 */
public interface SqlDataBaseManager extends Manager {
	/** The name of the main connectionProvider. */
	String MAIN_CONNECTION_PROVIDER_NAME = "main";

	/**
	 * @param name ConnectionProvider name
	 * @return SecondaryConnectionProvider
	 */
	SqlConnectionProvider getConnectionProvider(String name);

	/**
	 * Executes a sql query returning a list
	 * @param sqlStatement sqlStatement
	 * @param dataType the return dataType of the list
	 * @param limit the return limit (null if no limit)
	 * @param connection the sqlConnection
	 * @return the list
	 *
	 * @throws SQLException
	 */
	<O> List<O> executeQuery(
			SqlStatement sqlStatement,
			final Class<O> dataType,
			final Integer limit,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes a sql query returning the number of modified rows.
	 * @param sqlStatement sqlStatement
	 * @param connection sqlConnection
	 * @return either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * @throws SQLException
	 */
	int executeUpdate(
			SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes a sql query returning the number of modified rows.
	 * @param sqlStatement sqlStatement
	 * @param generationMode the generation methode
	 * @param columnName the column name (of the generated key)
	 * @param dataType the dataType of the generated key
	 * @param connection sqlConnection
	 * @return a tuple with the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing and the generated key
	 * @throws SQLException
	 */
	<O> Tuple<Integer, O> executeUpdateWithGeneratedKey(
			SqlStatement sqlStatement,
			final GenerationMode generationMode,
			final String columnName,
			final Class<O> dataType,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes the batch .
	 * @param sqlStatement sqlStatement
	 * @param connection sqlConnection
	 * @return the SUM of  row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * if no info available an empty Optional is returned
	 * @throws SQLException Si erreur
	 */
	OptionalInt executeBatch(
			SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException;
}
