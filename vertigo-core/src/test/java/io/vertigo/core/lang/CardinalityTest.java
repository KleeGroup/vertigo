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
/**
O * vertigo - simple java starter
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
package io.vertigo.core.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Cardinality.
 *
 * @author mlaroche.
 */
public final class CardinalityTest {

	@Test
	public void testOptional() {
		final Cardinality cardinality = Cardinality.fromSymbol("?");
		assertEquals("?", cardinality.toSymbol());
		assertTrue(cardinality.isOptionalOrNullable());
		assertFalse(cardinality.hasMany());
		assertFalse(cardinality.hasOne());
		assertEquals(Cardinality.OPTIONAL_OR_NULLABLE, cardinality);
	}

	@Test
	public void testOne() {
		final Cardinality cardinality = Cardinality.fromSymbol("1");
		assertEquals("1", cardinality.toSymbol());
		assertFalse(cardinality.isOptionalOrNullable());
		assertFalse(cardinality.hasMany());
		assertTrue(cardinality.hasOne());
		assertEquals(Cardinality.ONE, cardinality);
	}

	@Test
	public void testMany() {
		final Cardinality cardinality = Cardinality.fromSymbol("*");
		assertEquals("*", cardinality.toSymbol());
		assertFalse(cardinality.isOptionalOrNullable());
		assertTrue(cardinality.hasMany());
		assertFalse(cardinality.hasOne());
		assertEquals(Cardinality.MANY, cardinality);
	}

}
