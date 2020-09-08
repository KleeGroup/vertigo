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
package io.vertigo.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MapBuilderTest {
	@Test
	public void testMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		assertEquals(3, map.size());
		assertEquals(2, map.get("two").intValue());
	}

	@Test
	public void testPutall() {
		final Map<String, Integer> data = new HashMap<>();
		data.put("one", 1);
		data.put("two", 2);
		data.put("three", 3);

		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.putAll(data)
				.put("four", 4)
				.build();
		assertEquals(4, map.size());
	}

	@Test
	public void testModifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.build();

		map.put("nine", 9);
		assertEquals(4, map.size());
	}

	@Test
	public void testUnmodifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		Assertions.assertThrows(Exception.class,
				() -> map.put("nine", 9));
	}
}
