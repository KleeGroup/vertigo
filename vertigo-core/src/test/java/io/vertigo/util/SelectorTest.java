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
package io.vertigo.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.component.Component;
import io.vertigo.lang.Tuple;
import io.vertigo.util.Selector.ClassConditions;
import io.vertigo.util.Selector.FieldConditions;
import io.vertigo.util.Selector.MethodConditions;
import io.vertigo.util.data.ARandomAnnotation;
import io.vertigo.util.data.SA;
import io.vertigo.util.data.SAbstractD;
import io.vertigo.util.data.SAnnotationA;
import io.vertigo.util.data.SB;
import io.vertigo.util.data.SC;

/**
 * Junit test of the Selector Class.
 * @author mlaroche
 */
public final class SelectorTest {

	private static final String TEST_CLASSES_PACKAGE = "io.vertigo.util.data";

	//-----
	//---From
	//-----
	@Test
	public void testFromOneClass() {
		Assertions.assertEquals(1, new Selector().from(SA.class).findClasses().size());
	}

	@Test
	public void testFromClasses() {
		final List<Class> classes = new ListBuilder<Class>().add(SA.class).add(SB.class).build();
		Assertions.assertEquals(2, new Selector().from(classes).findClasses().size());
	}

	@Test
	public void testFromPackages() {
		final Collection<Class> result = new Selector().from(TEST_CLASSES_PACKAGE).findClasses();
		// ---
		Assertions.assertEquals(6, result.size());
	}

	@Test
	public void testFromSupplier() {
		final Collection<Class> result = new Selector().from(() -> new ListBuilder<Class>().add(SA.class).add(SB.class).build()).findClasses();
		// ---
		Assertions.assertEquals(2, result.size());
	}

	//-----
	//---Filter
	//-----
	@Test
	public void testFilterByClassAnnotation() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class))
				.findClasses();
		// ---
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterAbstract() {
		final List<Class> classes = new ListBuilder<Class>().add(SAbstractD.class).add(SA.class).add(SB.class).add(SC.class).build();
		Assertions.assertEquals(1, new Selector().from(classes).filterClasses(ClassConditions.isAbstract()).findClasses().size());
	}

	@Test
	public void testFilterBySubtype() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.subTypeOf(Component.class))
				.findClasses();
		// ---
		Assertions.assertEquals(3, result.size()); //SA SB SAbstractD
	}

	@Test
	public void testFilterByMethodAnnotation() {
		final Collection<Tuple<Class, Method>> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterMethods(MethodConditions.annotatedWith(SAnnotationA.class))
				.findMethods();
		// ---
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterByInterface() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.interfaces())
				.findClasses();
		// ---
		Assertions.assertEquals(3, result.size());
	}

	@Test
	public void testFindFields() {
		final Collection<Tuple<Class, Field>> result = new Selector()
				.from(SC.class)
				.findFields();
		// ---
		Assertions.assertEquals(SC.class.getDeclaredFields().length, result.size());
	}

	@Test
	public void testFindFieldsAnnotation() {
		final Collection<Tuple<Class, Field>> result = new Selector()
				.from(SC.class)
				.filterFields(FieldConditions.annotatedWith(SAnnotationA.class))
				.findFields();
		// ---
		Assertions.assertEquals(1, result.size());
	}

	//-----
	//---Logical operators
	//-----
	@Test
	public void testOr() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class)
						.or(ClassConditions.subTypeOf(SB.class)))
				.findClasses();
		// ---
		Assertions.assertEquals(2, result.size());
	}

	@Test
	public void testAnd() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class))
				.filterClasses(ClassConditions.subTypeOf(Component.class))
				.findClasses();
		// ---
		Assertions.assertEquals(1, result.size()); // SA
	}

	@Test
	public void testNot() {
		final Collection<Class> result = new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.interfaces().negate())
				.findClasses();
		// ---
		Assertions.assertEquals(3, result.size()); // SA SC an SAbstractC
	}

	//--- from -> filter -> find
	@Test
	public void testUsageException() {
		Assertions.assertThrows(IllegalStateException.class, this::testException);
	}

	private void testException() {
		//We want to check that all 'from" clauses must be put together
		new Selector()
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class))
				.from(SA.class)
				.findClasses();
	}
}
