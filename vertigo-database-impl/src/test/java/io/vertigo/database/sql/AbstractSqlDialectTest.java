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

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.database.sql.vendor.SqlDialect;

/**
 *
 * @author mlaroche
 */
public abstract class AbstractSqlDialectTest {

	@Test
	public void testInsertQuery() {
		final SqlDialect sqlDialect = getDialect();
		final String insertQuery = sqlDialect.createInsertQuery("id", Collections.singletonList("title"), "SEQ_", "MOVIE");
		Assertions.assertEquals(getExpectedInsertQuery(), insertQuery);
	}

	@Test
	public void testSelectForUpdateWildcardQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "*", "id");
		Assertions.assertEquals(getExpectedSelectForUpdateWildCardQuery(), selectForUpdateQuery);
	}

	@Test
	public void testSelectForUpdateFieldsQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "ID, TITLE", "id");
		Assertions.assertEquals(getExpectedSelectForUpdateFieldsQuery(), selectForUpdateQuery);
	}

	@Test
	public void testAppendMaxRows() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendListState(stringBuilder, 100, 0, null, false);
		final String query = stringBuilder.toString();
		Assertions.assertEquals(getExpectedAppendMaxRowsQuery(), query);
	}

	@Test
	public void testAppendSkipRows() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendListState(stringBuilder, null, 10, null, false);
		final String query = stringBuilder.toString();
		Assertions.assertEquals(getExpectedAppendSkipRowsQuery(), query);
	}

	@Test
	public void testAppendSort() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendListState(stringBuilder, null, 0, "title", false);
		final String query = stringBuilder.toString();
		Assertions.assertEquals(getExpectedAppendSortQuery(), query);
	}

	@Test
	public void testAppendSortDesc() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendListState(stringBuilder, null, 0, "title", true);
		final String query = stringBuilder.toString();
		Assertions.assertEquals(getExpectedAppendSortDescQuery(), query);
	}

	public abstract SqlDialect getDialect();

	public abstract String getExpectedInsertQuery();

	public abstract String getExpectedSelectForUpdateWildCardQuery();

	public abstract String getExpectedSelectForUpdateFieldsQuery();

	public abstract Optional<String> getExpectedCreatePrimaryKeyQuery();

	public String getExpectedAppendMaxRowsQuery() {
		return "select * from MOVIE order by 1 offset 0 rows fetch next 100 rows only";
	}

	public String getExpectedAppendSkipRowsQuery() {
		return "select * from MOVIE order by 1 offset 10 rows";
	}

	public String getExpectedAppendSortQuery() {
		return "select * from MOVIE order by TITLE";
	}

	public String getExpectedAppendSortDescQuery() {
		return "select * from MOVIE order by TITLE desc";
	}

}
