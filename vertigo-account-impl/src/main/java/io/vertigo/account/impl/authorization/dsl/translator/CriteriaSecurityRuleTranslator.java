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
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

/**
 * Translate a security rule into a criteria to be used in SQL queries and as a Java predicate.
 *
 * @param <E> The type of entity we are protecting
 * @author npiedeloup
 */
public final class CriteriaSecurityRuleTranslator<E extends Entity> extends AbstractSecurityRuleTranslator<CriteriaSecurityRuleTranslator<E>> {

	/**
	 * @return This security rule as search Query
	 */
	public Criteria<E> toCriteria() {
		Criteria<E> mainCriteria = null;
		for (final RuleMultiExpression expression : getMultiExpressions()) {
			mainCriteria = orCriteria(mainCriteria, toCriteria(expression));
		}
		Assertion.checkNotNull(mainCriteria);//can't be null
		return mainCriteria;
	}

	private Criteria<E> toCriteria(final RuleMultiExpression multiExpression) {
		if (multiExpression.isAlwaysTrue()) {
			return Criterions.alwaysTrue();
		}

		Criteria<E> mainCriteria = null;
		for (final RuleExpression expression : multiExpression.getExpressions()) {
			if (multiExpression.getBoolOperator() == BoolOperator.AND) {
				mainCriteria = andCriteria(mainCriteria, toCriteria(expression));
			} else {
				mainCriteria = orCriteria(mainCriteria, toCriteria(expression));
			}
		}
		for (final RuleMultiExpression expression : multiExpression.getMultiExpressions()) {
			if (multiExpression.getBoolOperator() == BoolOperator.AND) {
				mainCriteria = andCriteria(mainCriteria, toCriteria(expression));
			} else {
				mainCriteria = orCriteria(mainCriteria, toCriteria(expression));
			}
		}
		Assertion.checkNotNull(mainCriteria);//can be null ?
		return mainCriteria;
	}

	private Criteria<E> toCriteria(final RuleExpression expression) {
		if (expression.getValue() instanceof RuleUserPropertyValue) {
			final RuleUserPropertyValue userPropertyValue = (RuleUserPropertyValue) expression.getValue();
			final List<Serializable> userValues = getUserCriteria(userPropertyValue.getUserProperty());
			if (!userValues.isEmpty()) {
				Criteria<E> mainCriteria = null; //comment collecter en stream ?
				for (final Serializable userValue : userValues) {
					Assertion.checkNotNull(userValue);
					Assertion
							.when(!userValue.getClass().isArray())
							.check(() -> userValue instanceof Comparable,
									"Security keys must be serializable AND comparable (here : {0})", userValues.getClass().getSimpleName());
					Assertion
							.when(userValue.getClass().isArray())
							.check(() -> Comparable.class.isAssignableFrom(userValue.getClass().getComponentType()),
									"Security keys must be serializable AND comparable (here : {0})", userValue.getClass().getComponentType());
					//----
					mainCriteria = orCriteria(mainCriteria, toCriteria(expression.getFieldName(), expression.getOperator(), userValue));
				}
				Assertion.checkNotNull(mainCriteria);//can't be null
				return mainCriteria;
			}
			return Criterions.alwaysFalse();
		} else if (expression.getValue() instanceof RuleFixedValue) {
			return toCriteria(expression.getFieldName(), expression.getOperator(), ((RuleFixedValue) expression.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expression.getValue().getClass().getName());
		}
	}

	private Criteria<E> toCriteria(final String fieldName, final ValueOperator operator, final Serializable value) {
		if (isSimpleSecurityField(fieldName)) {
			//field normal
			return toCriteria(fieldName::toString, operator, value);
		}
		final SecurityDimension securityDimension = getSecurityDimension(fieldName);
		switch (securityDimension.getType()) {
			case SIMPLE: //TODO not use yet ?
				return toCriteria(fieldName::toString, operator, value);
			case ENUM:
				Assertion.checkArgument(value instanceof String, "Enum criteria must be a code String ({0})", value);
				//----
				return enumToCriteria(securityDimension, operator, String.class.cast(value));
			case TREE:
				return treeToCriteria(securityDimension, operator, value);
			default:
				throw new IllegalArgumentException("securityDimensionType not supported " + securityDimension.getType());
		}
	}

	private Criteria<E> toCriteria(final DtFieldName<E> fieldName, final ValueOperator operator, final Serializable value) {
		switch (operator) {
			case EQ:
				return Criterions.isEqualTo(fieldName, value);
			case GT:
				return Criterions.isGreaterThan(fieldName, value);
			case GTE:
				return Criterions.isGreaterThanOrEqualTo(fieldName, value);
			case LT:
				return Criterions.isLessThan(fieldName, value);
			case LTE:
				return Criterions.isLessThanOrEqualTo(fieldName, value);
			case NEQ:
				return Criterions.isNotEqualTo(fieldName, value);
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

	private Criteria<E> enumToCriteria(final SecurityDimension securityDimension, final ValueOperator operator, final String value) {
		final DtFieldName<E> fieldName = securityDimension::getName;
		switch (operator) {
			case EQ:
				return Criterions.isEqualTo(fieldName, value);
			case GT:
				return Criterions.in(fieldName, toStringArray(subValues(securityDimension.getValues(), false, value, false)));
			case GTE:
				return Criterions.in(fieldName, toStringArray(subValues(securityDimension.getValues(), false, value, true)));
			case LT:
				return Criterions.in(fieldName, toStringArray(subValues(securityDimension.getValues(), true, value, false)));
			case LTE:
				return Criterions.in(fieldName, toStringArray(subValues(securityDimension.getValues(), true, value, true)));
			case NEQ:
				return Criterions.isNotEqualTo(fieldName, value);
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

	private static Serializable[] toStringArray(final List<Serializable> subValues) {
		return subValues.toArray(new String[subValues.size()]);
	}

	private Criteria<E> treeToCriteria(final SecurityDimension securityDimension, final ValueOperator operator, final Serializable value) {
		Assertion.checkArgument(value instanceof String[]
				|| value instanceof Integer[]
				|| value instanceof Long[], "Security TREE axe ({0}) must be set in UserSession as Arrays (current:{1})", securityDimension.getName(), value.getClass().getName());
		if (value instanceof String[]) {
			return treeToCriteria(securityDimension, operator, (String[]) value);
		} else if (value instanceof Integer[]) {
			return treeToCriteria(securityDimension, operator, (Integer[]) value);
		} else {
			return treeToCriteria(securityDimension, operator, (Long[]) value);
		}
	}

	private <K extends Serializable> Criteria<E> treeToCriteria(final SecurityDimension securityDimension, final ValueOperator operator, final K[] treeKeys) {
		//on vérifie qu'on a bien toutes les clées.
		final List<String> strDimensionfields = securityDimension.getFields().stream()
				.map(DtField::getName)
				.collect(Collectors.toList());
		Assertion.checkArgument(strDimensionfields.size() == treeKeys.length, "User securityKey for tree axes must match declared fields: ({0})", strDimensionfields);
		Criteria<E> mainCriteria = null;

		//cas particuliers du == et du !=
		if (operator == ValueOperator.EQ) {
			for (int i = 0; i < strDimensionfields.size(); i++) {
				final DtFieldName<E> fieldName = strDimensionfields.get(i)::toString;
				mainCriteria = andCriteria(mainCriteria, Criterions.isEqualTo(fieldName, treeKeys[i]));
			}
		} else if (operator == ValueOperator.NEQ) {
			for (int i = 0; i < strDimensionfields.size(); i++) {
				final DtFieldName<E> fieldName = strDimensionfields.get(i)::toString;
				mainCriteria = andCriteria(mainCriteria, Criterions.isNotEqualTo(fieldName, treeKeys[i]));
			}
		} else { //cas des < , <= , > et >=
			//le < signifie au-dessus dans la hierachie et > en-dessous

			//on détermine le dernier field non null du user, les règles pivotent sur ce point là
			final int lastIndexNotNull = lastIndexNotNull(treeKeys);

			//1- règles avant le point de pivot : 'Eq' pout tous les opérateurs
			for (int i = 0; i < lastIndexNotNull; i++) {
				final DtFieldName<E> fieldName = strDimensionfields.get(i)::toString;
				mainCriteria = andCriteria(mainCriteria, Criterions.isEqualTo(fieldName, treeKeys[i]));
			}

			//2- règles pour le point de pivot
			if (lastIndexNotNull >= 0) {
				final DtFieldName<E> fieldName = strDimensionfields.get(lastIndexNotNull)::toString;
				switch (operator) {
					case GT:
						//pour > : doit être null (car non inclus)
						mainCriteria = andCriteria(mainCriteria, Criterions.isNull(fieldName));
						break;
					case GTE:
						//pour >= : doit être égale à la clé du user ou null (supérieur)
						final Criteria<E> equalsCriteria = Criterions.isEqualTo(fieldName, treeKeys[lastIndexNotNull]);
						final Criteria<E> greaterCriteria = Criterions.isNull(fieldName);
						final Criteria<E> gteCriteria = greaterCriteria.or(equalsCriteria);
						mainCriteria = andCriteria(mainCriteria, gteCriteria);
						break;
					case LT:
					case LTE:
						//pour < et <= on test l'égalité
						mainCriteria = andCriteria(mainCriteria, Criterions.isEqualTo(fieldName, treeKeys[lastIndexNotNull]));
						break;
					case EQ:
					case NEQ:
					default:
						throw new IllegalArgumentException("Operator not supported " + operator.name());
				}
			}

			//3- règles après le point de pivot (les null du user donc)
			for (int i = lastIndexNotNull + 1; i < strDimensionfields.size(); i++) {
				final DtFieldName<E> fieldName = strDimensionfields.get(i)::toString;
				switch (operator) {
					case GT:
					case GTE:
						//pour > et >= on test l'égalité (isNull donc)
						mainCriteria = andCriteria(mainCriteria, Criterions.isNull(fieldName));
						break;
					case LT:
						//pout < : le premier non null, puis pas de filtre : on accepte toutes valeurs
						if (i == lastIndexNotNull + 1) {
							mainCriteria = andCriteria(mainCriteria, Criterions.isNotNull(fieldName));
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
		}
		Assertion.checkNotNull(mainCriteria);//can be null ?
		return mainCriteria;
	}

	private Criteria<E> andCriteria(final Criteria<E> oldCriteria, final Criteria<E> newCriteria) {
		if (oldCriteria == null) {
			return newCriteria;
		}
		return oldCriteria.and(newCriteria);
	}

	private Criteria<E> orCriteria(final Criteria<E> oldCriteria, final Criteria<E> newCriteria) {
		if (oldCriteria == null) {
			return newCriteria;
		}
		return oldCriteria.or(newCriteria);
	}
}
