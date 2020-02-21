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
package io.vertigo.core.component.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.component.Container;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Injector.
 * Create new instances.
 * Warning : Activeable méthods are not managed by Injector.
 *
 * @author pchretien
 */
public final class DIInjector {
	private DIInjector() {
		//constructor is protected, Injector contains only static methods
	}

	/**
	 * Injection de dépendances.
	 * Création d'une instance  à partir d'un conteneur de composants déjà intsanciés.
	 *
	 * @param <T> Type de l'instance
	 * @param clazz Classe de l'instance
	 * @param container Fournisseur de composants
	 * @return Instance de composants créée.
	 */
	public static <T> T newInstance(final Class<T> clazz, final Container container) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(container);
		//-----
		//On encapsule la création par un bloc try/ctach afin de préciser le type de composant qui n'a pas pu être créé.
		try {
			final T instance = createInstance(clazz, container);
			injectMembers(instance, container);
			return instance;
		} catch (final Exception e) {
			//Contextualisation de l'exception et des assertions.
			throw new DIException("Erreur lors de la création du composant de type : '" + clazz.getName() + "'", e);
		}
	}

	private static <T> T createInstance(final Class<T> clazz, final Container container) {
		//On a un et un seul constructeur public injectable.
		final Constructor<T> constructor = DIAnnotationUtil.findInjectableConstructor(clazz);
		//On recherche les paramètres
		final Object[] constructorParameters = findConstructorParameters(container, constructor);
		return ClassUtil.newInstance(constructor, constructorParameters);
	}

	/**
	 * Inject members/properties into an instance in a contex defined by a container.
	 * @param instance Object in which the members/propertis will be injected
	 * @param container container of all the components that can be injected in the instance
	 */
	public static void injectMembers(final Object instance, final Container container) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(container);
		//-----
		final Collection<Field> fields = ClassUtil.getAllFields(instance.getClass(), Inject.class);
		for (final Field field : fields) {
			final DIDependency dependency = new DIDependency(field);
			final Object injected = getInjected(container, dependency);

			//On vérifie que si il s'agit d'un champ non primitif alors ce champs n'avait pas été initialisé
			Assertion.when(!field.getType().isPrimitive())
					.check(() -> null == ClassUtil.get(instance, field), "field '{0}' is already initialized", field);
			ClassUtil.set(instance, field, injected);
		}
	}

	private static Object[] findConstructorParameters(final Container container, final Constructor<?> constructor) {
		final Object[] parameters = new Object[constructor.getParameterTypes().length];
		for (int i = 0; i < constructor.getParameterTypes().length; i++) {
			final DIDependency dependency = new DIDependency(constructor, i);
			parameters[i] = getInjected(container, dependency);
		}
		return parameters;
	}

	private static Object getInjected(final Container container, final DIDependency dependency) {
		if (dependency.isOption()) {
			if (container.contains(dependency.getName())) {
				//On récupère la valeur et on la transforme en option.
				//ex : <param name="opt-port" value="a value that can be null or not">
				return Optional.ofNullable(container.resolve(dependency.getName(), dependency.getType()));
			}
			//
			return Optional.empty();
		} else if (dependency.isList()) {
			//on récupère la liste des objets du type concerné
			final List<Object> list = new ArrayList<>();
			for (final String id : container.keySet()) {
				//On prend tous les objets ayant l'identifiant requis
				final boolean match = id.equals(dependency.getName()) || id.startsWith(dependency.getName() + '#');
				if (match) {
					final Object injected = container.resolve(id, Object.class);
					Assertion.checkArgument(dependency.getType().isAssignableFrom(injected.getClass()), "type of {0} is incorrect ; expected : {1}", id, dependency.getType().getName());
					list.add(injected);
				}
			}
			return Collections.unmodifiableList(list);
		}
		//-----
		final Object value = container.resolve(dependency.getName(), dependency.getType());
		Assertion.checkNotNull(value);
		//-----
		return value;
	}
}
