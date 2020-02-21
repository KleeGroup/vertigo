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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

/**
 * Test de l'utilitaitre de manipulation des dates.
 *
 * @author pchretien
 */
public final class DateUtilTest {

	@Test
	public void testNewDate() {
		final LocalDate date = DateUtil.newDate();
		assertEquals(date, LocalDate.now());
	}

	@Test
	public void testNewInstant() {
		final Instant date = DateUtil.newInstant();
		assertEquals(date, Instant.now());
	}

	@Test
	public void testDaysBetween() {
		final LocalDate today = LocalDate.now();
		final LocalDate date2 = today.plusDays(48);
		final long days = DateUtil.daysBetween(today, date2);
		assertEquals(48L, days);
	}

	@Test
	public void testDaysBetweenCEST2CET() {
		final LocalDate startDate = LocalDate.of(2011, 3, 1);
		final LocalDate date2 = startDate.plusDays(48);
		final long days = DateUtil.daysBetween(startDate, date2);
		assertEquals(48, days);
	}

	@Test
	public void testDaysBetweenCET2CEST() {
		final LocalDate startDate = LocalDate.of(2011, 9, 20);
		final LocalDate date2 = startDate.plusDays(48);
		final long days = DateUtil.daysBetween(startDate, date2);
		assertEquals(48L, days);
	}

	@Test
	public void testCompareDateLower() {
		final LocalDate today = LocalDate.now();
		final LocalDate date2 = today.plusDays(20);
		assertTrue(DateUtil.compareLocalDate(today, date2) < 0);
	}

	@Test
	public void testCompareDateGreater() {
		final LocalDate today = LocalDate.now();
		final LocalDate date2 = today.minusDays(20);
		assertTrue(DateUtil.compareLocalDate(today, date2) > 0);
	}

	@Test
	public void testCompareDateEquals() {
		final LocalDate today = LocalDate.now();
		final LocalDate date2 = LocalDate.now();
		assertTrue(DateUtil.compareLocalDate(today, date2) == 0);
	}

	@Test
	public void testCompareDateTimeLower() {
		final Instant now = DateUtil.newInstant();
		final Instant date2 = now.plus(20, ChronoUnit.MINUTES);
		assertTrue(DateUtil.compareInstant(now, date2) < 0);
	}

	@Test
	public void testCompareDateTimeGreater() {
		final Instant now = DateUtil.newInstant();
		final Instant date2 = now.minus(20, ChronoUnit.MINUTES);
		assertTrue(DateUtil.compareInstant(now, date2) > 0);
	}

	@Test
	public void testCompareDateTimeEquals() {
		final Instant now = DateUtil.newInstant();
		final Instant date2 = now.minus(0, ChronoUnit.MINUTES);
		assertTrue(DateUtil.compareInstant(now, date2) == 0);
	}
}
