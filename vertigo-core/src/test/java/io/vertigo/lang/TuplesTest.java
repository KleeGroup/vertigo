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
package io.vertigo.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tuples.
 *
 * @author pchretien
 */
public final class TuplesTest {
	@Test
	public void testTuple() {
		final Tuple<String, String> homer = Tuple.of("homer", "simpson");
		final Tuple<String, String> homer2 = Tuple.of("homer", "simpson");
		final Tuple<String, String> marge = Tuple.of("marge", "simpson");

		assertEquals(homer, homer);
		assertTrue(homer.equals(homer));
		assertEquals(homer.hashCode(), homer2.hashCode());
		assertEquals(homer, homer2);
		assertNotEquals(homer, marge);
		assertNotNull(marge);

		assertEquals("homer", homer.getVal1());
		assertEquals("simpson", homer.getVal2());

		assertEquals("marge", marge.getVal1());
		assertEquals("simpson", marge.getVal2());
	}
}
