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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Either.
 *
 * @author pchretien
 */
public final class EitherTest {
	@Test
	public void test() {
		final Either<String, Integer> one = Either.left("one");
		final Either<String, Integer> two = Either.right(2);

		assertTrue(one.left().isPresent());
		assertTrue(one.right().isEmpty());
		assertTrue(one.left().get().equals("one"));
		try {
			one.right().get();
			fail("right is empty");
		} catch (final Exception e) {
			//ok
		}
		//---
		assertTrue(two.left().isEmpty());
		assertTrue(two.right().isPresent());
		assertTrue(two.right().get().equals(2));
		try {
			two.left().get();
			fail("left is empty");
		} catch (final Exception e) {
			//ok
		}
	}
}
