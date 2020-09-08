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
package io.vertigo.core.node.component.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.component.Container;
import io.vertigo.core.node.component.di.data.A;
import io.vertigo.core.node.component.di.data.B;
import io.vertigo.core.node.component.di.data.B2;
import io.vertigo.core.node.component.di.data.E;
import io.vertigo.core.node.component.di.data.F;
import io.vertigo.core.node.component.di.data.P;
import io.vertigo.core.node.component.di.data.P2;
import io.vertigo.core.node.component.di.data.P3;

/**
 * Voir sur reactor pour l'arbre des dépendances des objets A==>F.
 * @author pchretien
 */
public final class InjectorTest {
	private static class MyContainer implements Container {
		private final Map<String, Object> map = new HashMap<>();

		@Override
		public boolean contains(final String id) {
			Assertion.check().isNotBlank(id);
			//-----
			return map.containsKey(id);
		}

		public void put(final String id, final Object object) {
			Assertion.check()
					.isNotBlank(id)
					.isNotNull(object);
			//-----
			map.put(id, object);
		}

		@Override
		public <C> C resolve(final String id, final Class<C> componentClass) {
			Assertion.check()
					.isNotBlank(id)
					.isNotNull(componentClass);
			//-----
			final Object object = map.get(id);
			Assertion.check().isNotNull(object, "{0} not found", id);
			return componentClass.cast(object);
		}

		@Override
		public Set<String> keySet() {
			return map.keySet();
		}
	}

	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testA() {
		final A a = DIInjector.newInstance(A.class, new Container() {

			@Override
			public boolean contains(final String id) {
				return false;
			}

			@Override
			public <T> T resolve(final String id, final Class<T> componentClass) {
				throw new VSystemException("component info with id '{0}' not found.", id);
			}

			@Override
			public Set<String> keySet() {
				return Collections.emptySet();
			}
		});
		nop(a);
	}

	@Test
	public void testBFail() {
		Assertions.assertThrows(DIException.class,
				() -> {
					final B b = DIInjector.newInstance(B.class, new Container() {

						@Override
						public boolean contains(final String id) {
							return false;
						}

						@Override
						public <T> T resolve(final String id, final Class<T> componentClass) {
							return null;
						}

						@Override
						public Set<String> keySet() {
							return Collections.emptySet();
						}
					});
					nop(b);
				});
	}

	@Test
	public void testB2() {
		final MyContainer container = new MyContainer();
		final A a = DIInjector.newInstance(A.class, container);
		container.put("a", a);

		Assertions.assertThrows(DIException.class,
				() -> {
					final B2 b2 = DIInjector.newInstance(B2.class, container);
					nop(b2);
				});
	}

	@Test
	public void testB() {
		final MyContainer container = new MyContainer();
		final A a = DIInjector.newInstance(A.class, container);
		container.put("a", a);
		final B b = DIInjector.newInstance(B.class, container);
		assertEquals(a, b.getA());
	}

	@Test
	public void testE() {
		final MyContainer container = new MyContainer();
		final A a = DIInjector.newInstance(A.class, container);
		container.put("a", a);
		container.put("p3", new P3());
		E e = DIInjector.newInstance(E.class, container);
		assertTrue(e.getA().isPresent());
		assertEquals(a, e.getA().get());
		assertFalse(e.getB().isPresent());
		assertEquals(0, e.getPPlugins().size());
		assertEquals(0, e.getP2Plugins().size());
		//-----
		container.put("p", new P());
		container.put("p#1", new P());
		container.put("pen", new P2());
		container.put("pen#1", new P2());
		container.put("pen#2", new P2());
		e = DIInjector.newInstance(E.class, container);
		assertTrue(e.getA().isPresent());
		assertEquals(a, e.getA().get());
		assertFalse(e.getB().isPresent());
		assertEquals(2, e.getPPlugins().size());
		assertEquals(3, e.getP2Plugins().size());
	}

	@Test
	public void testF() {
		final MyContainer container = new MyContainer();
		final A a = DIInjector.newInstance(A.class, container);
		container.put("a", a);
		container.put("param1", "test1");
		container.put("param2", "test2");
		container.put("param3", "test3");
		final F f = DIInjector.newInstance(F.class, container);
		assertEquals(a, f.getA());
		assertEquals("test1", f.getParam1());
		assertEquals("test2", f.getParam2());
		assertTrue(f.getParam3().isPresent());
		assertEquals("test3", f.getParam3().get());
		assertFalse(f.getParam4().isPresent());
	}
}
