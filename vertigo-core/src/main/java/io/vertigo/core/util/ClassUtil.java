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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

/**
 * The ClassUtil class provides methods to determine the structure of a class or to create instances.
 *
 * @author pchretien
 */
public final class ClassUtil {
	private static final Class<?>[] EMPTY_CLAZZ_ARRAY = new Class[0];

	/**
	 * Constructor
	 */
	private ClassUtil() {
		// private constructor
	}

	/**
	 * Création d'une nouvelle instance non typée via un nom de classe (constructeur vide).
	 * Veuillez privilégier les méthodes retournat une instance typé dés que le type est connu.
	 * @param javaClassName Nom de la classe
	 * @return Nouvelle instance
	 */
	public static Object newInstance(final String javaClassName) {
		final Class<?> javaClass = classForName(javaClassName);
		return newInstance(javaClass);
	}

	/**
	 * Création d'une nouvelle instance typée via un nom de classe (constructeur vide).
	 *
	 * @param <J> Type de l'instance retournée
	 * @param javaClassName Nom de la classe
	 * @param  type Type retourné
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final String javaClassName, final Class<J> type) {
		final Class<? extends J> javaClass = classForName(javaClassName, type);
		return newInstance(javaClass);
	}

	/**
	 * Création d'une nouvelle instance typée via une classe (constructeur vide).
	 *
	 * @param <J> Type de l'instance retournée
	 * @param clazz Classe
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final Class<J> clazz) {
		final Constructor<? extends J> constructor = findConstructor(clazz);
		return newInstance(constructor, EMPTY_CLAZZ_ARRAY);
	}

	/**
	 * Création d'une nouvelle instance typée via un constructeur et ses arguments.
	 *
	 * @param <J> Type de l'instance retournée
	 * @param constructor Constructeur
	 * @param args Arguments de la construction
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final Constructor<J> constructor, final Object[] args) {
		Assertion.check()
				.isNotNull(constructor)
				.isNotNull(args);
		//-----
		try {
			return constructor.newInstance(args);
		} catch (final InvocationTargetException e) {
			throw WrappedException.wrap(e, "An error has occurred while invoking the constructor on class : {0} ", constructor.getDeclaringClass());
		} catch (final java.lang.IllegalAccessException e) {
			throw WrappedException.wrap(e, "The constructor on class {0} is not accessible", constructor.getDeclaringClass());
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Unable to instanciate the class {0}", constructor.getDeclaringClass());
		}
	}

	/**
	 * Récupère le constructeur sans paramètres.
	 * @param clazz Classe sur laquelle on recherche le constructeur
	 * @return Constructeur recherché
	 */
	private static <J> Constructor<J> findConstructor(final Class<J> clazz) {
		return findConstructor(clazz, EMPTY_CLAZZ_ARRAY);
	}

	/**
	* Récupère le constructeur correspondant à la signature indiquée.
	* @param <J> Class type
	* @param clazz Classe sur laquelle on recherche le constructeur
	* @param parameterTypes Signature du constructeur recherché
	* @return Constructeur recherché
	*/
	public static <J> Constructor<J> findConstructor(final Class<J> clazz, final Class<?>[] parameterTypes) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(parameterTypes);
		//-----
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (final NoSuchMethodException e) {
			if (parameterTypes.length == 0) {
				//Dans le cas des constructeur vide (sans paramètre), on lance un message plus simple.
				throw WrappedException.wrap(e, "Aucun constructeur vide trouvé sur {0}", clazz.getSimpleName());
			}
			throw WrappedException.wrap(e, "Aucun constructeur trouvé sur {0} avec la signature {1}", clazz.getSimpleName(), Arrays.toString(parameterTypes));
		}
	}

	/**
	 * Récupération d'une classe non typée à partir de son nom.
	 *
	 * @param javaClassName Nom de la classe
	 * @return Classe java
	 */
	public static Class<?> classForName(final String javaClassName) {
		Assertion.check()
				.isNotBlank(javaClassName);
		//-----
		try {
			return Class.forName(javaClassName);
		} catch (final ClassNotFoundException e) {
			throw WrappedException.wrap(e, "Impossible de trouver la classe : {0}", javaClassName);
		}
	}

	/**
	 * Récupération d'une classe typée à partir de son nom.
	 *
	 * @param <J> Type de l'instance retournée
	 * @param javaClassName Nom de la classe
	 * @param type Type.
	 * @return Classe java
	 */
	public static <J> Class<? extends J> classForName(final String javaClassName, final Class<J> type) {
		Assertion.check()
				.isNotNull(javaClassName)
				.isNotNull(type);
		//-----
		try {
			return Class.forName(javaClassName).asSubclass(type);
		} catch (final ClassNotFoundException e) {
			throw WrappedException.wrap(e, "Impossible de trouver la classe : '{0}'", javaClassName);
		} catch (final NoClassDefFoundError e) {
			throw WrappedException.wrap(e, "Impossible de charger une des classes dépendante de : '{0}'", javaClassName);
		} catch (final ClassCastException e) {
			throw WrappedException.wrap(e, "La classe {0} doit être une sous-class de : {1}", javaClassName, type.getSimpleName());
		}
	}

	/**
	 * Dynamic invocation of a method on a specific instance.
	 *
	 * @param instance Object
	 * @param method method which is invocated
	 * @param args Args
	 * @return value provided as the result by the method
	 */
	public static Object invoke(final Object instance, final Method method, final Object... args) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(method);
		//-----
		try {
			return method.invoke(instance, args);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "accès impossible à la méthode : {0} de {1}", method.getName(), method.getDeclaringClass().getName());
		} catch (final InvocationTargetException e) {
			throw WrappedException.wrap(e, "Erreur lors de l'appel de la méthode : {0} de {1}", method.getName(), method.getDeclaringClass().getName());
		}
	}

	/**
	 * Affectation dynamique de la valeur d'un champ (méme privé).
	 *
	 * @param instance Objet sur lequel est invoqué la méthode
	 * @param field Champ concerné
	 * @param value Nouvelle valeur
	 */
	public static void set(final Object instance, final Field field, final Object value) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(field);
		//-----
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "accès impossible au champ : {0} de {1}", field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * Récupération dynamique de la valeur d'un champ.
	 *
	 * @param instance Objet sur lequel est invoqué la méthode
	 * @param field Champ concerné
	 * @return Valeur
	 */
	public static Object get(final Object instance, final Field field) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(field);
		//-----
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "accès impossible au champ : {0} de {1}", field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * Récupère la méthode correspondant au nom et à la signature indiquée parmi les méthodes passées.
	 * @param clazz Classe sur laquelle on recherche les méthodes
	 * @param methodName Nom de la méthode recherchée
	 * @param parameterTypes Signature de la méthode recherchée
	 * @return Méthode recherchée
	 */
	public static Method findMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(methodName)
				.isNotNull(parameterTypes);
		//-----
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (final NoSuchMethodException e) {
			throw WrappedException.wrap(e, "Méthode {0} non trouvée sur {1}", methodName, clazz.getName());
		}
	}

	/**
	 * Retourne tous les champs déclarés (incluant les champs parents) et annotés pour une classe donnée.
	 * @param clazz Class
	 * @param annotation Annotation attendue
	 * @return Tous les champs déclarés (incluant les champs parents)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(annotation);
		//-----
		return ClassUtil.getAllFields(clazz)
				.stream()
				.filter(field -> field.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}

	/**
	 * Retourne toutes les méthodes déclarées et annotées par la dite annotation.
	 * @param clazz Class
	 * @param annotation Annotation attendue
	 * @return Tous les champs déclarés (incluant les champs parents)
	 */
	public static Collection<Method> getAllMethods(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(annotation);
		//-----
		return ClassUtil.getAllMethods(clazz)
				.stream()
				.filter(method -> method.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}

	/**
	 * Retourne tous les champs déclarés (incluant les champs parents) pour une classe donnée.
	 * @param clazz Class
	 * @return Tous les champs déclarés (incluant les champs parents)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//-----
		final List<Field> fields = new ArrayList<>();
		final Field[] declaredFields = clazz.getDeclaredFields();
		fields.addAll(Arrays.asList(declaredFields));
		final Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			fields.addAll(getAllFields(parent));
		}
		return Collections.unmodifiableCollection(fields);
	}

	/**
	 * Retourne toutes les méthodes déclarées pour une classe donnée (incluant les méthodes des parents).
	 * @param clazz Class
	 * @return Toutes les méthodes déclarées (incluant les méthodes des parents)
	 */
	public static Collection<Method> getAllMethods(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//-----
		final List<Method> methods = new ArrayList<>();
		final Method[] declaredMethods = clazz.getDeclaredMethods();
		methods.addAll(Arrays.asList(declaredMethods));
		final Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			methods.addAll(getAllMethods(parent));
		}
		return Collections.unmodifiableCollection(methods);
	}

	/**
	 * Retourne toutes les interfaces (incluant celles des parents) pour une classe donnée.
	 * @param clazz Class
	 * @return Toutes les interfaces implémentées
	 */
	public static Set<Class<?>> getAllInterfaces(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//-----
		Class<?> root = clazz;
		final Set<Class<?>> allInterfaces = new HashSet<>();
		while (root != null) {
			for (final Class<?> intf : root.getInterfaces()) {
				allInterfaces.add(intf);
				for (final Class<?> iIntf : getAllInterfaces(intf)) {
					allInterfaces.add(iIntf);
				}
			}
			root = root.getSuperclass();
		}
		return Collections.unmodifiableSet(allInterfaces);
	}

	/**
	 * Récupération du type générique d'un champ paramétré.
	 * Il convient qu'il y ait UN et un seul générique déclaré.
	 * exemple  :
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param constructor constructeur
	 * @param i Index du paramètre dans le composant
	 * @return Classe du type générique
	 */
	public static Class<?> getGeneric(final Constructor<?> constructor, final int i) {
		Assertion.check()
				.isNotNull(constructor);
		//-----
		return getGeneric(
				constructor.getGenericParameterTypes()[i],
				() -> new UnsupportedOperationException("La détection du générique n'a pas pu être effectuée sur le constructeur " + constructor));
	}

	/**
	 * Récupération du type générique d'un champ paramétré.
	 * Il convient qu'il y ait UN et un seul générique déclaré.
	 * exemple  :
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param method method
	 * @param i Index du paramètre dans le composant
	 * @return Classe du type générique
	 */
	public static Class<?> getGeneric(final Method method, final int i) {
		Assertion.check()
				.isNotNull(method);
		//-----
		return getGeneric(
				method.getGenericParameterTypes()[i],
				() -> new UnsupportedOperationException("La détection du générique n'a pas pu être effectuée sur la methode " + method.getDeclaringClass() + "." + method.getName()));
	}

	/**
	 * Récupération du type générique d'un champ paramétré.
	 * Il convient qu'il y ait UN et un seul générique déclaré.
	 * exemple  :
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param field Champ
	 * @return Classe du type générique
	 */
	public static Class<?> getGeneric(final Field field) {
		Assertion.check()
				.isNotNull(field);
		//-----
		return getGeneric(field.getGenericType(),
				() -> new UnsupportedOperationException("La détection du générique n'a pas pu être effectuée sur le champ " + field.getName()));
	}

	/**
	 * Finds the generic type.
	 * Ex : List<Car> ==> Car
	 * @param type the
	 * @param exceptionSupplier
	 * @return first Generic of this class
	 */
	public static Class<?> getGeneric(
			final Type type,
			final Supplier<RuntimeException> exceptionSupplier) {
		Assertion.check()
				.isNotNull(type)
				.isNotNull(exceptionSupplier);
		//---
		if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
			Assertion.check()
					.isTrue(parameterizedType.getActualTypeArguments().length == 1, "Il doit y avoir 1 et 1 seul générique déclaré");
			final Type optionType = parameterizedType.getActualTypeArguments()[0];
			if (optionType instanceof Class) {
				return (Class<?>) optionType;
			} else if (optionType instanceof ParameterizedType) {
				//Cas ou le type paramétré est lui même paramétré
				return (Class<?>) ((ParameterizedType) optionType).getRawType();
			}

		}
		throw exceptionSupplier.get();
	}

	/**
	 * Détermine le nom de la propriété associée à un getteur.
	 * @param method Méthode du getteur
	 * @return Nom de la propriété associée
	 */
	public static String getPropertyName(final Method method) {
		Assertion.check()
				.isNotNull(method);
		//-----
		final String property;
		if (method.getName().startsWith("get")) {
			property = method.getName().substring("get".length());
		} else if (method.getName().startsWith("is")) {
			Assertion.check()
					.isTrue(Boolean.class.equals(method.getReturnType()) || boolean.class.equals(method.getReturnType()), "une méthode is concerne un boolean : {0}", method);
			property = method.getName().substring("is".length());
		} else {
			throw new IllegalArgumentException("Type de Méthode " + method + " non gérée en tant que propriété");
		}
		//On abaisse la première lettre
		return StringUtil.first2LowerCase(property);
	}
}
