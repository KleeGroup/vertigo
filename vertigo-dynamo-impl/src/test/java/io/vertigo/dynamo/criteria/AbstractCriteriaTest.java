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

import static io.vertigo.dynamo.criteria.CriterionLimit.ofExcluded;
import static io.vertigo.dynamo.criteria.CriterionLimit.ofIncluded;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.dynamo.criteria.data.movies.Movie2;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;

public abstract class AbstractCriteriaTest extends AbstractTestCaseJU5 {
	private static final DtFieldName<Movie2> year = () -> "year";
	private static final DtFieldName<Movie2> title = () -> "title";

	abstract public void assertCriteria(final long expected, final Criteria<Movie2> criteriaBool);

	@Test
	public final void testIsEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984);
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testIsEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, null);
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.isEqualTo(title, "1984"));
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isEqualTo(year, 2014));
		assertCriteria(5, criteriaBool);
	}

	@Test
	public final void testAndOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.startsWith(title, "a"))
				.or(Criterions.isEqualTo(year, 2014));
		assertCriteria(3 - 2 + 2, criteriaBool);
	}

	@Test
	public final void testOrAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isGreaterThanOrEqualTo(year, 2000))
				.and(Criterions.startsWith(title, "m"));
		assertCriteria(3 + 3 - 2, criteriaBool);
	}

	@Test
	public final void testAndOrAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.startsWith(title, "a"))
				.or(
						Criterions.isGreaterThan(year, 2000)
								.and(Criterions.isEqualTo(title, "mommy")));
		assertCriteria(1 + 1, criteriaBool);
	}

	@Test
	public final void testOrAndOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isGreaterThan(year, 2000)
						.and(Criterions.isEqualTo(year, 2014)));
		assertCriteria(3 + 2, criteriaBool);
	}

	@Test
	public final void testIsNotEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isNotEqualTo(year, 1984);
		assertCriteria(13 - 3, criteriaBool);
	}

	@Test
	public final void testIsNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isNull(year);
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testIsNotNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isNotNull(year);
		assertCriteria(12, criteriaBool);
	}

	@Test
	public final void testIsGreaterThan() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThan(year, 2000);
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThan(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanOrEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThanOrEqualTo(year, 2000);
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanOrEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThanOrEqualTo(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsLessThan() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThan(year, 2000);
		assertCriteria(9, criteriaBool);
	}

	@Test
	public final void testIsLessThanNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThan(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsLessThanOrEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThanOrEqualTo(year, 2000);
		assertCriteria(10, criteriaBool);
	}

	@Test
	public final void testIsLessThanOrEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThanOrEqualTo(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsBetween() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(1980), ofExcluded(2000));
		assertCriteria(5, criteriaBool);
	}

	@Test
	public final void testIsBetweenWithNull1() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(null), ofExcluded(2000));
		assertCriteria(9, criteriaBool);
	}

	@Test
	public final void testIsBetweenWithNull2() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(1980), ofExcluded(null));
		assertCriteria(8, criteriaBool);
	}

	@Test
	public final void testStartsWith() {
		final Criteria<Movie2> criteriaBool = Criterions.startsWith(title, "a");
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testStartsWithNull() {
		final Criteria<Movie2> criteriaBool = Criterions.startsWith(title, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testInNumber() {
		final Criteria<Movie2> criteriaBool = Criterions.in(year, 1984, 1933);
		assertCriteria(3 + 1, criteriaBool);
	}

	@Test
	public final void testInNumberEmpty() {
		final Criteria<Movie2> criteriaBool = Criterions.in(year);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testInString() {
		final Criteria<Movie2> criteriaBool = Criterions.in(title, "terminator", "amadeus");
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testInStringEmpty() {
		final Criteria<Movie2> criteriaBool = Criterions.in(title);
		assertCriteria(0, criteriaBool);

		final Criteria<Movie2> criteriaBool2 = Criterions.in(title, "terminator", "amadeus").and(Criterions.in(title, (Serializable[]) new String[0]));
		assertCriteria(0, criteriaBool2);
	}

}
