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
package io.vertigo.dynamo.criteria;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

/**
 *
 * This class provides criterions (aka where clause) for a field of an entity.
 *
 * @author pchretien
 *
 */
public final class Criterions {
	private Criterions() {
		//stateless
	}

	/**
	 * @param dtFieldName the field
	 * @return is null
	 */
	public static <E extends Entity> Criteria<E> isNull(final DtFieldName<E> dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not null
	 */
	public static <E extends Entity> Criteria<E> isNotNull(final DtFieldName<E> dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NOT_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isEqualTo(final DtFieldName<E> dtFieldName, final Serializable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.EQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isNotEqualTo(final DtFieldName<E> dtFieldName, final Serializable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.NEQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isGreaterThan(final DtFieldName<E> dtFieldName, final Serializable value) {
		Assertion.checkArgument(value == null || value instanceof Comparable, "value must be comparable");
		//---
		return new Criterion<>(dtFieldName, CriterionOperator.GT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than or equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isGreaterThanOrEqualTo(final DtFieldName<E> dtFieldName, final Serializable value) {
		Assertion.checkArgument(value == null || value instanceof Comparable, "value must be comparable");
		//---
		return new Criterion<>(dtFieldName, CriterionOperator.GTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isLessThan(final DtFieldName<E> dtFieldName, final Serializable value) {
		Assertion.checkArgument(value == null || value instanceof Comparable, "value must be comparable");
		//---
		return new Criterion<>(dtFieldName, CriterionOperator.LT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than or equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isLessThanOrEqualTo(final DtFieldName<E> dtFieldName, final Serializable value) {
		Assertion.checkArgument(value == null || value instanceof Comparable, "value must be comparable");
		//---
		return new Criterion<>(dtFieldName, CriterionOperator.LTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return starts with the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> startsWith(final DtFieldName<E> dtFieldName, final String value) {
		return new Criterion<>(dtFieldName, CriterionOperator.STARTS_WITH, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is between min and max
	 * @param min the min value
	 * @param max the max value
	 */
	public static <E extends Entity> Criteria<E> isBetween(final DtFieldName<E> dtFieldName, final CriterionLimit<E> min, final CriterionLimit<E> max) {
		Assertion.checkNotNull(min);
		Assertion.checkNotNull(max);
		return new Criterion<>(dtFieldName, CriterionOperator.BETWEEN, min, max);
	}

	/**
	 * @param dtFieldName the field
	 * @return is in a list of values
	 * @param values list of allowed values
	 */
	public static <E extends Entity> Criteria<E> in(final DtFieldName<E> dtFieldName, final Serializable... values) {
		return new Criterion<>(dtFieldName, CriterionOperator.IN, values);
	}

	/**
	 * An always true criteria.
	 * @return true
	 */
	public static <E extends Entity> Criteria<E> alwaysTrue() {
		return AlwaysCriteria.ALWAYS_TRUE;
	}

	/**
	 * An always false criteria.
	 * @return true
	 */
	public static <E extends Entity> Criteria<E> alwaysFalse() {
		return AlwaysCriteria.ALWAYS_FALSE;
	}

	private static class AlwaysCriteria<E extends Entity> extends Criteria<E> {
		private static final long serialVersionUID = 2967018427662007659L;
		private static final Criteria ALWAYS_TRUE = new AlwaysCriteria<>(true);
		private static final Criteria ALWAYS_FALSE = new AlwaysCriteria<>(false);
		private final boolean result;

		private AlwaysCriteria(final boolean result) {
			this.result = result;
		}

		@Override
		public Predicate<E> toPredicate() {
			return entity -> result;
		}

		@Override
		String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect) {
			return result ? "1=1" : "0=1";
		}

		@Override
		public String toString() {
			return result ? "true" : "false";
		}
	}
}
