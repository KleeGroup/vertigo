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
package io.vertigo.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class DateQueryParserUtilTest {
	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Long DIFF_MS = 5 * 1000L;

	private static void assertEqualsLocalDate(final LocalDate expectedDate, final String query) {
		final LocalDate compareDate = DateUtil.parseToLocalDate(query, DATE_PATTERN);
		assertTrue(expectedDate.isEqual(compareDate));
	}

	private static void assertEqualsInstant(final Instant expectedDate, final String query) {
		final Instant compareInstant = DateUtil.parseToInstant(query, DATE_PATTERN);
		final long deltaInMillis = Math.abs(expectedDate.toEpochMilli() - compareInstant.toEpochMilli());
		assertTrue(deltaInMillis < DIFF_MS, "expects " + expectedDate + " and finds " + compareInstant);
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError0() {
		Assertions.assertThrows(Exception.class,
				() -> DateUtil.parseToLocalDate("now+", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError1() {
		Assertions.assertThrows(Exception.class,
				//an explicit  number must be defined
				() -> DateUtil.parseToLocalDate("now+DAY", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError2() {
		Assertions.assertThrows(Exception.class,
				//Day must be in upperCase : DAY
				() -> DateUtil.parseToLocalDate("now+1Day", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError3() {
		Assertions.assertThrows(Exception.class,
				//day is not a calendar unit
				() -> DateUtil.parseToLocalDate("now+1day", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError4() {
		Assertions.assertThrows(Exception.class,
				//D is not a calendar unit even if d is valid
				() -> DateUtil.parseToLocalDate("now+1D", DATE_PATTERN));
	}

	/**
	 * Test le keyword now.
	 */
	@Test
	public void testNow() {
		final LocalDate expectedDate = LocalDate.now();
		assertEqualsLocalDate(expectedDate, "now");
	}

	@Test
	public void testInstant() {
		final Instant expectedInstant = Instant.now();
		assertEqualsInstant(expectedInstant, "now");
	}

	/**
	 * Teste l'ajout d'un jour sans multiplicateur.
	 */
	@Test
	public void testAddDay() {
		final LocalDate expectedDate = DateUtil.newDate().plusDays(1);
		assertEqualsLocalDate(expectedDate, "now+1d");
	}

	/**
	 * Add 5 weeks.
	 */
	@Test
	public void testAddWeek() {
		final LocalDate expectedDate = DateUtil.newDate().plusWeeks(5);
		assertEqualsLocalDate(expectedDate, "now+5w");
	}

	/**
	 * Teste l'ajout d'un jour avec multiplicateur.
	 */
	@Test
	public void testAddDays() {
		final LocalDate expectedDate = LocalDate.now().plusDays(2);
		assertEqualsLocalDate(expectedDate, "now+2d");
	}

	/**
	 * Teste la soustraction de jour avec multiplicateur.
	 */
	@Test
	public void testRemoveDays() {
		final LocalDate expectedDate = LocalDate.now().minusDays(12);
		assertEqualsLocalDate(expectedDate, "now-12d");
	}

	/**
	 * Teste l'ajout de mois avec multiplicateur.
	 */
	@Test
	public void testAddMonths() {
		final LocalDate expectedDate = LocalDate.now().plusMonths(3);
		assertEqualsLocalDate(expectedDate, "now+3M");
	}

	/**
	 * Teste l'ajout d'années avec multiplicateur.
	 */
	@Test
	public void testAddYears() {
		final LocalDate expectedDate = LocalDate.now().plusYears(5);
		assertEqualsLocalDate(expectedDate, "now+5y");
	}

	/**
	 * Teste l'ajout d'heures avec multiplicateur.
	 */
	@Test
	public void testAddHours() {
		final Instant expectedInstant = Instant.now().plus(50, ChronoUnit.HOURS);
		assertEqualsInstant(expectedInstant, "now+50h");
	}

	/**
	 * Teste le parsing d'une date fixe.
	 */
	@Test
	public void testFixedDate() {
		final LocalDate expectedDate = LocalDate.of(2014, 5, 25);
		assertEqualsLocalDate(expectedDate, "25/05/14");
	}
}
