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
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterId;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class IdFormatterTest {

	private final Formatter formatterId = new FormatterId("");

	/**
	 * Test du formatter de ID.
	 * @throws FormatterException
	*/
	@Test
	public void testFormatter() throws FormatterException {
		Assertions.assertEquals(10L, formatterId.stringToValue("10", DataType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue(null, DataType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue("", DataType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue(" ", DataType.Long));

		Assertions.assertEquals(10L, formatterId.stringToValue(" 10", DataType.Long));
		Assertions.assertEquals(10L, formatterId.stringToValue("10 ", DataType.Long));

		Assertions.assertEquals("10", formatterId.valueToString(10L, DataType.Long));
		Assertions.assertEquals("", formatterId.valueToString(null, DataType.Long));
	}

	@Test
	public void testFormatter1() {
		Assertions.assertThrows(FormatterException.class, () -> formatterId.stringToValue("abc ", DataType.Long));
	}

}
