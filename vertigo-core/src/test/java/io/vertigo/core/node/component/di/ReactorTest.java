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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.component.di.data.A;
import io.vertigo.core.node.component.di.data.B;
import io.vertigo.core.node.component.di.data.C;
import io.vertigo.core.node.component.di.data.D;
import io.vertigo.core.node.component.di.data.E;
import io.vertigo.core.node.component.di.data.F;
import io.vertigo.core.node.component.di.data.P;
import io.vertigo.core.node.component.di.data.P3;

/**
 * A ne dépend de personne.
 * B dépend de A.
 * C dépend de B et D.;
 * D dépend de C.
 * E dépend d'options de A et B.
 * F dépende de A et de paramètres nommés.
 *
 * @author pchretien
 */
public final class ReactorTest {
	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testIdArgument() {
		Assertions.assertThrows(NullPointerException.class,
				() -> {
					final DIReactor reactor = new DIReactor()
							.addComponent(null, A.class);
					nop(reactor);
				});
	}

	@Test
	public void testClassArgument() {
		Assertions.assertThrows(NullPointerException.class,
				() -> {
					final DIReactor reactor = new DIReactor()
							.addComponent("a", null);
					nop(reactor);
				});
	}

	@Test
	public void testA() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.proceed();
		assertEquals(1, list.size());
		assertEquals("a", list.get(0));
	}

	@Test
	public void testB() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.proceed();
		assertEquals(2, list.size());
		assertEquals("a", list.get(0));
		assertEquals("b", list.get(1));
	}

	@Test
	public void testB2() {
		//La resolution fonctionne ; l'erreur ne survenant qu'à la résolution
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.proceed();
		assertEquals(2, list.size());
		assertEquals("a", list.get(0));
	}

	@Test
	public void testBWithParent() {
		//rappel B dépend de A
		final List<String> list = new DIReactor()
				.addParent("a")
				.addComponent("b", B.class)
				.proceed();
		assertEquals(1, list.size());
		assertEquals("b", list.get(0));
	}

	@Test
	/** On change l'ordre ; on vérifie que Reactor gère bien l'ordre */
	public void testDependency2bis() {
		final List<String> list = new DIReactor()
				.addComponent("b", B.class)
				.addComponent("a", A.class)
				.proceed();
		assertEquals(2, list.size());
		assertEquals("a", list.get(0));
		assertEquals("b", list.get(1));
	}

	@Test
	/** On teste les options */
	public void testOption() {
		final List<String> list = new DIReactor()
				.addComponent("e", E.class)
				.addComponent("a", A.class)
				.addComponent("p3", P3.class) //Plugin objligatoire
				.addComponent("p", P.class) //Plugin facultatif car liste
				.addComponent("p2", P.class) //Plugin facultatif car liste
				.proceed();
		//E dépend de option(A) et de option(B) donc A doit être le premier élément listé
		assertEquals(5, list.size());
		assertEquals("a", list.get(0));
		//		assertEquals("p3", list.get(1));
		//		assertEquals("p", list.get(1));
		//		assertEquals("p2", list.get(1));
		assertEquals("e", list.get(4));
	}

	@Test
	/** On teste les paramètres */
	public void testParams() {
		final Set<String> params = new HashSet<>();
		params.add("param1");
		params.add("param2");
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("f", F.class, params)
				.proceed();
		assertEquals(2, list.size());
		assertEquals("a", list.get(0));
		assertEquals("f", list.get(1));
	}

	@Test
	/** On teste les paramètres */
	public void testParams2() {
		final Set<String> params = new HashSet<>();
		params.add("a");
		final List<String> list = new DIReactor()
				.addComponent("b", B.class, params)
				.proceed();
		assertEquals(1, list.size());
		assertEquals("b", list.get(0));
	}

	@Test
	public void testDependencyMissing() {
		Assertions.assertThrows(DIException.class,
				() -> {
					final List<String> list = new DIReactor()
							.addComponent("a", A.class)
							.addComponent("b", B.class)
							.addComponent("c", C.class)
							.proceed();
					nop(list);
				});
	}

	@Test
	public void testDependencyCyclic() {
		Assertions.assertThrows(DIException.class,
				() -> {
					final List<String> list = new DIReactor()
							.addComponent("a", A.class)
							.addComponent("b", B.class)
							.addComponent("c", C.class)
							.addComponent("D", D.class)
							.proceed();
					nop(list);
				});
	}

	@Test
	public void testDependencyMultiple() {
		Assertions.assertThrows(DIException.class,
				() -> {
					final List<String> list = new DIReactor()
							.addComponent("a", A.class)
							.addComponent("b", B.class)
							.addComponent("b", B.class)
							.proceed();
					nop(list);
				});
	}

	@Test
	public void testDependencyMultiple2() {
		Assertions.assertThrows(DIException.class,
				() -> {
					final List<String> list = new DIReactor()
							.addComponent("a", A.class)
							.addComponent("b", B.class)
							.addParent("b")
							.proceed();
					nop(list);
				});
	}
}
