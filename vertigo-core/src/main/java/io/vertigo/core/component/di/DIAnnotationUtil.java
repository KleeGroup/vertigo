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

import javax.inject.Inject;

import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * @author prahmoune
 */
public final class DIAnnotationUtil {
	private DIAnnotationUtil() {
		//Classe utilitaire, constructeur est privé.
	}

	/**
	 * Récupération du constructeur.
	 * Il doit y avoir 1 et un seul constructeur public
	 * Ce constructeur doit être vide ou marqué avec l'annotation @Inject.
	 * @param clazz Class de l'objet
	 * @return Constructeur de l'objet
	 */
	public static <T> Constructor<T> findInjectableConstructor(final Class<T> clazz) {
		Assertion.checkNotNull(clazz);
		//-----
		final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
		Assertion.checkNotNull(constructors, "Aucun constructeur public identifiable");
		Assertion.checkArgument(constructors.length == 1, "Un seul constructeur public doit être déclaré sur {0}", clazz.getName());
		Assertion.checkArgument(isInjectable(constructors[0]), "Le constructeur public de {0} doit être marqué avec l'annotation @Inject ou bien être vide", clazz.getName());
		//-----

		//On a un et un seul constructeur.
		return constructors[0];
	}

	private static boolean isInjectable(final Constructor<?> constructor) {
		return constructor.getParameterTypes().length == 0 || constructor.isAnnotationPresent(Inject.class);
	}

	/**
	 * Construction d'un ID pour un composant défini par une implémentation.
	 * @param clazz Classe d'implémentation du composant
	 * @return Identifiant du composant
	 */
	public static String buildId(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//-----
		//On construit l'identifiant du composant.
		//Par convention on prend le nom de la classe.
		return StringUtil.first2LowerCase(clazz.getSimpleName());
	}

}
