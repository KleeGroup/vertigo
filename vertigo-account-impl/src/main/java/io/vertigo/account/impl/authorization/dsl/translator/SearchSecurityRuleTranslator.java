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
package io.vertigo.account.impl.authorization.dsl.translator;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.account.authorization.metamodel.SecurityDimension;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleExpression.ValueOperator;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleFixedValue;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression.BoolOperator;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleUserPropertyValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 *
 *
 * @author npiedeloup
 */
public final class SearchSecurityRuleTranslator extends AbstractSecurityRuleTranslator<SearchSecurityRuleTranslator> {

	private static final String ES_EXISTS_CRITERIA = "_exists_";
	private static final String DEFAULT_BOOL_SEP = " ";

	/**
	 * @return This security rule as search Query
	 */
	public String toSearchQuery() {
		return buildQueryString();
	}

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		String sep = "";
		for (final RuleMultiExpression multiExpressionDefinition : getMultiExpressions()) {
			query.append(sep).append('(');//On ajoute cette parenthèse car le premier niveau de multiExpression est en OR
			appendMultiExpression(query, multiExpressionDefinition);
			query.append(')');
			sep = DEFAULT_BOOL_SEP;
		}
		String queryString = cleanQuery(query.toString());
		queryString = EMPTY_QUERY_PATTERN.matcher(queryString).replaceAll("(*:*)");// replace empty query to all
		return queryString;
	}

	private void appendMultiExpression(final StringBuilder query, final RuleMultiExpression multiExpressionDefinition) {
		String sep = "";
		String boolSep;
		if (multiExpressionDefinition.getBoolOperator() == BoolOperator.AND) {
			boolSep = "+";
		} else {
			boolSep = "";
		}

		for (final RuleExpression expression : multiExpressionDefinition.getExpressions()) {
			query.append(sep);
			if (expression.getOperator() != ValueOperator.NEQ) {
				query.append(boolSep);
			}
			appendExpression(query, expression);
			sep = " ";
		}
		for (final RuleMultiExpression multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			query.append(sep).append(boolSep).append('(');
			appendMultiExpression(query, multiExpression);
			query.append(')');
			sep = " ";
		}
	}

	private void appendExpression(final StringBuilder query, final RuleExpression expressionDefinition) {
		if (expressionDefinition.getValue() instanceof RuleUserPropertyValue) {
			final RuleUserPropertyValue userPropertyValue = (RuleUserPropertyValue) expressionDefinition.getValue();
			final List<Serializable> userValues = getUserCriteria(userPropertyValue.getUserProperty());
			if (!userValues.isEmpty()) {
				final boolean useParenthesisAroundValue = userValues.size() > 1;
				String sep = "";
				for (final Serializable userValue : userValues) {
					Assertion.checkNotNull(userValue);
					Assertion.when(!userValue.getClass().isArray())
							.check(() -> userValue instanceof Comparable,
									"Security keys must be serializable AND comparable (here : {0})", userValues.getClass().getSimpleName());
					Assertion.when(userValue.getClass().isArray())
							.check(() -> Comparable.class.isAssignableFrom(userValue.getClass().getComponentType()),
									"Security keys must be serializable AND comparable (here : {0})", userValue.getClass().getComponentType());
					//----
					query.append(sep);
					appendExpression(query, expressionDefinition.getFieldName(), expressionDefinition.getOperator(), userValue);
					sep = " ";
				}
				query.append(useParenthesisAroundValue ? ")" : "");
			} else {
				//always false
				query.append("_exists_:always_false");
			}
			//always false
		} else if (expressionDefinition.getValue() instanceof RuleFixedValue) {
			appendExpression(query, expressionDefinition.getFieldName(), expressionDefinition.getOperator(), ((RuleFixedValue) expressionDefinition.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expressionDefinition.getValue().getClass().getName());
		}
	}

	private static void appendSimpleExpression(final StringBuilder query, final String fieldName, final ValueOperator operator, final Serializable userValue, final boolean strict) {
		if (userValue != null && !StringUtil.isEmpty(String.valueOf(userValue))) {
			if (operator == ValueOperator.NEQ) {
				query.append('-');
			}
			query.append(fieldName)
					.append(':')
					.append(toOperator(operator))
					.append(strict ? "'" : "")
					.append(userValue)
					.append(strict ? "'" : "");
		}
	}

	private static void appendSimpleExpression(final StringBuilder query, final String fieldName, final ValueOperator operator, final List<Serializable> userValues, final boolean strict) {
		final boolean useParenthesisAroundValue = userValues.size() > 1;
		if (!userValues.isEmpty()) {
			if (operator == ValueOperator.NEQ) {
				query.append('-');
			}
			query.append(fieldName)
					.append(':')
					.append(toOperator(operator));
			if (useParenthesisAroundValue) {
				query.append('(');
			}
			String inSep = "";
			for (final Serializable userValue : userValues) {
				query.append(inSep)
						.append(strict ? "'" : "")
						.append(userValue)
						.append(strict ? "'" : "");
				inSep = " ";
			}
			if (useParenthesisAroundValue) {
				query.append(')');
			}
		}
	}

	private void appendExpression(final StringBuilder query, final String fieldName, final ValueOperator operator, final Serializable value) {
		if (isSimpleSecurityField(fieldName)) {
			appendSimpleExpression(query, fieldName, operator, value, false);
		} else {
			final SecurityDimension securityDimension = getSecurityDimension(fieldName);
			switch (securityDimension.getType()) {
				case SIMPLE: //TODO not use yet ?
					appendSimpleExpression(query, fieldName, operator, value, false);
					break;
				case ENUM:
					Assertion.checkArgument(value instanceof String, "Enum criteria must be a code String ({0})", value);
					//----
					appendEnumExpression(query, securityDimension, operator, String.class.cast(value));
					break;
				case TREE:
					appendTreeExpression(query, securityDimension, operator, value);
					break;
				default:
					throw new IllegalArgumentException("securityDimensionType not supported " + securityDimension.getType());
			}
		}
	}

	private void appendEnumExpression(final StringBuilder query, final SecurityDimension securityDimension, final ValueOperator operator, final String value) {
		final String fieldName = securityDimension.getName();
		switch (operator) {
			case EQ:
			case NEQ:
				appendSimpleExpression(query, fieldName, operator, value, true);
				break;
			case GT:
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, subValues(securityDimension.getValues(), false, value, false), true);
				break;
			case GTE:
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, subValues(securityDimension.getValues(), false, value, true), true);
				break;
			case LT:
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, subValues(securityDimension.getValues(), true, value, false), true);
				break;
			case LTE:
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, subValues(securityDimension.getValues(), true, value, true), true);
				break;

			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

	private void appendTreeExpression(final StringBuilder query, final SecurityDimension securityDimension, final ValueOperator operator, final Serializable value) {
		Assertion.checkArgument(value instanceof String[]
				|| value instanceof Integer[]
				|| value instanceof Long[], "Security TREE axe ({0}) must be set in UserSession as Arrays (current:{1})", securityDimension.getName(), value.getClass().getName());
		if (value instanceof String[]) {
			appendTreeExpression(query, securityDimension, operator, (String[]) value);
		} else if (value instanceof Integer[]) {
			appendTreeExpression(query, securityDimension, operator, (Integer[]) value);
		} else {
			appendTreeExpression(query, securityDimension, operator, (Long[]) value);
		}
	}

	private <K extends Serializable> void appendTreeExpression(final StringBuilder query, final SecurityDimension securityDimension, final ValueOperator operator, final K[] treeKeys) {
		//on vérifie qu'on a bien toutes les clées.
		final List<String> strDimensionfields = securityDimension.getFields().stream()
				.map(DtField::getName)
				.collect(Collectors.toList());
		Assertion.checkArgument(strDimensionfields.size() == treeKeys.length, "User securityKey for tree axes must match declared fields: ({0})", strDimensionfields);
		query.append('(');

		//cas particuliers du == et du !=
		if (operator == ValueOperator.EQ) {
			String inSep = "";
			for (int i = 0; i < strDimensionfields.size(); i++) {
				if (treeKeys[i] != null) {
					query.append(inSep).append('+');
					appendSimpleExpression(query, strDimensionfields.get(i), ValueOperator.EQ, treeKeys[i], false);
					inSep = " ";
				} else {
					query.append(inSep);
					appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.NEQ, strDimensionfields.get(i), false);
					inSep = " ";
				}
			}
		} else if (operator == ValueOperator.NEQ) {
			String inSep = "";
			for (int i = 0; i < strDimensionfields.size(); i++) {
				if (treeKeys[i] != null) {
					query.append(inSep);
					appendSimpleExpression(query, strDimensionfields.get(i), ValueOperator.NEQ, treeKeys[i], false);
					inSep = " ";
				} else {
					query.append(inSep);
					appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.EQ, strDimensionfields.get(i), false);
					inSep = " ";
				}
			}
		} else { //cas des < , <= , > et >=
			//le < signifie au-dessus dans la hierachie et > en-dessous

			//on détermine le dernier field non null du user, les règles pivotent sur ce point là
			final int lastIndexNotNull = lastIndexNotNull(treeKeys);

			//1- règles avant le point de pivot : 'Eq' pout tous les opérateurs
			appendBeforePivotPoint(query, treeKeys, strDimensionfields, lastIndexNotNull);

			String inSep = lastIndexNotNull > 0 ? " " : "";
			//2- règles pour le point de pivot
			if (lastIndexNotNull >= 0) {
				query.append(inSep);
				final String fieldName = strDimensionfields.get(lastIndexNotNull);
				appendPivotPoint(query, operator, treeKeys[lastIndexNotNull], fieldName);
				inSep = " ";
			}

			//3- règles après le point de pivot (les null du user donc)
			for (int i = lastIndexNotNull + 1; i < strDimensionfields.size(); i++) {
				final String fieldName = strDimensionfields.get(i);
				appendAfterPivotPoint(query, operator, inSep, lastIndexNotNull, i, fieldName);
			}
		}
		query.append(')');
	}

	private <K extends Serializable> void appendBeforePivotPoint(final StringBuilder query, final K[] treeKeys, final List<String> strDimensionfields, final int lastIndexNotNull) {
		String inSep = "";
		for (int i = 0; i < lastIndexNotNull; i++) {
			query.append(inSep).append('+');
			appendSimpleExpression(query, strDimensionfields.get(i), ValueOperator.EQ, treeKeys[i], false);
			inSep = " ";
		}
	}

	private <K extends Serializable> void appendPivotPoint(final StringBuilder query, final ValueOperator operator, final K treeKey, final String fieldName) {
		switch (operator) {
			case GT:
				//pour > : doit être null (car non inclus)
				appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.NEQ, fieldName, false);
				break;
			case GTE:
				//pour >= : doit être égale à la clé du user ou null (supérieur)
				query.append("+(");
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, treeKey, false);
				//just append ' ' -> OR
				query.append(' ');
				appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.NEQ, fieldName, false);
				query.append(')');
				break;
			case LT:
			case LTE:
				//pour < et <= on test l'égalité
				query.append('+');
				appendSimpleExpression(query, fieldName, ValueOperator.EQ, treeKey, false);
				break;
			case EQ:
			case NEQ:
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

	private void appendAfterPivotPoint(final StringBuilder query, final ValueOperator operator, final String firstInSep, final int lastIndexNotNull, final int i, final String fieldName) {
		String inSep = firstInSep;
		switch (operator) {
			case GT:
			case GTE:
				//pour > et >= on test l'égalité (isNull donc)
				query.append(inSep);
				appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.NEQ, fieldName, false);
				inSep = " ";
				break;
			case LT:
				//pout < : le premier non null, puis pas de filtre : on accepte toutes valeurs
				if (i == lastIndexNotNull + 1) {
					query.append(inSep).append('+');
					appendSimpleExpression(query, ES_EXISTS_CRITERIA, ValueOperator.EQ, fieldName, false);
					inSep = " ";
				}
				break;
			case LTE:
				//pour <= : pas de filtre on accepte toutes valeurs
				break;
			case EQ:
			case NEQ:
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

	private static String toOperator(final ValueOperator operator) {
		switch (operator) {
			case GT:
				return ">";
			case GTE:
				return ">=";
			case LT:
				return "<";
			case LTE:
				return "<=";
			case EQ:
			case NEQ:
				return "";
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

}
