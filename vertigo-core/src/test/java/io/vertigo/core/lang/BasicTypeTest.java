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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Basic Type.
 *
 * @author mlaroche
 */
public final class BasicTypeTest {

	@Test
	public void testInteger() {
		final Optional<BasicType> intType = BasicType.of(Integer.class);
		assertNotNull(intType.get());
		assertTrue(intType.get().isNumber());
		assertFalse(intType.get().isAboutDate());
		assertEquals(Integer.class, intType.get().getJavaClass());
		intType.get().checkValue(1);
		try {
			intType.get().checkValue(1L);
			fail("Long isn't a Integer");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			intType.get().checkValue("1");
			fail("String isn't a Integer");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testDouble() {
		final Optional<BasicType> doubleType = BasicType.of(Double.class);
		assertNotNull(doubleType.get());
		assertTrue(doubleType.get().isNumber());
		assertFalse(doubleType.get().isAboutDate());
		assertEquals(Double.class, doubleType.get().getJavaClass());
		doubleType.get().checkValue(1d);
		try {
			doubleType.get().checkValue(1);
			fail("Integer isn't a Double");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			doubleType.get().checkValue("1");
			fail("String isn't a Double");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testLong() {
		final Optional<BasicType> longType = BasicType.of(Long.class);
		assertNotNull(longType.get());
		assertTrue(longType.get().isNumber());
		assertFalse(longType.get().isAboutDate());
		assertEquals(Long.class, longType.get().getJavaClass());
		longType.get().checkValue(1L);
		try {
			longType.get().checkValue(1);
			fail("Integer isn't a Long");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			longType.get().checkValue("1");
			fail("String isn't a Long");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testBoolean() {
		final Optional<BasicType> booleanType = BasicType.of(Boolean.class);
		assertNotNull(booleanType.get());
		assertFalse(booleanType.get().isNumber());
		assertFalse(booleanType.get().isAboutDate());
		assertEquals(Boolean.class, booleanType.get().getJavaClass());
		booleanType.get().checkValue(true);
		try {
			booleanType.get().checkValue(1);
			fail("Integer isn't a Boolean");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			booleanType.get().checkValue("1");
			fail("String isn't a Boolean");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testString() {
		final Optional<BasicType> stringType = BasicType.of(String.class);
		assertNotNull(stringType.get());
		assertFalse(stringType.get().isNumber());
		assertFalse(stringType.get().isAboutDate());
		assertEquals(String.class, stringType.get().getJavaClass());
		stringType.get().checkValue("1");
		try {
			stringType.get().checkValue(1);
			fail("Integer isn't a String");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			stringType.get().checkValue(1L);
			fail("Long isn't a String");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testBigDecimal() {
		final Optional<BasicType> bigDecimalType = BasicType.of(BigDecimal.class);
		assertNotNull(bigDecimalType.get());
		assertTrue(bigDecimalType.get().isNumber());
		assertFalse(bigDecimalType.get().isAboutDate());
		assertEquals(BigDecimal.class, bigDecimalType.get().getJavaClass());
		bigDecimalType.get().checkValue(new BigDecimal(1));
		try {
			bigDecimalType.get().checkValue(1);
			fail("Integer isn't a BigDecimal");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			bigDecimalType.get().checkValue(1L);
			fail("Long isn't a BigDecimal");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testLocalDate() {
		final Optional<BasicType> localDateType = BasicType.of(LocalDate.class);
		assertNotNull(localDateType.get());
		assertFalse(localDateType.get().isNumber());
		assertTrue(localDateType.get().isAboutDate());
		assertEquals(LocalDate.class, localDateType.get().getJavaClass());
		localDateType.get().checkValue(LocalDate.now());
		try {
			localDateType.get().checkValue(1);
			fail("Integer isn't a LocalDate");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			localDateType.get().checkValue("1");
			fail("String isn't a LocalDate");
		} catch (final ClassCastException e) {
			//ok
		}
	}

	@Test
	public void testInstant() {
		final Optional<BasicType> instantType = BasicType.of(Instant.class);
		assertNotNull(instantType.get());
		assertFalse(instantType.get().isNumber());
		assertTrue(instantType.get().isAboutDate());
		assertEquals(Instant.class, instantType.get().getJavaClass());
		instantType.get().checkValue(Instant.now());
		try {
			instantType.get().checkValue(1);
			fail("Integer isn't a Instant");
		} catch (final ClassCastException e) {
			//ok
		}

		try {
			instantType.get().checkValue(LocalDate.now());
			fail("LocalDate isn't a Instant");
		} catch (final ClassCastException e) {
			//ok
		}
	}

}
