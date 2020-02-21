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
import java.util.List;
import java.util.function.Predicate;

import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuple;

/**
 * A criteria to filter a list.
 * A criteria can be translated to an SQL query and a Predicate for Java filtering
 * To create a Criteria use the class {@link Criterions}
 * @param <E> the type of entity filtered with this criteria
 * @author mlaroche
 */
public abstract class Criteria<E extends Entity> implements Serializable {
	private static final long serialVersionUID = -990254492823334724L;

	/**
	 * Return a new criteria composing the previous criteria and the provided one with a and operator.
	 * @param criteria the criteria to add
	 * @return the composed criteria
	 */
	public final Criteria<E> and(final Criteria<E> criteria) {
		return CriteriaUtil.and(this, criteria);
	}

	/**
	 * Return a new criteria composing the previous criteria and the provided one with a or operator.
	 * @param criteria the criteria to add
	 * @return the composed criteria
	 */
	public final Criteria<E> or(final Criteria<E> criteria) {
		return CriteriaUtil.or(this, criteria);
	}

	/**
	 * Translate the criteria to a Java predicate
	 * @return the predicate
	 */
	public abstract Predicate<E> toPredicate();

	abstract String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect);

	/**
	 * Translate the criteria to a SQL statement
	 * @param sqlDialect the dialect to use
	 * @return a tuple with the query and the sql params
	 */
	public Tuple<String, CriteriaCtx> toSql(final SqlDialect sqlDialect) {
		Assertion.checkNotNull(sqlDialect);
		//---
		final CriteriaCtx ctx = new CriteriaCtx();
		final String sql = this.toSql(ctx, sqlDialect);
		return Tuple.of(sql, ctx);

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toSql(new SqlDialect() {
			@Override
			public void appendListState(final StringBuilder query, final Integer maxRows, final int skipRows, final String sortFieldName, final boolean sortDesc) {
				//rien
			}

			@Override
			public String createInsertQuery(final String idFieldName, final List<String> dataFieldsName, final String sequencePrefix, final String tableName) {
				return null;
			}

			@Override
			public GenerationMode getGenerationMode() {
				return null;
			}
		}).getVal1();
	}
}
