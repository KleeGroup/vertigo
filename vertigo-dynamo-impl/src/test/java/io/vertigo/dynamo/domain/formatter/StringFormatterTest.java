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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamox.domain.formatter.FormatterString;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class StringFormatterTest {

	@Test
	public void testUpper() {
		final Formatter formatterString = new FormatterString("UPPER");
		Assertions.assertEquals("AA", formatterString.valueToString("aa", DataType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("AA", DataType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("Aa", DataType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("aA", DataType.String));
	}

	@Test
	public void testLower() {
		final Formatter formatterString = new FormatterString("LOWER");
		Assertions.assertEquals("aa", formatterString.valueToString("aa", DataType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("AA", DataType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("Aa", DataType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("aA", DataType.String));
	}

	@Test
	public void testUpperFirst() {
		final Formatter formatterString = new FormatterString("UPPER_FIRST");
		Assertions.assertEquals("Aa", formatterString.valueToString("aa", DataType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("AA", DataType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("Aa", DataType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("aA", DataType.String));
	}
}
