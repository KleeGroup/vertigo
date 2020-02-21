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
package io.vertigo.account.authorization.dsl;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.account.impl.authorization.dsl.translator.SearchSecurityRuleTranslator;
import io.vertigo.account.impl.authorization.dsl.translator.SqlSecurityRuleTranslator;

/**
 * @author  npiedeloup
 */
public final class DslSecurityRulesBuilderTest {

	@Test
	public void testStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult, OtherAcceptedResult ...
				{ "ALL=${query}", "Test", "ALL=Test", "(+ALL:Test)" }, //0
				{ "ALL=${query}", "'Test test2'", "ALL='Test test2'", "(+ALL:'Test test2')" }, //1
				{ "ALL=${query} && OTHER='VALID'", "Test", "ALL=Test AND OTHER='VALID'", "(+ALL:Test +OTHER:'VALID')" }, //2
				{ "ALL=${query} || OTHER='VALID'", "Test", "ALL=Test OR OTHER='VALID'", "(ALL:Test OTHER:'VALID')" }, //3
				{ "(ALL=${query} || OTHER='VALID')", "Test", "(ALL=Test OR OTHER='VALID')", "(ALL:Test OTHER:'VALID')" }, //4
				{ "((ALL=${query} || OTHER='VALID') && (ALL=${query} || OTHER='VALID'))", "Test",
						"((ALL=Test OR OTHER='VALID') AND (ALL=Test OR OTHER='VALID'))",
						"(+(ALL:Test OTHER:'VALID') +(ALL:Test OTHER:'VALID'))" }, //5
				{ "(ALL=${query} || OTHER='VALID') && (ALL=${query} || OTHER='VALID')", "Test",
						"(ALL=Test OR OTHER='VALID') AND (ALL=Test OR OTHER='VALID')",
						"(+(ALL:Test OTHER:'VALID') +(ALL:Test OTHER:'VALID'))" }, //6
				//{ "ALL>${query}", "'Test'", "ALL like 'Test' || '%'" }, //3

		};
		testSearchAndSqlQuery(testQueries);
	}

	private void testSearchAndSqlQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			testSqlQuery(testParam, i);
			testSearchQuery(testParam, i);
			i++;
		}
	}

	int getSqlResult() {
		return 2;
	}

	int getSearchResult() {
		return 3;
	}

	private void testSqlQuery(final String[] testParam, final int i) {
		final SqlSecurityRuleTranslator securityRuleTranslator = new SqlSecurityRuleTranslator()
				.withRule(testParam[0])
				.withCriteria(Collections.singletonMap("query", Collections.singletonList(testParam[1])));
		final String result = securityRuleTranslator.toSql();
		final String expectedResult = testParam[Math.min(getSqlResult(), testParam.length - 1)];
		Assertions.assertEquals(expectedResult, result, "Built sql query #" + i + " incorrect");
	}

	private void testSearchQuery(final String[] testParam, final int i) {
		final SearchSecurityRuleTranslator securityRuleTranslator = new SearchSecurityRuleTranslator()
				.withRule(testParam[0])
				.withCriteria(Collections.singletonMap("query", Collections.singletonList(testParam[1])));
		final String result = securityRuleTranslator.toSearchQuery();
		final String expectedResult = testParam[Math.min(getSearchResult(), testParam.length - 1)];
		Assertions.assertEquals(expectedResult, result, "Built search query #" + i + " incorrect");
	}

}
