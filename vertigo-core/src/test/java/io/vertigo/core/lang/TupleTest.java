/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tuple.
 *
 * @author pchretien
 */
public final class TupleTest {
	@Test
	public void testTuple() {
		final Tuple<String, String> homer = Tuple.of("homer", "simpson");
		final Tuple<String, String> homer2 = Tuple.of("homer", "simpson");
		final Tuple<String, String> marge = Tuple.of("marge", "simpson");

		//---
		assertNotNull(homer);
		assertNotNull(homer2);
		assertNotNull(marge);

		//--- EQ / NEQ
		assertEquals(homer, homer);
		assertEquals(homer, homer2);
		assertEquals(homer.hashCode(), homer2.hashCode());
		assertNotEquals(homer, marge);
		assertNotEquals(homer2, marge);

		//---getVal
		assertEquals("homer", homer.getVal1());
		assertEquals("simpson", homer.getVal2());

		assertEquals("marge", marge.getVal1());
		assertEquals("simpson", marge.getVal2());
	}
}
