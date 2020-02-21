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

import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * SqlStatement.
 *
 * Holds all the information for executing a sql query :
 *  - the raw sql query
 *  - the bound parameters
 *
 * @author mlaroche,pchretien
 */
public final class SqlStatement {

	private final String sqlQuery;
	private final List<List<SqlParameter>> sqlParameters;

	public static SqlStatementBuilder builder(final String sqlQuery) {
		return new SqlStatementBuilder(sqlQuery);
	}

	public SqlStatement(
			final String sqlQuery,
			final List<List<SqlParameter>> sqlParameters) {
		Assertion.checkArgNotEmpty(sqlQuery);
		Assertion.checkNotNull(sqlParameters);
		//-----
		this.sqlQuery = sqlQuery;
		this.sqlParameters = sqlParameters;
		//-----
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public List<List<SqlParameter>> getSqlParametersForBatch() {
		return sqlParameters;
	}

	public List<SqlParameter> getSqlParameters() {
		Assertion.checkState(sqlParameters.size() <= 1, "when a query is not in batch mode only one list of parameters can be provided ");
		if (sqlParameters.size() == 0) {
			return Collections.emptyList();
		}
		return sqlParameters.get(0);
	}

}
