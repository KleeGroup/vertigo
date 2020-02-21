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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.lang.VUserException;
import io.vertigo.util.data.SA;
import io.vertigo.util.data.SAnnotationA;

/**
 * Test de l'utilitaire de manipulation des classes.
 *
 * @author pchretien
 */
public final class ClassUtilTest {
	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testNewInstance() {
		final String className = StringBuilder.class.getCanonicalName();
		final Object created = ClassUtil.newInstance(className);
		assertEquals(StringBuilder.class, created.getClass());
	}

	@Test
	public void testNewInstanceWithType() {
		final String className = StringBuilder.class.getCanonicalName();
		final StringBuilder created = ClassUtil.newInstance(className, StringBuilder.class);
		nop(created);
		assertEquals(StringBuilder.class, created.getClass());
	}

	@Test
	public void testNewInstanceWithTypeError() {
		assertThrows(Exception.class, (() -> {
			final String className = StringBuilder.class.getCanonicalName() + ".";
			final StringBuilder created = ClassUtil.newInstance(className, StringBuilder.class);
			nop(created);

		}));
	}

	@Test
	public void testNewInstanceWithClasss() {
		final Object created = ClassUtil.newInstance(StringBuilder.class);
		assertEquals(StringBuilder.class, created.getClass());
	}

	@Test
	public void testFindConstructor() {
		final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { String.class });
		assertEquals(constructor.getDeclaringClass(), StringBuilder.class);
	}

	@Test
	public void testFindConstructorWithError() {
		assertThrows(Exception.class, (() -> {
			final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { Date.class });
			nop(constructor);
		}));
	}

	@Test
	public void testNewInstanceWithConstructor() {
		final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { String.class });
		final StringBuilder created = ClassUtil.newInstance(constructor, new Object[] { "Hello" });
		assertEquals("Hello", created.toString());
	}

	@Test
	public void testNewInstanceFail() {
		assertThrows(Exception.class, (() -> {
			final String className = StringBuilder.class.getCanonicalName() + ".";
			final Object created = ClassUtil.newInstance(className);
			nop(created);
		}));
	}

	@Test
	public void testClassForName() {
		final String className = StringBuilder.class.getCanonicalName();
		assertEquals(StringBuilder.class, ClassUtil.classForName(className));
	}

	@Test
	public void testClassForNameFail() {
		assertThrows(Exception.class, (() -> {
			final String className = StringBuilder.class.getCanonicalName() + ".";
			final Class<?> clazz = ClassUtil.classForName(className);
			nop(clazz);
		}));
	}

	@Test
	public void testClassForNameWithType() {
		final String className = StringBuilder.class.getCanonicalName();
		assertEquals(StringBuilder.class, ClassUtil.classForName(className, StringBuilder.class));
	}

	@Test
	public void testClassForNameWithTypeFail() {
		assertThrows(Exception.class, (() -> {
			final String className = StringBuilder.class.getCanonicalName() + ".";
			final Class<?> clazz = ClassUtil.classForName(className, StringBuilder.class);
			nop(clazz);
		}));
	}

	@Test
	public void testClassForNameWithSuperType() {
		final String className = StringBuilder.class.getCanonicalName();
		assertEquals(StringBuilder.class, ClassUtil.classForName(className, Serializable.class));
	}

	@Test
	public void testFindMethod() {
		final Method method = ClassUtil.findMethod(StringBuilder.class, "lastIndexOf", String.class);
		nop(method);
		assertEquals("lastIndexOf", method.getName());
	}

	@Test
	public void testFindMethodWithError() {
		assertThrows(Exception.class, (() -> {
			final Method method = ClassUtil.findMethod(StringBuilder.class, "lastIndexOf", Date.class);
			nop(method);
		}));
	}

	@Test
	public void testAnnotatedMethods() {
		final Collection<Method> methods = ClassUtil.getAllMethods(SA.class, SAnnotationA.class);
		Assertions.assertEquals(1, methods.size());
		Assertions.assertEquals("annotatedMethod", methods.iterator().next().getName());
	}

	@Test
	public void testAllFields() {
		final Collection<Field> fields = ClassUtil.getAllFields(String.class);
		//On vérifi que la propriété 'value' appartient à la liste
		final boolean found = fields.stream()
				.anyMatch(field -> "value".equals(field.getName()));
		assertTrue(found);
	}

	@Test
	public void testGetField() throws SecurityException {
		final MyInjected myInjected = new MyInjected();
		Field field;
		field = getField(MyInjected.class, "myLong");
		assertEquals(1L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myPrivateLong");
		assertEquals(2L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myFinalLong");
		assertEquals(3L, ClassUtil.get(myInjected, field));
	}

	@Test
	public void testSetField() throws SecurityException {
		final MyInjected myInjected = new MyInjected();
		Field field;
		//---
		field = getField(MyInjected.class, "myLong");
		ClassUtil.set(myInjected, field, 10L);
		assertEquals(10L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myPrivateLong");
		ClassUtil.set(myInjected, field, 20L);
		assertEquals(20L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myFinalLong");
		ClassUtil.set(myInjected, field, 30L);
		assertEquals(30L, ClassUtil.get(myInjected, field));
	}

	@Test
	public void testInvoke() {
		final Method addMethod = ClassUtil.findMethod(MyMath.class, "add", long.class, long.class);
		final Object result = ClassUtil.invoke(new MyMath(), addMethod, 4L, 6L);
		assertEquals(10L, result);
	}

	@Test
	public void testInvokeWithError() {
		assertThrows(ArithmeticException.class, (() -> {
			final Method divMethod = ClassUtil.findMethod(MyMath.class, "div", long.class, long.class);
			final Object result = ClassUtil.invoke(new MyMath(), divMethod, 4L, 0L);
			nop(result);
		}));
	}

	@Test
	public void testInvokeWithException() {
		assertThrows(VUserException.class, (() -> {
			final Method addMethod = ClassUtil.findMethod(MyMath.class, "kuser", long.class, long.class);
			final Object result = ClassUtil.invoke(new MyMath(), addMethod, 4L, 6L);
			nop(result);
		}));
	}

	@Test
	public void testGeneric() throws SecurityException, NoSuchFieldException {
		Field field;
		//---
		field = MyGenerics.class.getField("myOption");
		assertEquals(Long.class, ClassUtil.getGeneric(field));
		//---
		field = MyGenerics.class.getField("myList");
		assertEquals(Long.class, ClassUtil.getGeneric(field));
		//---
		field = MyGenerics.class.getField("myList3");
		assertEquals(Map.class, ClassUtil.getGeneric(field));
	}

	public List<String> getWords() {
		return null;
	}

	@Test
	public void testGenericReturn() throws NoSuchMethodException, SecurityException {
		assertEquals(String.class, ClassUtil.getGeneric(
				this.getClass().getMethod("getWords").getGenericReturnType(),
				() -> new IllegalStateException()));
	}

	@Test
	public void testGenericWithError() throws SecurityException {
		assertThrows(Exception.class, (() -> {
			final Field field = MyGenerics.class.getField("myList2");
			final Class<?> generic = ClassUtil.getGeneric(field);
			nop(generic);
		}));
	}

	@Test
	public void testMethodParameterGeneric() throws NoSuchMethodException, SecurityException {
		final Method method = MyGenerics.class.getMethod("setOption", Optional.class);
		//---
		assertEquals(Long.class, ClassUtil.getGeneric(method, 0));
	}

	@Test
	public void testConstructorParameterGeneric() throws NoSuchMethodException, SecurityException {
		final Constructor<MyGenerics> constructor = MyGenerics.class.getConstructor(Optional.class, List.class, List.class, List.class);
		//---
		assertEquals(Long.class, ClassUtil.getGeneric(constructor, 0));
		//---
		assertEquals(Long.class, ClassUtil.getGeneric(constructor, 1));
		//---
		//assertEquals(Long.class, ClassUtil.getGeneric(constructor, 2));
		//---
		assertEquals(Map.class, ClassUtil.getGeneric(constructor, 3));
	}

	@Test
	public void testConstructorParameterGenericWithError() throws NoSuchMethodException, SecurityException {
		final Constructor<MyGenerics> constructor = MyGenerics.class.getConstructor(Optional.class, List.class, List.class, List.class);
		//---
		assertThrows(Exception.class, (() -> {
			final Class<?> generic = ClassUtil.getGeneric(constructor, 2);
			nop(generic);
		}));
	}

	@Test
	public void testGetAllFieldsByAnnotation() throws SecurityException {
		int i = 0;
		Collection<Field> fields;
		//--
		final String[] injectedFieldNames = { "publicValue2", "protectedValue2", "packageValue2", "privateValue2" };
		fields = ClassUtil.getAllFields(MyBean.class, Inject.class);

		assertEquals(injectedFieldNames.length, fields.size());
		for (final Field field : fields) {
			assertEquals(injectedFieldNames[i], field.getName());
			i++;
		}
		//--
		i = 0;
		final String[] deprectatedFieldNames = { "publicValue1" };
		fields = ClassUtil.getAllFields(MyBean.class, Deprecated.class);

		assertEquals(deprectatedFieldNames.length, fields.size());
		for (final Field field : fields) {
			assertEquals(deprectatedFieldNames[i], field.getName());
			i++;
		}
		//--
		i = 0;
		final String[] injectedFieldNames2 = { "privateValue4", "publicValue2", "protectedValue2", "packageValue2", "privateValue2" };
		fields = ClassUtil.getAllFields(MySubBean.class, Inject.class);

		assertEquals(injectedFieldNames2.length, fields.size());
		for (final Field field : fields) {
			assertEquals(injectedFieldNames2[i], field.getName());
			i++;
		}
	}

	@Test
	public void testGetAllInterfaces() throws SecurityException {
		Set<Class<?>> interfaces;
		final Set<Class<?>> declaredInterfaces = new HashSet<>();
		declaredInterfaces.add(MyInterface1.class);
		interfaces = ClassUtil.getAllInterfaces(MyBean.class);
		//--
		assertEquals(declaredInterfaces.size(), interfaces.size());
		assertEquals(declaredInterfaces, interfaces);

		final Set<Class<?>> inheritedInterfaces = new HashSet<>();
		inheritedInterfaces.add(MyInterface2.class);
		inheritedInterfaces.add(MyInterface4.class);
		inheritedInterfaces.add(MyInterface3.class);
		inheritedInterfaces.add(MyInterface1.class);
		interfaces = ClassUtil.getAllInterfaces(MySubBean.class);
		//--
		assertEquals(inheritedInterfaces.size(), interfaces.size());
		assertEquals(inheritedInterfaces, interfaces);
	}

	@Test
	public void testGetPropertyName() throws NoSuchMethodException, SecurityException {
		Method method;
		//---
		method = MyPojo.class.getDeclaredMethod("getValue1");
		assertEquals("value1", ClassUtil.getPropertyName(method));
		//---
		method = MyPojo.class.getDeclaredMethod("getValueCamelCase");
		assertEquals("valueCamelCase", ClassUtil.getPropertyName(method));
		//---
		method = MyPojo.class.getDeclaredMethod("isValue2");
		assertEquals("value2", ClassUtil.getPropertyName(method));
	}

	@Test
	public void testGetPropertyNameSetterWithError() throws NoSuchMethodException, SecurityException {
		final Method method = MyPojo.class.getDeclaredMethod("setValue1", Long.class);
		assertThrows(Exception.class, (() -> {
			final String name = ClassUtil.getPropertyName(method);
			nop(name);
		}));
	}

	@Test
	public void testGetPropertyNameIsWithError() throws NoSuchMethodException, SecurityException {
		final Method method = MyPojo.class.getDeclaredMethod("isValueLong");

		assertThrows(Exception.class, (() -> {
			final String name = ClassUtil.getPropertyName(method);
			nop(name);
		}));
	}

	@Test
	public void testGetPropertyNameHasWithError() throws SecurityException {
		assertThrows(Exception.class, (() -> {
			final Method method = MyPojo.class.getDeclaredMethod("hasValue2");
			final String name = ClassUtil.getPropertyName(method);
			nop(name);
		}));
	}

	public static final class MyInjected {
		public Long myLong = 1L;
		private Long myPrivateLong = 2L;
		private final Long myFinalLong = 3L;

		final void initMyPrivateLong() {
			myPrivateLong = 2L;
			nop(myPrivateLong); //Pour faire croire que la var est utilisée
			nop(myFinalLong); //idem
		}
	}

	public static final class MyGenerics {
		//Tests pour les génériques
		public Optional<Long> myOption;
		public List<Long> myList;
		public List<?> myList2;
		public List<Map<?, ?>> myList3;

		public MyGenerics(final Optional<Long> myOption, final List<Long> myList, final List<?> myList2, final List<Map<?, ?>> myList3) {
			//rien
		}

		public void setOption(final Optional<Long> option) {
			myOption = option;
		}
	}

	public static final class MyMath {
		public long div(final long v1, final long v2) {
			return v1 / v2;
		}

		public long add(final long v1, final long v2) {
			return v1 + v2;
		}

		public long kuser(final long v1, final long v2) {
			throw new VUserException("test");
		}

	}

	public static class MyBean implements MyInterface1 {
		@Deprecated
		public Long publicValue1;
		@Inject
		public Long publicValue2;
		protected Long protectedValue1;
		@Inject
		protected Long protectedValue2;
		Long packageValue1;
		@Inject
		Long packageValue2;
		private Long privateValue1;
		@Inject
		private Long privateValue2;

		public MyBean() {
			nop(privateValue1);
			nop(privateValue2);
		}
	}

	public static final class MySubBean extends MyBean implements MyInterface2, MyInterface4 {
		private Long privateValue3;
		@Inject
		private Long privateValue4;

		public MySubBean() {
			nop(privateValue3);
			nop(privateValue4);
		}

	}

	public interface MyInterface1 {
		//rien
	}

	public interface MyInterface2 {
		//rien
	}

	public interface MyInterface3 {
		//rien
	}

	public interface MyInterface4 extends MyInterface3 {
		//rien
	}

	public static class MyPojo {
		public Long getValue1() {
			return 1L;
		}

		public void setValue1(final Long value1) {
			nop(value1);
		}

		public Long getValueCamelCase() {
			return 1L;
		}

		public boolean isValue2() {
			return true;
		}

		public boolean hasValue3() {
			return true;
		}

		public Long isValueLong() {
			return 2L;
		}
	}

	private static Field getField(final Class<?> clazz, final String fieldName) {
		final Collection<Field> fields = ClassUtil.getAllFields(clazz);
		for (final Field field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		fail("field " + fieldName + " non trouvé dans " + clazz.getSimpleName());
		return null;
	}

}
