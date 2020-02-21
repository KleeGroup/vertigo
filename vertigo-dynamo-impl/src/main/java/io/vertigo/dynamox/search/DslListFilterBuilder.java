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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamox.search.dsl.model.DslBlockQuery;
import io.vertigo.dynamox.search.dsl.model.DslExpression;
import io.vertigo.dynamox.search.dsl.model.DslField;
import io.vertigo.dynamox.search.dsl.model.DslFixedQuery;
import io.vertigo.dynamox.search.dsl.model.DslMultiExpression;
import io.vertigo.dynamox.search.dsl.model.DslMultiField;
import io.vertigo.dynamox.search.dsl.model.DslQuery;
import io.vertigo.dynamox.search.dsl.model.DslRangeQuery;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery.EscapeMode;
import io.vertigo.dynamox.search.dsl.model.DslUserCriteria;
import io.vertigo.dynamox.search.dsl.rules.DslParserUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.BeanUtil;
import io.vertigo.util.StringUtil;

/**
 * Default builder from Criteria to ListFilter with a query pattern with DSL.
 * Pattern syntax is easy :
 * #QUERY# : criteria.toString() : use this when Criteria is a user string
 * #MY_FIELD# : criteria.myField
 * #MY_FIELD#!(myDefault) : criteria.myField!=null?criteria.myField:myDefault
 * QueryString modifier must be add into the ## and will be repeated for all word (separated by regexp \p{White_Space})
 *
 * example:
 *  "" // all result
 *  #QUERY# //directly use user's query as is
 *  code:"#code#"  //CODE equals strictly
 *  comment:#comment*#  //COMMENT contains words prefixed with criteria's comment words (all words)
 *  comment:#+comment*#  //COMMENT MUST contains all words prefixed with criteria's comment words (all words)
 *  year:[#yearMin# TO #yearMax#] //YEAR between crieteria's year_min and year_max
 *  +(addr1:#address# addr2:#address#) //criteria ADDRESS field should be in ADDR1 or ADDR2 index's fields
 *  For more info, look for ElasctiSearch QueryString Syntax
 *  @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax"
 *
 * If a criteria field contains OR / AND it will be use as logical operator.
 * If a criteria field contains XXX:yyyy it will be use as a specific field query and will not be transformed
 *
 * @author npiedeloup
 * @param <C> Criteria type
 */
public final class DslListFilterBuilder<C> implements ListFilterBuilder<C> {

	private static final String USER_QUERY_KEYWORD = "query";

	private static final Set<String> RESERVED_QUERY_KEYWORDS = new HashSet<>(Arrays.asList(new String[] { "AND", "OR", "*" }));
	private static final String QUERY_RESERVERD_PATTERN_STR = "(?i)([\\+\\-\\=\\&\\|\\>\\<\\!\\(\\)\\{\\}\\[\\]\\^\"\\~\\*\\?\\:\\\\\\/])|((?<=\\s|^)(OR|AND)(?=\\s|$))";
	private static final Pattern QUERY_RESERVERD_PATTERN = Pattern.compile(QUERY_RESERVERD_PATTERN_STR);
	private static final String QUERY_INCOMPLETE_GRAMMAR_PATTERN_STR = "(?i)"
			+ buildIncompletePatternStr('(', ')', 250) //cherche les ( mal fermées et les ) mal ouvertes (1 seul niveau d'imbrication maximum)
			+ "|" + buildIncompletePatternStr('{', '}', 250)
			+ "|" + buildIncompletePatternStr('[', ']', 250)
			+ "|(?<![\\w\\)\\}\\]]\\s{0,250})(?<=[^\\w\\\\\\)\\}\\]]|^)(OR|AND)(?=\\W|$)|(?<=[^\\\\\\w]|^)(OR|AND)(?=[^\\w\\(\\{\\[]|$)(?!\\s*[\\w\\(\\{\\[])";
	private static final Pattern QUERY_INCOMPLETE_GRAMMAR_PATTERN = Pattern.compile(QUERY_INCOMPLETE_GRAMMAR_PATTERN_STR);

	private static final Pattern MAY_USE_BLOCK_1_PATTERN = Pattern.compile("((\\(.*\\))|([\\[\\{].*[\\]\\}])|(\\\".*\\\")|\\*)(\\^[0-9]+)?");
	private static final Pattern MAY_USE_BLOCK_2_PATTERN = Pattern.compile("(?i)([\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<\\s]|OR|AND)");

	private static final Pattern BEGIN_LINE_TRIM_PATTERN = Pattern.compile("^\\s+");

	private List<DslMultiExpression> myBuildQuery;
	private C myCriteria;

	private static String buildIncompletePatternStr(final char openChar, final char closeChar, final int maxLookBehind) {
		return "(\\" + openChar + ")(?![^\\" + openChar + "]*\\" + closeChar + "|[^\\" + openChar + "]*\\" + openChar + "[^\\" + openChar + "]*\\" + closeChar + "[^\\" + openChar + "]*\\" + closeChar + ")|(?<!\\" + openChar + "[^\\" + closeChar + "]{0," + maxLookBehind + "}|\\" + openChar + "[^\\" + closeChar + "]{0," + maxLookBehind + "}\\" + openChar + "[^\\" + closeChar + "]{0," + maxLookBehind + "}\\" + closeChar + "[^\\" + closeChar + "]{0," + maxLookBehind + "})(\\" + closeChar + ")";
	}

	/**
	 * Fix query pattern.
	 * @param buildQuery Pattern (not null, could be empty)
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withBuildQuery(final String buildQuery) {
		Assertion.checkNotNull(buildQuery);
		Assertion.checkState(myBuildQuery == null, "query was already set : {0}", myBuildQuery);
		//-----
		try {
			myBuildQuery = DslParserUtil.parseMultiExpression(buildQuery);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture du listFilterPattern {0}\n{1}", buildQuery, e.getFullMessage());
			throw WrappedException.wrap(e, message);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture du listFilterPattern {0}\n{1}", buildQuery, e.getMessage());
			throw WrappedException.wrap(e, message);
		}
		return this;
	}

	/**
	 * Fix criteria.
	 * @param criteria Criteria
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withCriteria(final C criteria) {
		Assertion.checkNotNull(criteria);
		Assertion.checkState(myCriteria == null, "criteria was already set : {0}", myCriteria);
		//-----
		this.myCriteria = criteria;
		return this;

	}

	/** {@inheritDoc} */
	@Override
	public ListFilter build() {
		final String query = buildQueryString();
		return ListFilter.of(query);
	}

	private static final Pattern BEGINNING_LINE_PATTERN = Pattern.compile("^\\s+");
	private static final Pattern END_LINE_PATTERN = Pattern.compile("\\s+$");
	private static final Pattern MULTIPLE_WHITESPACE_PATTERN = Pattern.compile("\\s+");

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		for (final DslMultiExpression multiExpressionDefinition : myBuildQuery) {
			appendMultiExpression(query, multiExpressionDefinition);
		}
		return cleanQuery(query.toString());
	}

	protected static String cleanQuery(final String query) {
		String queryString = BEGINNING_LINE_PATTERN.matcher(query).replaceAll("");//replace whitespaces at beginning of a line
		queryString = END_LINE_PATTERN.matcher(queryString).replaceAll("");//replace whitespaces at end of a line
		queryString = MULTIPLE_WHITESPACE_PATTERN.matcher(queryString).replaceAll(" ");// replace multiple whitespaces by space
		return queryString;
	}

	private void appendMultiExpression(final StringBuilder query, final DslMultiExpression multiExpressionDefinition) {
		final StringBuilder multiExpressionQuery = new StringBuilder();
		for (final DslExpression expression : multiExpressionDefinition.getExpressions()) {
			appendExpression(multiExpressionQuery, expression);
		}
		for (final DslMultiExpression multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			appendMultiExpression(multiExpressionQuery, multiExpression);
		}
		flushSubQueryToQuery(query, multiExpressionDefinition.getPreBody(), multiExpressionDefinition.getPostBody(), multiExpressionDefinition.isBlock(), multiExpressionQuery);
	}

	private void appendExpression(final StringBuilder query, final DslExpression expressionDefinition) {
		final StringBuilder expressionQuery = new StringBuilder();
		final DslQuery dslQuery = expressionDefinition.getQuery();
		appendQuery(query, expressionDefinition, expressionQuery, dslQuery);
		flushExpressionToQuery(query, expressionDefinition, expressionQuery);
	}

	private static void flushExpressionToQuery(final StringBuilder query, final DslExpression expressionDefinition, final StringBuilder expressionQuery) {
		if (expressionQuery.length() > 0) {
			final String[] trimedExpression = splitTrimedSubQueryToQuery(expressionQuery.toString());
			query.append(trimedExpression[0]);
			query.append(expressionDefinition.getPreBody());
			if (expressionDefinition.getField().isPresent()) {
				appendField(query, expressionDefinition.getField().get());
			}
			final boolean useBlock = mayUseBlock(trimedExpression[1]);

			query.append(useBlock ? "(" : "")
					.append(trimedExpression[1])
					.append(useBlock ? ")" : "")
					.append(expressionDefinition.getPostBody());
			expressionQuery.setLength(0);
		}
	}

	private static boolean mayUseBlock(final String trimedExpression) {
		//on place des parenthèses s'il n'y a pas encore de block, ou des caractères interdits
		return !MAY_USE_BLOCK_1_PATTERN.matcher(trimedExpression).matches()//not : (...) or [...] or "..." but may finished by ^2
				&& MAY_USE_BLOCK_2_PATTERN.matcher(trimedExpression).find();//contains any reserved char +-!*?~^=>< or any spaces

	}

	private static void flushSubQueryToQuery(final StringBuilder query, final String preExpression, final String postExpression, final boolean useBlock, final StringBuilder subQuery) {
		if (subQuery.length() > 0) {
			final String[] trimedQuery = splitTrimedSubQueryToQuery(subQuery.toString());
			final boolean isAlreadyBlock = preExpression.endsWith("\"") && postExpression.startsWith("\"")
					|| preExpression.endsWith("(") && postExpression.startsWith(")");
			query.append(trimedQuery[0]) //[0] contient les caractères du trim : on les place avant
					.append(preExpression)
					.append(!isAlreadyBlock && useBlock ? "(" : "")
					.append(trimedQuery[1])
					.append(!isAlreadyBlock && useBlock ? ")" : "")
					.append(postExpression);
			subQuery.setLength(0);
		}
	}

	private static String[] splitTrimedSubQueryToQuery(final String subQueryStr) {
		final String[] result = new String[2];
		if (!subQueryStr.isEmpty()) {
			final String trimSubQueryStr = BEGIN_LINE_TRIM_PATTERN.matcher(subQueryStr).replaceFirst("");
			final String preTrimSubQueryStr = subQueryStr.substring(0, subQueryStr.length() - trimSubQueryStr.length());
			result[0] = preTrimSubQueryStr;
			result[1] = trimSubQueryStr;
		}
		return result;
	}

	private static void appendField(final StringBuilder query, final DslField dslField) {
		query.append(dslField.getPreBody())
				.append(dslField.getFieldName())
				.append(dslField.getPostBody())
				.append(':');
	}

	private void appendMultiQuery(final StringBuilder query, final DslBlockQuery dslMultiQueryDefinition, final DslExpression expressionDefinition, final StringBuilder parentQuery) {
		final StringBuilder expressionMultiQuery = new StringBuilder();
		for (final DslQuery dslQuery : dslMultiQueryDefinition.getQueries()) {
			appendQuery(parentQuery, expressionDefinition, expressionMultiQuery, dslQuery);
		}
		flushSubQueryToQuery(query, dslMultiQueryDefinition.getPreBody(), dslMultiQueryDefinition.getPostBody(), true, expressionMultiQuery);
	}

	private void appendQuery(final StringBuilder query, final DslExpression expressionDefinition, final StringBuilder expressionQuery, final DslQuery dslQuery) {
		if (dslQuery instanceof DslTermQuery) {
			if (expressionDefinition.getMultiField().isPresent() && ((DslTermQuery) dslQuery).getPreTerm().isEmpty()) {
				//recherche compact => on boucle les fields puis les user terms
				appendCompactFields(query, expressionDefinition, expressionQuery, dslQuery);
			} else {
				//recherche multifield => on boucle les users terms puis les fields
				appendTermQuery(expressionQuery, (DslTermQuery) dslQuery, expressionDefinition, query);
			}
			if (expressionDefinition.getMultiField().isPresent()) {
				//si multiFields on a déjà appliqué le field: , donc on flush a ce niveau
				final boolean useBlock = !(expressionDefinition.getPreBody().isEmpty() && expressionDefinition.getPostBody().isEmpty())
						&& !(expressionQuery.toString().startsWith("(") && expressionQuery.toString().endsWith(")"));
				flushSubQueryToQuery(query, expressionDefinition.getPreBody(), expressionDefinition.getPostBody(), useBlock, expressionQuery);
			}
		} else if (dslQuery instanceof DslBlockQuery) {
			appendMultiQuery(expressionQuery, (DslBlockQuery) dslQuery, expressionDefinition, query);
		} else if (dslQuery instanceof DslRangeQuery) {
			appendRangeQuery(expressionQuery, (DslRangeQuery) dslQuery, expressionDefinition);
		} else if (dslQuery instanceof DslFixedQuery) {
			appendFixedQuery(expressionQuery, (DslFixedQuery) dslQuery);
		} else {
			throw new VSystemException("dslQuery of type '{0}' is not supported is query", dslQuery.getClass());
		}
	}

	private void appendCompactFields(final StringBuilder query, final DslExpression expressionDefinition, final StringBuilder expressionQuery, final DslQuery dslQuery) {
		String expressionSep = "";
		final DslMultiField dslMultiField = expressionDefinition.getMultiField().get();
		for (final DslField dslField : dslMultiField.getFields()) {
			final DslField monoFieldDefinition = new DslField(
					firstNotEmpty(dslField.getPreBody(), dslMultiField.getPreBody()),
					dslField.getFieldName(),
					firstNotEmpty(dslField.getPostBody(), dslMultiField.getPostBody()));
			final DslExpression monoFieldExpressionDefinition = new DslExpression(
					concat(expressionSep, expressionDefinition.getPreBody()),
					Optional.of(monoFieldDefinition), Optional.empty(),
					dslQuery,
					expressionDefinition.getPostBody());
			appendTermQuery(expressionQuery, (DslTermQuery) dslQuery, monoFieldExpressionDefinition, query);
			flushExpressionToQuery(query, monoFieldExpressionDefinition, expressionQuery);
			expressionSep = " ";
		}
	}

	private void appendTermQuery(final StringBuilder query, final DslTermQuery dslQuery, final DslExpression expressionDefinition, final StringBuilder outExpressionQuery) {
		final String fieldName = dslQuery.getTermField();
		final Object value;
		if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
			value = cleanUserCriteria(myCriteria.toString(), dslQuery.getEscapeMode());
		} else {
			value = cleanUserCriteria(BeanUtil.getValue(myCriteria, fieldName), dslQuery.getEscapeMode());
		}
		appendTermQueryWithValue(value, query, dslQuery, expressionDefinition, outExpressionQuery);
	}

	private static <O> O cleanUserCriteria(final O value, final EscapeMode escapeMode) {
		if (value instanceof String) {
			if (((String) value).trim().isEmpty()) { //so not null too
				return (O) "*";
			} else if (escapeMode == EscapeMode.escape) {
				return (O) QUERY_RESERVERD_PATTERN.matcher((String) value).replaceAll("\\\\$0");
			} else if (escapeMode == EscapeMode.remove) {
				return (O) QUERY_RESERVERD_PATTERN.matcher((String) value).replaceAll(""); //par on retire le deuxième espace
			}
			//replace standard invalid syntax to escape this one
			return (O) QUERY_INCOMPLETE_GRAMMAR_PATTERN.matcher((String) value).replaceAll("\\\\$0");
		}
		return value;
	}

	private void appendTermQueryWithValue(
			final Object value,
			final StringBuilder query,
			final DslTermQuery dslQuery,
			final DslExpression expressionDefinition,
			final StringBuilder outExpressionQuery) {
		final boolean useBlock;
		final StringBuilder queryPart = new StringBuilder();
		if (value instanceof String) { //so not null too
			useBlock = appendUserStringCriteria(queryPart, dslQuery, expressionDefinition, (String) value, outExpressionQuery);
		} else if (value instanceof Instant) { //so not null too
			useBlock = appendSimpleCriteria(queryPart, dslQuery, formatInstant((Instant) value));
		} else if (value instanceof LocalDate) { //so not null too
			useBlock = appendSimpleCriteria(queryPart, dslQuery, formatDate((LocalDate) value));
		} else if (value != null) {
			useBlock = appendSimpleCriteria(queryPart, dslQuery, value.toString());
		} else if (dslQuery.getDefaultValue().isPresent()) { //if value null => defaultValue
			useBlock = appendSimpleCriteria(queryPart, dslQuery, dslQuery.getDefaultValue().get());
		} else {
			useBlock = false;
		}
		flushSubQueryToQuery(query, dslQuery.getPreBody(), dslQuery.getPostBody(), useBlock, queryPart);
		//if defaultValue null => no criteria
	}

	private void appendRangeQuery(final StringBuilder query, final DslRangeQuery dslQuery, final DslExpression expressionDefinition) {
		final DslQuery startQueryDefinition = dslQuery.getStartQueryDefinitions();
		final DslQuery endQueryDefinition = dslQuery.getEndQueryDefinitions();
		final StringBuilder startRangeQuery = new StringBuilder();
		if (startQueryDefinition instanceof DslTermQuery) {
			appendTermQuery(startRangeQuery, (DslTermQuery) startQueryDefinition, expressionDefinition, null); //null because, can't use upper output
		} else if (startQueryDefinition instanceof DslFixedQuery) {
			appendFixedQuery(startRangeQuery, (DslFixedQuery) startQueryDefinition);
		} else {
			throw new IllegalArgumentException("can't parse query \\\"\"+startQueryDefinition+\"\\\" of type " + startQueryDefinition.getClass().getSimpleName() + " (expected DslTermQuery or DslFixedQuery)");
		}
		final StringBuilder endRangeQuery = new StringBuilder();
		if (endQueryDefinition instanceof DslTermQuery) {
			appendTermQuery(endRangeQuery, (DslTermQuery) endQueryDefinition, expressionDefinition, null); //null because, can't use upper output
		} else if (endQueryDefinition instanceof DslFixedQuery) {
			appendFixedQuery(endRangeQuery, (DslFixedQuery) endQueryDefinition);
		} else {
			throw new IllegalArgumentException("can't parse query \"" + endQueryDefinition + "\" of type " + endQueryDefinition.getClass().getSimpleName() + " (expected DslTermQuery or DslFixedQuery)");
		}

		//flush Range Query
		final String startRangeStr = startRangeQuery.length() > 0 ? startRangeQuery.toString() : "*";
		final String endRangeStr = endRangeQuery.length() > 0 ? endRangeQuery.toString() : "*";

		if (!"*".equals(startRangeStr) || !"*".equals(endRangeStr)) {
			query.append(dslQuery.getPreBody())
					.append(dslQuery.getStartRange())
					.append(startRangeStr)
					.append(" TO ") //toUpperCase car ES n'interprete pas correctement en lowercase
					.append(endRangeStr)
					.append(dslQuery.getEndRange())
					.append(dslQuery.getPostBody());
		}
	}

	private static void appendFixedQuery(final StringBuilder query, final DslFixedQuery dslQuery) {
		query.append(dslQuery.getFixedQuery());
	}

	private static boolean appendSimpleCriteria(final StringBuilder query, final DslTermQuery dslTermDefinition, final String value) {
		query.append(dslTermDefinition.getPreTerm())
				.append(value)
				.append(dslTermDefinition.getPostTerm());
		return false; //never use block
	}

	private boolean appendUserStringCriteria(
			final StringBuilder query,
			final DslTermQuery dslTermDefinition,
			final DslExpression expressionDefinition,
			final String userString,
			final StringBuilder outExpressionQuery) {
		final List<DslUserCriteria> userCriteriaList = DslParserUtil.parseUserCriteria(userString);

		int criteriaOnDefinitionField = 0; //On compte les fields sur le field de la definition. Si >1 on mettra des ( )
		for (final DslUserCriteria userCriteria : userCriteriaList) {
			final String criteriaValue = userCriteria.getCriteriaWord();
			if (!userCriteria.getOverridedFieldName().isEmpty()) {
				//si le field est surchargé on flush l'expression précédente
				flushExpressionToQuery(outExpressionQuery, expressionDefinition, query);
				criteriaOnDefinitionField = 0;
				//et on ajout la requete sur l'autre champs
				outExpressionQuery.append(userCriteria.getPreMissingPart())
						.append(userCriteria.getOverridedFieldName())
						.append(userCriteria.getOverridedPreModifier())
						.append(criteriaValue)
						.append(userCriteria.getOverridedPostModifier())
						.append(expressionDefinition.getPostBody())
						.append(userCriteria.getPostMissingPart());

			} else if (expressionDefinition.getMultiField().isPresent()) {
				criteriaOnDefinitionField++;
				final DslMultiField dslMultiField = expressionDefinition.getMultiField().get();
				query.append(userCriteria.getPreMissingPart());
				final List<DslExpression> monoFieldExpressionDefinitions = flattenMultiToMonoFieldExpressionDefinition(dslTermDefinition, userCriteria, criteriaValue, dslMultiField);
				final DslMultiExpression monoFieldMultiExpressionDefinition = new DslMultiExpression(
						firstNotEmpty(userCriteria.getOverridedPreModifier(), dslTermDefinition.getPreTerm()), true,
						monoFieldExpressionDefinitions, Collections.emptyList(),
						"");

				appendMultiExpression(query, monoFieldMultiExpressionDefinition);
				query.append(userCriteria.getPostMissingPart());
			} else {
				criteriaOnDefinitionField++;
				query.append(userCriteria.getPreMissingPart());
				if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
					query.append(criteriaValue.toUpperCase(Locale.ROOT)); //toUpperCase car ES n'interprete pas correctement en lowercase
				} else {
					appendStandardCriteriaValue(query, dslTermDefinition, userCriteria, criteriaValue);
				}
				query.append(userCriteria.getPostMissingPart());
			}
		}
		return criteriaOnDefinitionField > 1; //useBlock if more than 1 criteria
	}

	private static void appendStandardCriteriaValue(final StringBuilder query, final DslTermQuery dslTermDefinition, final DslUserCriteria userCriteria, final String criteriaValue) {
		query.append(userCriteria.getOverridedPreModifier().isEmpty() ? dslTermDefinition.getPreTerm() : userCriteria.getOverridedPreModifier())
				.append(criteriaValue)
				.append(userCriteria.getOverridedPostModifier().isEmpty() ? dslTermDefinition.getPostTerm() : userCriteria.getOverridedPostModifier());
	}

	private static List<DslExpression> flattenMultiToMonoFieldExpressionDefinition(
			final DslTermQuery dslTermDefinition,
			final DslUserCriteria userCriteria,
			final String criteriaValue,
			final DslMultiField dslMultiField) {
		final List<DslExpression> monoFieldExpressionDefinitions = new ArrayList<>();
		for (final DslField dslField : dslMultiField.getFields()) {
			final DslField monoFieldDefinition = new DslField(
					firstNotEmpty(dslField.getPreBody(), dslMultiField.getPreBody()),
					dslField.getFieldName(),
					"");
			final DslExpression monoFieldExpressionDefinition = new DslExpression(
					monoFieldExpressionDefinitions.isEmpty() ? "" : " ",
					Optional.of(monoFieldDefinition), Optional.empty(),
					new DslFixedQuery(concat(criteriaValue, firstNotEmpty(userCriteria.getOverridedPostModifier(), dslTermDefinition.getPostTerm()))),
					firstNotEmpty(dslField.getPostBody(), dslMultiField.getPostBody()));
			monoFieldExpressionDefinitions.add(monoFieldExpressionDefinition);
		}
		return monoFieldExpressionDefinitions;
	}

	private static String firstNotEmpty(final String... elements) {
		return Arrays.stream(elements)
				.filter(element -> !element.isEmpty())
				.findFirst().orElse("");
	}

	/**
	 * Concat string elements.
	 * @param elements Nullable elements
	 * @return Concat string
	 */
	private static String concat(final String... elements) {
		return Arrays.stream(elements)
				.collect(Collectors.joining());
	}

	/**
	 * Retourne la date UTC en string.
	 *
	 * @param date la date.
	 * @return la chaine de caractere formattée.
	 */
	private static String formatDate(final LocalDate date) {
		return new StringBuilder("\"").append(date.toString()).append("\"").toString();
	}

	/**
	 * Retourne l'Instant UTC en string.
	 *
	 * @param instant l'Instant.
	 * @return la chaine de caractere formattée.
	 */
	private static String formatInstant(final Instant instant) {
		return new StringBuilder("\"").append(instant.toString()).append("\"").toString();
	}
}
