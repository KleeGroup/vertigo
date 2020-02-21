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
package io.vertigo.dynamox.search;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.util.DateUtil;

/**
 * @author  npiedeloup
 */
public final class DslListFilterBuilderTest {

	@Test
	public void testStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult, OtherAcceptedResult ...
				{ "ALL:#query#", "Test", "ALL:Test" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query*#", "Test", "ALL:(Test*)" }, //2
				{ "ALL:#query*#", "Test test2", "ALL:(Test* test2*)" }, //3
				{ "ALL:#+query#", "Test", "ALL:(+Test)" }, //4
				{ "ALL:#+query#", "Test test2", "ALL:(+Test +test2)" }, //5
				{ "+ALL:#query#", "Test", "+ALL:(Test)", "+ALL:Test" }, //6
				{ "+ALL:#query#", "Test test2", "+ALL:(Test test2)" }, //7
				{ "+ALL:#query#", "T", "+ALL:T" }, //8
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringGlobalModifierQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test", "ALL:(Test)", "ALL:Test" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query#*", "Test", "ALL:(Test)*", "ALL:(Test*)" }, //2
				{ "ALL:#query#*", "Test test2", "ALL:(Test test2)*", "ALL:((Test test2)*)" }, //3
				{ "ALL:+#query#", "Test", "ALL:+(Test)", "ALL:(+Test)" }, //4
				{ "ALL:+#query#", "Test test2", "ALL:+(Test test2)", "ALL:(+(Test test2))" }, //5
				{ "-ALL:+#query#*", "Test", "-ALL:+(Test)*", "-ALL:(+Test*)" }, //6
				{ "-ALL:+#query#*", "Test test2", "-ALL:+(Test test2)*", "-ALL:(+(Test test2)*)" }, //7
				{ "-ALL:+#query#*", "Test AND (test2 OR test3)", "-ALL:+(Test AND (test2 OR test3))*", "-ALL:(+(Test AND (test2 OR test3))*)" }, //8
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringAdvancedQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test or test2", "ALL:(Test or test2)" }, //0
				{ "ALL:#query#", "Test and test2", "ALL:(Test and test2)" }, //1
				{ "ALL:#query#", "Test OR test2", "ALL:(Test OR test2)" }, //2
				{ "ALL:#query#", "Test AND test2", "ALL:(Test AND test2)" }, //3
				{ "ALL:#query#", "Test AND ( test2 OR test3 )", "ALL:(Test AND ( test2 OR test3 ))" }, //4
				{ "ALL:#query*#", "Test AND test2", "ALL:(Test* AND test2*)" }, //5
				{ "ALL:#query*#", "Test AND (test2 OR test3)", "ALL:(Test* AND (test2* OR test3*))" }, //6
				{ "ALL:#+query*#", "Test AND (test2 OR test3)", "ALL:(+Test* AND (+test2* OR +test3*))" }, //7
				{ "+ALL:#query#", "Test or test2", "+ALL:(Test or test2)" }, //8
				{ "ALL:#+query~#", "Test AND (test2 OR test3)", "ALL:(+Test~ AND (+test2~ OR +test3~))" }, //9
				{ "ALL:#+query~1#", "Test AND (test2 OR test3)", "ALL:(+Test~1 AND (+test2~1 OR +test3~1))" }, //10
				{ "ALL:#+query#", "Test AND (test2^2 OR test3)", "ALL:(+Test AND (+test2^2 OR +test3))" }, //11
				{ "ALL:#+query^2#", "Test AND (test2 OR test3)", "ALL:(+Test^2 AND (+test2^2 OR +test3^2))" }, //12
				{ "ALL:#+query#^2", "Test AND (test2 OR test3)", "ALL:(+Test AND (+test2 OR +test3))^2" }, //13
				{ "ALL:#+query*#", "Test, test2, test3", "ALL:(+Test*, +test2*, +test3*)" }, //14
				{ "ALL:#query# +YEAR:[2000 to 2005]", "Test AND (test2 OR test3)", "ALL:(Test AND (test2 OR test3)) +YEAR:[2000 TO 2005]" }, //15
				{ "ALL:(#query# #query*# #Query~2#)", "Test test2", "ALL:((Test test2) Test* test2* (Test~2 test2~2))", "ALL:((Test test2) (Test* test2*) (Test~2 test2~2))" }, //16
				{ "ALL:(#query#^4 #query*#^2 #Query~2#)", "Test test2", "ALL:((Test test2)^4 (Test* test2*)^2 (Test~2 test2~2))" }, //17
				{ "+JOB_CODE:#+query*#", "00000-1111", "+JOB_CODE:(+00000-1111*)" }, //18
				{ "+JOB_CODE:#+query*#", "00000/1111", "+JOB_CODE:(+00000/1111*)" }, //19
				{ "+JOB_CODE:#+query*#", "130.IC", "+JOB_CODE:(+130.IC*)" }, //20
				{ "+JOB_CODE:+#query*#", "130.IC rouge", "+JOB_CODE:(+(130.IC* rouge*))" }, //21
				{ "PART_NUMBER:#+query*#", "130.IC rouge", "PART_NUMBER:(+130.IC* +rouge*)" }, //22
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringEscapedModeQuery() {
		final String[][] testQueries = new String[][] {

				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test or test2", "ALL:(Test or test2)" }, //0
				{ "ALL:#query#", "Test and test2", "ALL:(Test and test2)" }, //1
				{ "ALL:#query#", "Test Or test2", "ALL:(Test Or test2)" }, //2
				{ "ALL:#query#", "Test And test2", "ALL:(Test And test2)" }, //3
				{ "ALL:#query#", "Test OR test2", "ALL:(Test OR test2)" }, //4
				{ "ALL:#query#", "Test AND test2", "ALL:(Test AND test2)" }, //5
				{ "ALL:#query#?(escapeReserved)", "Test or test2", "ALL:(Test \\or test2)" }, //6
				{ "ALL:#query#?(escapeReserved)", "Test and test2", "ALL:(Test \\and test2)" }, //7
				{ "ALL:#query#?(escapeReserved)", "Test Or test2", "ALL:(Test \\Or test2)" }, //8
				{ "ALL:#query#?(escapeReserved)", "Test And test2", "ALL:(Test \\And test2)" }, //9
				{ "ALL:#query#?(escapeReserved)", "Test OR test2", "ALL:(Test \\OR test2)" }, //10
				{ "ALL:#query#?(escapeReserved)", "Test AND test2", "ALL:(Test \\AND test2)" }, //11
				{ "ALL:#query#?(removeReserved)", "Test or test2", "ALL:(Test test2)" }, //12
				{ "ALL:#query#?(removeReserved)", "Test and test2", "ALL:(Test test2)" }, //13
				{ "ALL:#query#?(removeReserved)", "Test OR test2", "ALL:(Test test2)" }, //14
				{ "ALL:#query#?(removeReserved)", "Test AND test2", "ALL:(Test test2)" }, //15
				{ "ALL:#query#?(removeReserved)", "Test Or test2", "ALL:(Test test2)" }, //16
				{ "ALL:#query#?(removeReserved)", "Test And test2", "ALL:(Test test2)" }, //17

				{ "ALL:#query#", "test +1 -2 =3 &&4 ||5 >6 <7 !8 (9 )a {b }c test2", "ALL:(test +1 -2 =3 &&4 ||5 >6 <7 !8 (9 )a {b }c test2)" }, //18
				{ "ALL:#query#", "test [1 ]2 ^3 \"4 ~5 *6 ?7 :8 \\9 /a test2", "ALL:(test [1 ]2 ^3 \"4 ~5 *6 ?7 :8 \\9 /a test2)" }, //19
				{ "ALL:#query#?(escapeReserved)", "test +1 -2 =3 &&4 ||5 >6 <7 !8 (9 )a {b }c test2",
						"ALL:(test \\+1 \\-2 \\=3 \\&\\&4 \\|\\|5 \\>6 \\<7 \\!8 \\(9 \\)a \\{b \\}c test2)" }, //20
				{ "ALL:#query#?(escapeReserved)", "test [1 ]2 ^3 \"4 ~5 *6 ?7 :8 \\9 /a test2",
						"ALL:(test \\[1 \\]2 \\^3 \\\"4 \\~5 \\*6 \\?7 \\:8 \\\\9 \\/a test2)" }, //21
				{ "ALL:#query#?(removeReserved)", "test +1 -2 =3 &&4 ||5 >6 <7 !8 (9 )a {b }c test2", "ALL:(test 1 2 3 4 5 6 7 8 9 a b c test2)" }, //22
				{ "ALL:#query#?(removeReserved)", "test [1 ]2 ^3 \"4 ~5 *6 ?7 :8 \\9 /a test2", "ALL:(test 1 2 3 4 5 6 7 8 9 a test2)" }, //23

				{ "ALL:#query#?(escapeReserved)", "Test ordonance test2", "ALL:(Test ordonance test2)" }, //24
				{ "ALL:#query#?(escapeReserved)", "Test andy test2", "ALL:(Test andy test2)" }, //25

				{ "ALL:#query#?(escapeReserved)", "Test meteor test2", "ALL:(Test meteor test2)" }, //26
				{ "ALL:#query#?(escapeReserved)", "Test nand test2", "ALL:(Test nand test2)" }, //27

				{ "ALL:#query*#?(escapeReserved)^2", "Test meteor test2", "ALL:(Test* meteor* test2*)^2" }, //28
				{ "ALL:#query*#?(escapeReserved)^2", "and", "ALL:(\\and*^2)" }, //29
				{ "ALL:#query*#?(escapeReserved)^2", "or", "ALL:(\\or*^2)" }, //30
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringEscapedQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test \\or test2", "ALL:(Test \\or test2)" }, //0
				{ "ALL:#query#", "Test \\and test2", "ALL:(Test \\and test2)" }, //1
				{ "ALL:#query#", "Test \\OR test2", "ALL:(Test \\OR test2)" }, //2
				{ "ALL:#query#", "Test \\AND test2", "ALL:(Test \\AND test2)" }, //3
				{ "ALL:#query#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(Test \\AND \\(test2 \\OR test3\\))" }, //4
				{ "ALL:#query*#", "Test \\AND test2", "ALL:(Test* \\AND* test2*)" }, //5
				{ "ALL:#query*#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(Test* \\AND* \\(test2* \\OR* test3\\)*)" }, //6
				{ "ALL:#+query*#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(+Test* +\\AND* +\\(test2* +\\OR* +test3\\)*)" }, //7
				{ "+ALL:#query#", "Test \\or test2", "+ALL:(Test \\or test2)" }, //8
				{ "ALL:#+query~#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(+Test~ +\\AND~ +\\(test2~ +\\OR~ +test3\\)~)" }, //9
				{ "ALL:#+query~1#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(+Test~1 +\\AND~1 +\\(test2~1 +\\OR~1 +test3\\)~1)" }, //10
				{ "ALL:#+query#", "Test \\AND \\(test2\\^2 \\OR test3\\)", "ALL:(+Test +\\AND +\\(test2\\^2 +\\OR +test3\\))" }, //11
				{ "ALL:#+query^2#", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(+Test^2 +\\AND^2 +\\(test2^2 +\\OR^2 +test3\\)^2)" }, //12
				{ "ALL:#+query#^2", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(+Test +\\AND +\\(test2 +\\OR +test3\\))^2" }, //13
				{ "ALL:#+query*#", "Test\\, test2\\, test3", "ALL:(+Test\\,* +test2\\,* +test3*)" }, //14
				{ "ALL:#query# +YEAR:[2000 to 2005]", "Test \\AND \\(test2 \\OR test3\\)", "ALL:(Test \\AND \\(test2 \\OR test3\\)) +YEAR:[2000 TO 2005]" }, //15
				{ "+JOB_CODE:#+query*#", "00000\\-1111", "+JOB_CODE:(+00000\\-1111*)" }, //18
				{ "+JOB_CODE:#+query*#", "00000\\/1111", "+JOB_CODE:(+00000\\/1111*)" }, //19
				{ "+JOB_CODE:#+query*#", "130\\.IC", "+JOB_CODE:(+130\\.IC*)" }, //20
				{ "+JOB_CODE:+#query*#", "130\\.IC rouge", "+JOB_CODE:(+(130\\.IC* rouge*))" }, //21
				{ "PART_NUMBER:#+query*#", "130\\.IC rouge", "PART_NUMBER:(+130\\.IC* +rouge*)" }, //22
				{ "PART_NUMBER:#+query*#", "130 \\-IC \\(rouge\\)", "PART_NUMBER:(+130* +\\-IC* +\\(rouge\\)*)" }, //23
				{ "PART_NUMBER:#+query*#", "130 \\O\\R \\(rouge\\)", "PART_NUMBER:(+130* +\\O\\R* +\\(rouge\\)*)" }, //24
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringBooleanQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "F1:#query# or F2:#query#", "Test", "F1:Test OR F2:Test" }, //0
				{ "F1:#query# and F2:#query#", "Test", "F1:Test AND F2:Test" }, //1
				{ "F1:#query# OR F2:#query#", "Test", "F1:Test OR F2:Test" }, //2
				{ "F1:#query# AND F2:#query#", "Test", "F1:Test AND F2:Test" }, //3
				{ "F1:#query# AND (F2:#query# OR F3:#query#)", "Test", "F1:Test AND (F2:Test OR F3:Test)" }, //4
				{ "(F1:#query# OR F2:#query#) AND (F3:#query# OR F4:#query#)", "Test", "(F1:Test OR F2:Test) AND (F3:Test OR F4:Test)" }, //5
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringBadBooleanQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test or ", "ALL:(Test \\or )" }, //0
				{ "ALL:#query#", "Test and ", "ALL:(Test \\and )" }, //1
				{ "ALL:#query#", "Test OR ", "ALL:(Test \\OR )" }, //2
				{ "ALL:#query#", "Test AND ", "ALL:(Test \\AND )" }, //3
				{ "ALL:#query#", "Test AND (test2 OR )", "ALL:(Test AND (test2 \\OR ))" }, //4
				{ "ALL:#query#", "Test AND (test2 OR test3", "ALL:(Test AND \\(test2 OR test3)" }, //5
				{ "ALL:#query*#", "Test AND ", "ALL:(Test* \\AND* )" }, //6
				{ "ALL:#query*#", "Test AND (test2 OR )", "ALL:(Test* AND (test2* \\OR* ))" }, //7
				{ "ALL:#+query*#", "Test AND (test2 OR )", "ALL:(+Test* AND (+test2* +\\OR* ))" }, //8
				{ "+ALL:#query#", "Test or ", "+ALL:(Test \\or )" }, //9
				{ "ALL:#+query~#", "Test AND (test2 OR ", "ALL:(+Test~ AND +\\(test2~ +\\OR~ )" }, //10
				{ "ALL:#+query~1#", "Test AND (test2 OR ", "ALL:(+Test~1 AND +\\(test2~1 +\\OR~1 )" }, //11
				{ "ALL:#+query#", "Test AND (test2^2 OR )", "ALL:(+Test AND (+test2^2 +\\OR ))" }, //12
				{ "ALL:#+query#", "Test AND (test2^2 OR ", "ALL:(+Test AND +\\(test2^2 +\\OR )" }, //13
				{ "ALL:#+query^2#", "Test AND (test2 OR )", "ALL:(+Test^2 AND (+test2^2 +\\OR^2 ))" }, //14
				{ "ALL:#+query#^2", "Test AND (test2 OR )", "ALL:(+Test AND (+test2 +\\OR ))^2" }, //15
				{ "ALL:#+query^2#", "Test AND (test2 OR ", "ALL:(+Test^2 AND +\\(test2^2 +\\OR^2 )" }, //16
				{ "ALL:#+query#^2", "Test AND (test2 OR ", "ALL:(+Test AND +\\(test2 +\\OR )^2" }, //17
				{ "ALL:#+query*#", "Test, test2, test3", "ALL:(+Test*, +test2*, +test3*)" }, //18
				{ "ALL:#query# +YEAR:[2000 to 2005]", "Test AND (test2 OR ", "ALL:(Test AND \\(test2 \\OR ) +YEAR:[2000 TO 2005]" }, //19

				{ "ALL:#query#", " or test2", "ALL:(\\or test2)" }, //20
				{ "ALL:#query#", " and test2", "ALL:(\\and test2)" }, //21
				{ "ALL:#query#", " OR test2", "ALL:(\\OR test2)" }, //22
				{ "ALL:#query#", " AND test2", "ALL:(\\AND test2)" }, //23
				{ "ALL:#query#", " AND ( OR test3)", "ALL:(\\AND ( \\OR test3))" }, //24
				{ "ALL:#query*#", " AND test2", "ALL:(\\AND* test2*)" }, //25
				{ "ALL:#query*#", " AND ( OR test3)", "ALL:(\\AND* ( \\OR* test3*))" }, //26
				{ "ALL:#+query*#", " AND OR test3)", "ALL:(+\\AND* OR +test3\\)*)" }, //27
				{ "+ALL:#query#", " or test2", "+ALL:(\\or test2)" }, //28
				{ "ALL:#+query~#", " AND ( OR test3)", "ALL:(+\\AND~ ( +\\OR~ +test3~))" }, //29
				{ "ALL:#+query~1#", " AND OR test3)", "ALL:(+\\AND~1 OR +test3\\)~1)" }, //30
				{ "ALL:#+query#", " AND ( OR test3)", "ALL:(+\\AND ( +\\OR +test3))" }, //31
				{ "ALL:#+query^2#", " AND ( OR test3)", "ALL:(+\\AND^2 ( +\\OR^2 +test3^2))" }, //32
				{ "ALL:#+query#^2", " AND OR test3)", "ALL:(+\\AND OR +test3\\))^2" }, //33
				{ "ALL:#query# +YEAR:[2000 to 2005]", "Test AND test2 OR test3)", "ALL:(Test AND test2 OR test3\\)) +YEAR:[2000 TO 2005]" }, //34
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testNullableStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "", "ALL:*" }, //0
				{ "+YEAR:[2000 to #query#!(*)]", "", "+YEAR:[2000 TO *]" }, //1
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringOverridedFieldQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "OTHER:Test", "OTHER:(Test)", "OTHER:Test" }, //0
				{ "ALL:#+query*#", "OTHER:Test", "OTHER:(Test)", "OTHER:Test" }, //1
				{ "ALL:#query#", "OTHER:Test test2", "OTHER:(Test) ALL:(test2)", "OTHER:Test ALL:test2" }, //2
				{ "ALL:#query#", "Test OTHER:test2", "ALL:(Test) OTHER:(test2)", "ALL:Test OTHER:test2" }, //3
				{ "ALL:#query#", "OTHER:Test test2 test3", "OTHER:(Test) ALL:(test2 test3)", "OTHER:Test ALL:(test2 test3)" }, //4
				{ "ALL:#query#", "Test OTHER:test2 test3", "ALL:(Test) OTHER:(test2) ALL:(test3)", "ALL:Test OTHER:test2 ALL:test3" }, //5
				{ "ALL:#query#", "Test test2 OTHER:test3", "ALL:(Test test2) OTHER:(test3)", "ALL:(Test test2) OTHER:test3" }, //6
				{ "ALL:#+query*#", "Test test2 OTHER:test3", "ALL:(+Test* +test2*) OTHER:(test3)", "ALL:(+Test* +test2*) OTHER:test3" }, //7
				{ "+ALL:#query#", "Test test2 OTHER:test3", "+ALL:(Test test2) OTHER:(test3)", "+ALL:(Test test2) OTHER:test3" }, //8
				{ "ALL:#+query*#", "Test OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)" }, //9
				{ "+ALL:#query#", "Test OTHER:(test2 test3)", "+ALL:(Test) OTHER:(test2 test3)", "+ALL:Test OTHER:(test2 test3)" }, //10
				{ "ALL:#query#", "Test -OTHER:(test2 test3)", "ALL:(Test) -OTHER:(test2 test3)", "ALL:Test -OTHER:(test2 test3)" }, //11
				{ "ALL:#+query*#", "Test test2~", "ALL:(+Test* +test2~)" }, //12
				{ "ALL:#query#", "Test -OTHER:(test2 test3) Test4", "ALL:(Test) -OTHER:(test2 test3) ALL:(Test4)", "ALL:Test -OTHER:(test2 test3) ALL:Test4" }, //13
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringOverridedModifierQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "+Test", "ALL:(+Test)" }, //0
				{ "ALL:#query#", "+Test test2", "ALL:(+Test test2)" }, //1
				{ "ALL:#query#", "Test*", "ALL:(Test*)" }, //2
				{ "ALL:#query#", "Test* test2", "ALL:(Test* test2)" }, //3
				{ "ALL:#+query#", "-Test", "ALL:(-Test)" }, //4
				{ "ALL:#+query#", "-Test test2", "ALL:(-Test +test2)" }, //5
				{ "+ALL:#query#", "-Test", "+ALL:(-Test)" }, //6
				{ "+ALL:#query#", "-Test test2", "+ALL:(-Test test2)" }, //7
				{ "ALL:#+query*#", "-Test", "ALL:(-Test*)" }, //8
				{ "ALL:#+query*#", "-Test test2", "ALL:(-Test* +test2*)" }, //9
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringFixedQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:fixedValue", "Test", "ALL:fixedValue" },
				{ "ALL:fixedValue", "Test test2", "ALL:fixedValue" },
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringEmptyQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#+query*# +security:fixedValue", "Test", "ALL:(+Test*) +security:fixedValue" }, //0
				{ "ALL:#+query*# +security:fixedValue", "*", "ALL:* +security:fixedValue" }, //1
				{ "ALL:#+query*# +security:fixedValue", "*:*", "*:* +security:fixedValue" }, //2
				{ "ALL:#+query*# +security:fixedValue", " ", "ALL:* +security:fixedValue" }, //3
				{ "ALL:#+query*# +security:fixedValue", "", "ALL:* +security:fixedValue" }, //4
				{ "ALL:#+query*# +security:fixedValue", "YEAR:*", "YEAR:* +security:fixedValue" }, //5
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringSpecialCharQuery() {
		//ElasticSearch reserved characters are: + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#+query*#", "-Test", "ALL:(-Test*)" },
				{ "ALL:#+query*#", "Test-", "ALL:(+Test-)" },
				{ "ALL:#+query*#", "-Test-", "ALL:(-Test-)" },
				{ "ALL:#+query*#", "+Test+", "ALL:(+Test+)" },
				{ "ALL:#+query*#", "=Test=", "ALL:(=Test=)" },
				//{ "ALL:#+query*#", "&Test&", "ALL:(&Test&)" },
				//{ "ALL:#+query*#", "|Test|", "ALL:(|Test|)" },
				{ "ALL:#+query*#", ">Test>", "ALL:(>Test>)" },
				{ "ALL:#+query*#", "<Test<", "ALL:(<Test<)" },
				{ "ALL:#+query*#", "!Test!", "ALL:(!Test!)" },
				{ "ALL:#+query*#", "^Test^", "ALL:(^Test^)" },
				{ "ALL:#+query*#", "\"Test\"", "ALL:(\"Test\")", "ALL:\"Test\"" },
				{ "ALL:#+query*#", "~Test~", "ALL:(~Test~)" },
				{ "ALL:#+query*#", "*Test*", "ALL:(*Test*)" },
				{ "ALL:#+query*#", "?Test?", "ALL:(?Test?)" },
				//{ "ALL:#+query*#", ":Test:", "ALL:(:Test:)" },
				//{ "ALL:#+query*#", "\\Test\\", "ALL:(\\Test\\)" },
				//{ "ALL:#+query*#", "/Test/", "ALL:(/Test/)" },
				{ "ALL:#+query*#", ",Test,", "ALL:(,+Test*,)" },
				{ "ALL:#+query*#", ";Test;", "ALL:(;+Test*;)" },
				{ "ALL:#+query*#", "(Test)", "ALL:((+Test*))", "ALL:(+Test*)" },
				{ "ALL:#+query*#", "[Test]", "ALL:([Test])", "ALL:[Test]" },
				{ "ALL:#+query*#", "l'avion n'est pas là", "ALL:(+l'avion* +n'est* +pas* +là*)" },
				{ "ALL:#\"query\"#", "Andrey Mariette", "ALL:(\"Andrey\" \"Mariette\")" },
				{ "ALL:\"#query#\"", "Andrey Mariette", "ALL:\"Andrey Mariette\"" },
				{ "ALL:+\"#query#\"", "Andrey Mariette", "ALL:(+\"Andrey Mariette\")" },
				{ "ALL:(\"#query#\")", "Andrey Mariette", "ALL:(\"Andrey Mariette\")" },
				{ "ALL:(#query# #query#)", "Andrey Mariette", "ALL:((Andrey Mariette) (Andrey Mariette))" },
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringHackQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query# +security:fixedValue", "Test OR 1=1", "ALL:(Test OR 1=1) +security:fixedValue" },
				{ "ALL:#query# +security:\"fixedValue\"", "Test OR 1=1", "ALL:(Test OR 1=1) +security:\"fixedValue\"" },
				{ "ALL:#query# +security:fixedValue", "Test) OR (1=1", "ALL:(Test\\) OR \\(1=1) +security:fixedValue" }, //don't affect security
				{ "ALL:#query# +security:fixedValue", "*) OR ", "ALL:(*\\) \\OR ) +security:fixedValue" },
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testBeanQuery() {
		final LocalDate dateTest1 = DateUtil.parseToLocalDate("230715", "ddMMyy");
		final LocalDate dateTest2 = DateUtil.parseToLocalDate("230715", "ddMMyy");
		final Instant instantTest1 = LocalDateTime.of(2015, 07, 23, 12, 30, 00).toInstant(ZoneOffset.UTC);
		final Instant instantTest2 = LocalDateTime.of(2015, 07, 23, 16, 45, 00).toInstant(ZoneOffset.UTC);

		final TestBean testBean = new TestBean("Test", "Test test2", dateTest1, dateTest2, instantTest1, instantTest2, 5, 10);
		final Object[][] testQueries = new Object[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#str1#", testBean, "ALL:(Test)", "ALL:Test" }, //0
				{ "ALL:#str2#", testBean, "ALL:(Test test2)" }, //1
				{ "ALL:#date1#", testBean, "ALL:\"2015-07-23\"" }, //2
				{ "ALL:#date2#", testBean, "ALL:\"2015-07-23\"" }, //3
				{ "ALL:#int1#", testBean, "ALL:5" }, //4
				{ "ALL:#int2#", testBean, "ALL:10" }, //5
				{ "ALL:[#int1# to #int2#]", testBean, "ALL:[5 TO 10]" }, //6
				{ "ALL:[#int1# TO #int2#]", testBean, "ALL:[5 TO 10]" }, //7
				{ "ALL:[#date1# to #date2#]", testBean, "ALL:[\"2015-07-23\" TO \"2015-07-23\"]" }, //8
				{ "ALL:[#instant1# to #instant2#]", testBean, "ALL:[\"2015-07-23T12:30:00Z\" TO \"2015-07-23T16:45:00Z\"]" }, //9
				{ "ALL:[#int1# to #null#]", testBean, "ALL:[5 TO *]" }, //10
				{ "ALL:[#int1# to #null#!(*)]", testBean, "ALL:[5 TO *]" }, //11
				{ "ALL:[#null#!(*) to #int2#]", testBean, "ALL:[* TO 10]" }, //12
				{ "ALL:[#null# to #null#]", testBean, "" }, //13
				{ "ALL:[ #null# to #null# ]", testBean, "ALL:[  ]", "" }, //14
				{ "ALL:[#date1# to #null#!(*)]", testBean, "ALL:[\"2015-07-23\" TO *]" }, //15
				{ "ALL:[#instant1# to #null#!(*)]", testBean, "ALL:[\"2015-07-23T12:30:00Z\" TO *]" }, //16
				{ "ALL:[#null#!(*) to #null#!(*)]", testBean, "ALL:[* TO *]", "" }, //17
				{ "ALL:{#int1# TO #int2#]", testBean, "ALL:{5 TO 10]" }, //18
				{ "ALL:[#int1# TO #int2#}", testBean, "ALL:[5 TO 10}" }, //19
				{ "ALL:{#int1# TO #int2#}", testBean, "ALL:{5 TO 10}" }, //20
				{ "+DATE_SESSION:[* to #date1#}", testBean, "+DATE_SESSION:[* TO \"2015-07-23\"}" }, //21
				{ "+DATE_SESSION:[* to #instant1#}", testBean, "+DATE_SESSION:[* TO \"2015-07-23T12:30:00Z\"}" }, //22
				{ "+DATE_SESSION:[#date1# to *}", testBean, "+DATE_SESSION:[\"2015-07-23\" TO *}" }, //23
				{ "+DATE_SESSION:[#instant1# to *}", testBean, "+DATE_SESSION:[\"2015-07-23T12:30:00Z\" TO *}" }, //24
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str2# +DATE_MODIFICATION_DEPUIS:[#instant1#!(*) TO *] +DATE_NAISSANCE:#instant2#!(*)", testBean, "+(NOM_NAISSANCE:(+Test) OR NOM:(+Test)) +PRENOM:(+Test +test2) +DATE_MODIFICATION_DEPUIS:[\"2015-07-23T12:30:00Z\" TO *] +DATE_NAISSANCE:\"2015-07-23T16:45:00Z\"" }, //25
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str2# +DATE_MODIFICATION_DEPUIS:[#date1#!(*) TO *] +DATE_NAISSANCE:#date2#!(*)", testBean, "+(NOM_NAISSANCE:(+Test) OR NOM:(+Test)) +PRENOM:(+Test +test2) +DATE_MODIFICATION_DEPUIS:[\"2015-07-23\" TO *] +DATE_NAISSANCE:\"2015-07-23\"" }, //26
		};
		testObjectFixedQuery(testQueries);
	}

	@Test
	public void testMultiQuery() {
		final LocalDate dateTest1 = DateUtil.parseToLocalDate("230715", "ddMMyy");
		final Instant instantTest1 = LocalDateTime.of(2015, 07, 23, 12, 30, 00).toInstant(ZoneOffset.UTC);
		final TestBean testBeanNull = new TestBean(null, "Test test2", null, dateTest1, null, instantTest1, null, 5);
		final TestBean testBeanEmpty = new TestBean("", "Test test2", null, dateTest1, null, instantTest1, null, 5);
		final TestBean testBeanOne = new TestBean("12", "Test test2", null, null, null, null, null, null);
		final TestBean testBeanMultiple = new TestBean("12 13", "Test test2", null, null, null, null, null, null);
		final TestBean testBeanMultipleCode = new TestBean("CODE_1 CODE_3", "Test test2", null, null, null, null, null, null);
		final TestBean testBeanMultipleTrackIt = new TestBean(null, "item", null, null, null, null, null, null);
		final Object[][] testQueries = new Object[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanNull, " +ALL:(Test test2)", "+ALL:(Test test2)" }, //0
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanEmpty, "+PRO_ID:* +ALL:(Test test2)" }, //1
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanOne, "+PRO_ID:(12) +ALL:(Test test2)", "+PRO_ID:12 +ALL:(Test test2)" }, //2
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanMultiple, "+PRO_ID:(12 13) +ALL:(Test test2)" }, //3
				{ "+PRO_ID:#+str1# +ALL:#str2#", testBeanMultiple, "+PRO_ID:(+12 +13) +ALL:(Test test2)" }, //4
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanMultipleCode, "+PRO_ID:(CODE_1 CODE_3) +ALL:(Test test2)" }, //5
				{ "+PRO_ID:#+str1# +ALL:#str2#", testBeanMultipleCode, "+PRO_ID:(+CODE_1 +CODE_3) +ALL:(Test test2)" }, //6
				{ "+(PRO_ID:#str1#) +ALL:#str2#", testBeanNull, "+ALL:(Test test2)" }, //7
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#date2#!(*) TO *] +DATE_NAISSANCE:#date1#!(*)", testBeanNull, "+DATE_MODIFICATION_DEPUIS:[\"2015-07-23\" TO *] +DATE_NAISSANCE:*" }, //8
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#date2#!(*) TO *] +DATE_NAISSANCE:#date1#!(*)", testBeanEmpty, "+(NOM_NAISSANCE:* OR NOM:*) +PRENOM:* +DATE_MODIFICATION_DEPUIS:[\"2015-07-23\" TO *] +DATE_NAISSANCE:*" }, //9
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#date2#!(*) TO *] +DATE_NAISSANCE:#date1#!(*)", testBeanNull, "+DATE_MODIFICATION_DEPUIS:[\"2015-07-23\" TO *] +DATE_NAISSANCE:*" }, //10
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#instant2#!(*) TO *] +DATE_NAISSANCE:#instant1#!(*)", testBeanNull, "+DATE_MODIFICATION_DEPUIS:[\"2015-07-23T12:30:00Z\" TO *] +DATE_NAISSANCE:*" }, //11
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#instant2#!(*) TO *] +DATE_NAISSANCE:#instant1#!(*)", testBeanEmpty, "+(NOM_NAISSANCE:* OR NOM:*) +PRENOM:* +DATE_MODIFICATION_DEPUIS:[\"2015-07-23T12:30:00Z\" TO *] +DATE_NAISSANCE:*" }, //12
				{ "+(NOM_NAISSANCE:#+str1# OR NOM:#+str1#) +PRENOM:#+str1# +DATE_MODIFICATION_DEPUIS:[#instant2#!(*) TO *] +DATE_NAISSANCE:#instant1#!(*)", testBeanNull, "+DATE_MODIFICATION_DEPUIS:[\"2015-07-23T12:30:00Z\" TO *] +DATE_NAISSANCE:*" }, //13
				{ "+ITM_ID:#int1# +OPE_STATUS_CODE_NOT_ANALYZED:#str2# PART_NUMBER:#str1#^10 +[PART_NUMBER^10,DESCRIPTION_TRACKIT,COLLECTIONS,FAMILY]:#+str2*#", testBeanMultipleTrackIt,
						"+OPE_STATUS_CODE_NOT_ANALYZED:item +(+(PART_NUMBER:(item*)^10 DESCRIPTION_TRACKIT:(item*) COLLECTIONS:(item*) FAMILY:(item*)))" }, //14
				{ "+COM_ID:#str1# +INC_AGENTS_ACTIFS:#str1# +QUA_ID:#str1# +COR_ID:#str1# +COG_ID:#str1# +STR_ID:#str1# +MCL_ID_1:#str1# +MCL_ID_2:#str1# +MCL_ID_3:#str1# +MCL_ID_4:#str1# +MCL_ID_5:#str1# +(LISTE_MCL_ID:#str2#) +MOT_CLE_SUP:#str2# +(DATE_SEANCE:[#date1#!(*) to #date1#!(*)] INC_SEANCES_NULL:#booTrue#)",
						testBeanNull, "+MOT_CLE_SUP:(Test test2) +(LISTE_MCL_ID:(Test test2)) +(INC_SEANCES_NULL:true)" //15
				}
		};
		//testObjectFixedQuery(testQueries[12]);
		testObjectFixedQuery(testQueries);
	}

	@Test
	public void testMultiFieldQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "+FIELD_1:#query*#", "Test test2", "+FIELD_1:(Test* test2*)" }, //0
				{ "[FIELD_1,FIELD_2]:#query*#", "Test test2", "FIELD_1:(Test* test2*) FIELD_2:(Test* test2*)" }, //1
				{ "+[FIELD_1,FIELD_2]:#query*#", "Test test2", "+FIELD_1:(Test* test2*) +FIELD_2:(Test* test2*)" }, //2
				{ "+([FIELD_1,FIELD_2]:#query*#)", "Test test2", "+(FIELD_1:(Test* test2*) FIELD_2:(Test* test2*))" }, //3
				{ "[FIELD_1,FIELD_2]:#+query*#", "Test test2", "(+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*)))" }, //4
				{ "[FIELD_1,FIELD_2]:#+query*#^2", "Test test2", "(+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*)))^2" }, //5
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "Test test2", "(+(FIELD_1:(Test*) FIELD_2:(Test*)^2) +(FIELD_1:(test2*) FIELD_2:(test2*)^2))" }, //6
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "Test ALL:test2", "(+(FIELD_1:(Test*) FIELD_2:(Test*)^2)) ALL:test2" }, //7
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "ALL:test2 Test", "ALL:test2 +(FIELD_1:(Test*) FIELD_2:(Test*)^2)" }, //8
				{ "+[FIELD_1,FIELD_2]:(#query#^4 #+query*#^2 #query~2#)", "Test test2",
						"+FIELD_1:(Test test2)^4 +FIELD_2:(Test test2)^4 +((+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*)))^2) +FIELD_1:(Test~2 test2~2) +FIELD_2:(Test~2 test2~2)" }, //9
				{ "+[FIELD_1 \n\r\t  , \n\r\t  FIELD_2]:(#query#^4 #query*#^2 #query~2#)", "Test test2",
						"+FIELD_1:(Test test2)^4 +FIELD_2:(Test test2)^4 +FIELD_1:(Test* test2*)^2 +FIELD_2:(Test* test2*)^2 +FIELD_1:(Test~2 test2~2) +FIELD_2:(Test~2 test2~2)" }, //10
				{ "+[FIELD_1,FIELD_2]:(#query#^4 #query*#^2 #query~2#)", "Test test2",
						"+FIELD_1:(Test test2)^4 +FIELD_2:(Test test2)^4 +FIELD_1:(Test* test2*)^2 +FIELD_2:(Test* test2*)^2 +FIELD_1:(Test~2 test2~2) +FIELD_2:(Test~2 test2~2)" }, //11
				{ "+[FIELD_1,FIELD_2]:#+query*#", "Test test2", "+(+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*)))" }, //12
				{ "+[FIELD_1,FIELD_2*]:#+query#", "Test test2", "+(+(FIELD_1:Test FIELD_2:Test*) +(FIELD_1:test2 FIELD_2:test2*))" }, //13

		};
		testStringFixedQuery(testQueries);
	}

	private <O> ListFilterBuilder<O> createListFilterBuilder(final Class<O> criteriaType) {
		return (ListFilterBuilder<O>) new DslListFilterBuilder<>();
	}

	int getPreferedResult() {
		return 3;
	}

	private void testStringFixedQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			final ListFilter listFilter = createListFilterBuilder(String.class)
					.withBuildQuery(testParam[0])
					.withCriteria(testParam[1])
					.build();
			final String result = listFilter.getFilterValue();
			final String expectedResult = testParam[Math.min(getPreferedResult(), testParam.length - 1)];
			Assertions.assertEquals(expectedResult, result, "Built query #" + i + " incorrect");
			i++;
		}
	}

	private void testObjectFixedQuery(final Object[]... testData) {
		int i = 0;
		for (final Object[] testParam : testData) {
			final ListFilter listFilter = createListFilterBuilder(Object.class)
					.withBuildQuery((String) testParam[0])
					.withCriteria(testParam[1])
					.build();
			final String result = listFilter.getFilterValue();
			final Object expectedResult = testParam[Math.min(getPreferedResult(), testParam.length - 1)];
			Assertions.assertEquals(expectedResult, result, "Built query #" + i + " incorrect");
			i++;
		}
	}

	public static class TestBean {

		private final String str1;
		private final String str2;
		private final LocalDate date1;
		private final LocalDate date2;
		private final Instant instant1;
		private final Instant instant2;
		private final Integer int1;
		private final Integer int2;

		TestBean(
				final String str1,
				final String str2,
				final LocalDate date1,
				final LocalDate date2,
				final Instant instant1,
				final Instant instant2,
				final Integer int1,
				final Integer int2) {
			this.str1 = str1;
			this.str2 = str2;
			this.date1 = date1;
			this.date2 = date2;
			this.instant1 = instant1;
			this.instant2 = instant2;
			this.int1 = int1;
			this.int2 = int2;
		}

		public String getStr1() {
			return str1;
		}

		public String getStr2() {
			return str2;
		}

		public LocalDate getDate1() {
			return date1;
		}

		public LocalDate getDate2() {
			return date2;
		}

		public Instant getInstant1() {
			return instant1;
		}

		public Instant getInstant2() {
			return instant2;
		}

		public Integer getInt1() {
			return int1;
		}

		public Integer getInt2() {
			return int2;
		}

		public Boolean getBooNull() {
			return null;
		}

		public boolean getBooTrue() {
			return true;
		}

		public Object getNull() {
			return null;
		}

	}

}
