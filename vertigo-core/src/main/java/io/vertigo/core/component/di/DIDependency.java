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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Un composant possède une liste de dépendances.
 * Une dépendance est donc une relation entre une définition de composant et une autre définition de composant, identifiée par son id.
 * une dépendance peut porter sur
 * - une Classe quelconque,
 * - une option
 * - une Liste
 * Seul le premier type de relation revët un caractère obligatoire.
 *
 * @author pchretien
 */
final class DIDependency {
	private final String targetId;
	private final boolean isOption;
	private final boolean isList;
	private final Class<?> type;

	/**
	 * Constructor for field injection.
	 * @param field Field to inject into
	 */
	DIDependency(final Field field) {
		Assertion.checkNotNull(field);
		//-----
		final String named = getNamedValue(field.getAnnotations());
		final Class<?> rootType = field.getType();

		isOption = isOptional(rootType);
		isList = isList(rootType);
		type = (isOption || isList) ? ClassUtil.getGeneric(field) : rootType;
		targetId = named != null ? named : DIAnnotationUtil.buildId(type);
	}

	/**
	 * Constructor for constructor parameter injection.
	 * @param constructor Constructor to inject into
	 * @param i parameter index to inject into
	 */
	DIDependency(final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(constructor);
		//-----
		final String named = getNamedValue(constructor.getParameterAnnotations()[i]);
		final Class<?> rootType = constructor.getParameterTypes()[i];

		isOption = isOptional(rootType);
		isList = isList(rootType);
		type = (isOption || isList) ? ClassUtil.getGeneric(constructor, i) : rootType;
		targetId = named != null ? named : DIAnnotationUtil.buildId(type);
	}

	/**
	 * @return Inject name
	 */
	String getName() {
		return targetId;
	}

	/**
	 * @return if optionnal
	 */
	boolean isOption() {
		return isOption;
	}

	/**
	 * @return if required (not null)
	 */
	boolean isRequired() {
		return !(isList || isOption);
	}

	/**
	 * @return is list
	 */
	boolean isList() {
		return isList;
	}

	/**
	 * @return get object class
	 */
	Class<?> getType() {
		Assertion.checkNotNull(type);
		return type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (isList) {
			return targetId + '*';
		} else if (isOption) {
			return targetId + '?';
		}
		return targetId;
	}

	private static boolean isList(final Class<?> type) {
		return List.class.isAssignableFrom(type);
	}

	private static boolean isOptional(final Class<?> type) {
		return Optional.class.isAssignableFrom(type);
	}

	private static String getNamedValue(final Annotation[] annotations) {
		return Arrays.stream(annotations)
				.filter(annotation -> annotation instanceof ParamValue)
				.map(annotation -> ParamValue.class.cast(annotation).value())
				.findFirst().orElse(null);
	}
}
