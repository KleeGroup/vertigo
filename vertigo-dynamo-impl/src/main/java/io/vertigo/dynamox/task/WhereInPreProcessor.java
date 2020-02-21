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
package io.vertigo.dynamox.task;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Ce processor permet de remplacer le Where XXX_ID in (#YYY.ROWNUM.ZZZ_ID#) ou (#YYY.ROWNUM#).
 * @author npiedeloup
 */
final class WhereInPreProcessor {

	private static final int MASTER_TABLE_FK_GROUP = 1;
	private static final int OPTIONNAL_NOT_GROUP = 2;
	private static final int DTC_INPUTNAME_GROUP = 3;
	private static final int DTC_INPUT_PK_GROUP = 4;

	private static final String REGEXP_CHECK_PATTERN = "\\s(?:IN|in).+#.+(?:ROWNUM|rownum).*#";
	private static final Pattern JAVA_CHECK_PATTERN = Pattern.compile(REGEXP_CHECK_PATTERN);

	private static final String REGEXP_PATTERN = "\\W([a-zA-Z0-9_\\.]+)\\s+((?:NOT|not)\\s+)?(?:IN|in)\\s+\\(\\s*#([a-z][a-zA-Z0-9]*)\\.(?:ROWNUM|rownum)(?:\\.+([a-z][a-zA-Z0-9]*))?#\\s*\\)";
	private static final Pattern JAVA_PATTERN = Pattern.compile(REGEXP_PATTERN);

	private static final int NB_MAX_WHERE_IN_ITEM = 1000;
	private static final char IN_CHAR = '#';
	private final Map<TaskAttribute, Object> inTaskAttributes;

	/**
	 * Contructeur.
	 * @param inTaskAttributes Valeur des paramètres
	 */
	WhereInPreProcessor(final Map<TaskAttribute, Object> inTaskAttributes) {
		Assertion.checkNotNull(inTaskAttributes);
		//-----
		this.inTaskAttributes = inTaskAttributes;
	}

	/**
	 * @param sqlQuery Query to process
	 * @return Processed query
	 */
	public String evaluate(final String sqlQuery) {
		//On commence par vérifier la présence des mot clés.
		if (containsKeywords(sqlQuery)) {
			return doEvaluate(sqlQuery);
		}
		return sqlQuery;
	}

	private static boolean containsKeywords(final String sqlQuery) {
		//On vérifie la précense de tous les mots clés (.rownum., in, #)
		return JAVA_CHECK_PATTERN.matcher(sqlQuery).find(); //fast check
	}

	private TaskAttribute obtainInTaskAttribute(final String attributeName) {
		return inTaskAttributes.keySet()
				.stream()
				.filter(attribute -> attribute.getName().equals(attributeName))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(StringUtil.format("Attribute {0} not found.", attributeName)));
	}

	private String doEvaluate(final String sqlQuery) {
		final StringBuilder buildQuery = new StringBuilder(sqlQuery.length());
		int lastMatchOffset = 0;
		final Matcher matcher = JAVA_PATTERN.matcher(sqlQuery);
		while (matcher.find()) {
			//The first matched char is a blank, we keep it (matcher.start+1)
			buildQuery.append(sqlQuery.substring(lastMatchOffset, matcher.start() + 1));
			lastMatchOffset = matcher.end();
			final String fkFieldName = matcher.group(MASTER_TABLE_FK_GROUP);
			final String pkFieldName = matcher.group(DTC_INPUT_PK_GROUP);
			final String inputParamName = matcher.group(DTC_INPUTNAME_GROUP);
			final boolean isNotIn = matcher.group(OPTIONNAL_NOT_GROUP) != null; //null if not found
			final TaskAttribute attribute = obtainInTaskAttribute(inputParamName);
			Assertion.checkState(attribute.getDomain().isMultiple(), "Attribute {0} can't be use in WherInPreProcessor. Check it was declared as IN and is DtList type.", inputParamName);

			//-----
			final List<?> listObject = (List<?>) inTaskAttributes.get(attribute);
			if (listObject.isEmpty()) {
				//where XX not in <<empty>> => always true
				//where XX in <<empty>> => always false
				buildQuery.append(isNotIn ? "1=1" : "1=2");
			} else {
				//-----
				final boolean moreThanOneWhereIn = listObject.size() > NB_MAX_WHERE_IN_ITEM;
				if (moreThanOneWhereIn) {
					buildQuery.append("( ");
				}
				appendValuesToSqlQuery(
						buildQuery,
						fkFieldName,
						pkFieldName,
						inputParamName,
						isNotIn,
						listObject,
						attribute.getDomain().getScope().isPrimitive(),
						moreThanOneWhereIn);
				if (moreThanOneWhereIn) {
					buildQuery.append(')');
				}
			}
		}
		Assertion.checkState(lastMatchOffset > 0, "WhereInPreProcessor not applied. Keywords found but query doesn't match. Check syntaxe : XXX_ID <<not>> in (#YYY.ROWNUM.ZZZ_ID#) of {0}", sqlQuery);

		buildQuery.append(sqlQuery.substring(lastMatchOffset));
		return buildQuery.toString();
	}

	private static void appendValuesToSqlQuery(
			final StringBuilder buildQuery,
			final String fkFieldName,
			final String pkFieldName,
			final String inputParamName,
			final boolean isNotIn,
			final List<?> listObject,
			final boolean isPrimitive,
			final boolean moreThanOneWhereIn) {
		buildQuery.append(fkFieldName);
		buildQuery.append(isNotIn ? " NOT IN (" : " IN (");
		//-----
		String separator = "";
		int index = 1;
		for (final Object object : listObject) {
			buildQuery
					.append(separator)
					.append(IN_CHAR)
					.append(inputParamName)
					.append('.')
					.append(String.valueOf(listObject.indexOf(object)));
			if (!isPrimitive) {
				buildQuery
						.append('.')
						.append(pkFieldName);
			}
			buildQuery.append(IN_CHAR);
			separator = ",";
			//-----
			if (moreThanOneWhereIn && index % NB_MAX_WHERE_IN_ITEM == 0 && index != listObject.size()) {
				buildQuery
						.append(isNotIn ? ") AND " : ") OR ")
						.append(fkFieldName)
						.append(isNotIn ? " NOT IN (" : " IN (");
				separator = "";
			}
			//-----
			index++;
		}
		buildQuery.append(')');
	}
}
