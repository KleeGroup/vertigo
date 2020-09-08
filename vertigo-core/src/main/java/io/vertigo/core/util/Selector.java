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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.TypeElementsScanner;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Tuple;
import javassist.Modifier;

/**
 * Selector of classes and methods.
 *
 * 1. Define  a scope/set of classes
 *  - by addind them
 *  - by their packages
 *
 * 2. filter
 *  - classes
 *  - methods
 *
 * 3 - find your classes or methods.
 *
 * @author mlaroche
 */
public final class Selector {
	private static final Predicate ALWAYS_TRUE = o -> true;
	private final Set<Class> classes;

	private Predicate<Method> methodPredicates = ALWAYS_TRUE;
	private Predicate<Class> classPredicates = ALWAYS_TRUE;
	private Predicate<Field> fieldPredicates = ALWAYS_TRUE;

	private Selector(final Set<Class> classes) {
		Assertion.check().isNotNull(classes);
		//---
		this.classes = classes;
	}

	/**
	 * Adds a set of classes to the scope.
	 *
	 * @param classes a supplier of classes
	 * @return the selector
	 */
	public static Selector from(final Collection<Class> classes) {
		Assertion.check().isNotNull(classes);
		//---
		return new Selector(new HashSet(classes));
	}

	/**
	 * Adds a class to the scope.
	 *
	 * @param clazz the class to add
	 * @return the selector
	 */
	public static Selector from(final Class clazz) {
		Assertion.check().isNotNull(clazz);
		//---
		return from(Set.of(clazz));
	}

	/**
	 * Adds all the classes with a package prefix in the scope.
	 *
	 * @param packageName the root package
	 * @return the selector
	 */
	public static Selector from(final String packageName) {
		Assertion.check().isNotBlank(packageName);
		// ---
		final Set<Class> classes = new Reflections(packageName,
				new TypeElementsScanner().includeAnnotations(false).includeFields(false).includeMethods(false))
						.getStore()
						.keys(TypeElementsScanner.class.getSimpleName())
						.stream()
						.map(ClassUtil::classForName)
						.collect(Collectors.toSet());
		return from(classes);
	}

	/**
	 * Filters field with a predicate.
	 * @param fieldPredicate the predicate
	 * @return the selector
	 */
	public Selector filterFields(final Predicate<Field> fieldPredicate) {
		Assertion.check().isNotNull(fieldPredicate);
		// ---
		fieldPredicates = fieldPredicates.and(fieldPredicate);
		return this;
	}

	/**
	 * Filters method with a predicate.
	 * @param methodPredicate the predicate
	 * @return the selector
	 */
	public Selector filterMethods(final Predicate<Method> methodPredicate) {
		Assertion.check().isNotNull(methodPredicate);
		// ---
		methodPredicates = methodPredicates.and(methodPredicate);
		return this;
	}

	/**
	 * Filters classes with a predicate.
	 * @param classPredicate the predicate
	 * @return the selector
	 */
	public Selector filterClasses(final Predicate<Class> classPredicate) {
		Assertion.check().isNotNull(classPredicate);
		// ---
		classPredicates = classPredicates.and(classPredicate);
		return this;
	}

	/**
	 * Find the classes matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Class> findClasses() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.filter(filterClassesBasedOnFields())
				.collect(Collectors.toList());
	}

	/**
	 * Find the methods matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple<Class, Method>> findMethods() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnFields())
				.flatMap(clazz -> Stream.of(clazz.getDeclaredMethods()))
				.filter(methodPredicates)
				.map(method -> Tuple.of(Class.class.cast(method.getDeclaringClass()), method))
				.collect(Collectors.toList());
	}

	/**
	 * Finds the fields matching the requirements with the associatedClass.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple<Class, Field>> findFields() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
				.filter(fieldPredicates)
				.map(field -> Tuple.of(Class.class.cast(field.getDeclaringClass()), field))
				.collect(Collectors.toList());
	}

	private Predicate<Class> filterClassesBasedOnMethods() {
		return clazz -> {
			//We don't want to load all declared methods if we don't care
			if (ALWAYS_TRUE.equals(methodPredicates) || clazz.getDeclaredMethods().length == 0) {
				// no methodPredicate
				// or no declaring method
				// so we keep it
				return true;
			}
			// methods are declared so we check if a method match the requirements
			return Stream.of(clazz.getDeclaredMethods()).anyMatch(methodPredicates);
		};
	}

	private Predicate<Class> filterClassesBasedOnFields() {
		return clazz -> {
			//We don't want to load all field if we don't care
			if (ALWAYS_TRUE.equals(fieldPredicates) || clazz.getDeclaredFields().length == 0) {
				// no fieldPredicates
				// or no declaring field
				// so we keep it
				return true;
			}
			// fields are declared so we check if a field match the requirements
			return Stream.of(clazz.getDeclaredFields()).anyMatch(fieldPredicates);
		};
	}

	/**
	 * Conditions for selecting a method.
	 */
	public static final class MethodConditions {
		private MethodConditions() {
			//stateless
		}

		/**
		 * Builds a predicate to check if the method is Annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Method> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return method -> method.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Conditions for selecting a method.
	 */
	public static final class FieldConditions {
		private FieldConditions() {
			//stateless
		}

		/**
		 * Builds a predicate to check if the field is Annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Field> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return field -> field.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Conditions for selecting a class.
	 */
	public static final class ClassConditions {
		private ClassConditions() {
			//stateless
		}

		/**
		 * Builds a predicate to check if the class is annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Class> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return clazz -> clazz.getAnnotationsByType(annotationClass).length > 0;
		}

		/**
		 * Builds a predicate to check if the class is a subtype of the given class.
		 * @param clazz the annotation
		 * @return the predicate
		 */
		public static Predicate<Class> subTypeOf(final Class clazz) {
			Assertion.check().isNotNull(clazz);
			//---
			return clazz::isAssignableFrom;
		}

		/**
		 * Builds a predicate to check if the class is an abstract class. (we consider here that interface class are not abstract classes)
		 * To filter interface clazz use the interfaces() ClassCondition
		 * @return the predicate
		 */
		public static Predicate<Class> isAbstract() {
			return clazz -> !clazz.isInterface() && Modifier.isAbstract(clazz.getModifiers());
		}

		/**
		 * Builds a predicate to check if the class is an interface.
		 * @return the predicate
		 */
		public static Predicate<Class> interfaces() {
			return Class::isInterface;
		}
	}
}
