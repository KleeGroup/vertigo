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
package io.vertigo.dynamo.domain.formatter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterDate;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class DateFormatterTest extends AbstractTestCaseJU5 {
	private final FormatterDate formatterDate = new FormatterDate("yyyy-MM-dd");
	private final FormatterDate formatterDateTime = new FormatterDate("yyyy-MM-dd' 'HH:mm:ss");

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocalesAndDefaultZoneId("fr_FR", "UTC")
				.endBoot()
				.build();
	}

	@Test
	public void testLocalDateFormatter() throws FormatterException {
		final LocalDate localDate = LocalDate.of(2000, 12, 25);
		Assertions.assertEquals("2000-12-25", formatterDate.valueToString(localDate, DataType.LocalDate));
		Assertions.assertEquals(localDate, formatterDate.stringToValue("2000-12-25", DataType.LocalDate));
	}

	@Test
	public void testInstantFormatter() throws FormatterException {
		final Instant instant = LocalDateTime.of(2009, 2, 23, 16, 30).toInstant(ZoneOffset.UTC);
		Assertions.assertEquals("2009-02-23 16:30:00", formatterDateTime.valueToString(instant, DataType.Instant));
		Assertions.assertEquals(instant, formatterDateTime.stringToValue("2009-02-23 16:30:00", DataType.Instant));
	}

	@Test
	public void testFormatterErrorLocalDate() {
		Assertions.assertThrows(FormatterException.class, () -> {
			final LocalDate localDate = LocalDate.of(2000, 12, 25);
			Assertions.assertEquals(localDate, formatterDate.stringToValue("2003/09/15", DataType.LocalDate));
		});
	}

	@Test
	public void testFormatterErrorInstant() {
		Assertions.assertThrows(FormatterException.class, () -> {
			final Instant instant = LocalDateTime.of(2009, 2, 23, 16, 30).toInstant(ZoneOffset.UTC);
			Assertions.assertEquals(instant, formatterDate.stringToValue("2003/09/15 16:30:00", DataType.Instant));
		});
	}

}
