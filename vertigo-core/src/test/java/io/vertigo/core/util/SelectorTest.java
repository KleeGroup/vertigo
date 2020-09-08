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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.Tuple;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.util.Selector.ClassConditions;
import io.vertigo.core.util.Selector.FieldConditions;
import io.vertigo.core.util.Selector.MethodConditions;
import io.vertigo.core.util.data.ARandomAnnotation;
import io.vertigo.core.util.data.SA;
import io.vertigo.core.util.data.SAbstractD;
import io.vertigo.core.util.data.SAnnotationA;
import io.vertigo.core.util.data.SB;
import io.vertigo.core.util.data.SC;

/**
 * Junit test of the Selector Class.
 * @author mlaroche
 */
public final class SelectorTest {

	private static final String TEST_CLASSES_PACKAGE = "io.vertigo.core.util.data";

	//-----
	//---From
	//-----
	@Test
	public void testFromOneClass() {
		Assertions.assertEquals(1, Selector
				.from(SA.class)
				.findClasses().size());
	}

	@Test
	public void testFromClasses() {
		final Set<Class> classes = Set.of(SA.class, SB.class);
		Assertions.assertEquals(2, Selector
				.from(classes)
				.findClasses().size());
	}

	@Test
	public void testFromPackages() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.findClasses();
		// ---
		Assertions.assertEquals(7, result.size());
	}

	//-----
	//---Filter
	//-----
	@Test
	public void testFilterByClassAnnotation() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class))
				.findClasses();
		// ---
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterAbstract() {
		final Set<Class> classes = Set.of(SAbstractD.class, SA.class, SB.class, SC.class);
		Assertions.assertEquals(1, Selector
				.from(classes)
				.filterClasses(ClassConditions.isAbstract())
				.findClasses().size());
	}

	@Test
	public void testFilterBySubtype() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.subTypeOf(Component.class))
				.findClasses();
		// ---
		Assertions.assertEquals(3, result.size()); //SA SB SAbstractD
	}

	@Test
	public void testFilterByMethodAnnotation() {
		final Collection<Tuple<Class, Method>> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterMethods(MethodConditions.annotatedWith(SAnnotationA.class))
				.findMethods();
		// ---
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterByInterface() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.interfaces())
				.findClasses();
		// ---
		Assertions.assertEquals(3, result.size());
	}

	@Test
	public void testFindFields() {
		final Collection<Tuple<Class, Field>> result = Selector
				.from(SC.class)
				.findFields();
		// ---
		Assertions.assertEquals(SC.class.getDeclaredFields().length, result.size());
	}

	@Test
	public void testFindFieldsAnnotation() {
		final Collection<Tuple<Class, Field>> result = Selector
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
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class)
						.or(ClassConditions.subTypeOf(SB.class)))
				.findClasses();
		// ---
		Assertions.assertEquals(2, result.size());
	}

	@Test
	public void testAnd() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.annotatedWith(ARandomAnnotation.class))
				.filterClasses(ClassConditions.subTypeOf(Component.class))
				.findClasses();
		// ---
		Assertions.assertEquals(1, result.size()); // SA
	}

	@Test
	public void testNot() {
		final Collection<Class> result = Selector
				.from(TEST_CLASSES_PACKAGE)
				.filterClasses(ClassConditions.interfaces().negate())
				.findClasses();
		// ---
		Assertions.assertEquals(4, result.size()); // SA SC SD and SAbstractC
	}

}
