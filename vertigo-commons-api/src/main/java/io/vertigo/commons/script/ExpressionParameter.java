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
package io.vertigo.commons.script;

import io.vertigo.lang.Assertion;

/**
 * Paramètre.
 * Un paramètre est défini par
 * - son nom
 * - sa valeur
 * - sont type java
 *
 * @author  pchretien
 */
public final class ExpressionParameter {
	private final String name;
	private final Class<?> type;
	private final Object value;

	/**
	 * Constructeur definit un paramètre pour le ScriptEvaluator.
	 * @param name Nom du paramètre
	 * @param type Type du paramètre
	 * @param value Valeur du paramètre
	 */
	public ExpressionParameter(final String name, final Class<?> type, final Object value) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.when(value != null).check(() -> type.isInstance(value), "Valeur du paramètre '{0}' inconsistant avec son type '{1}'", name, type.getSimpleName());
		//-----
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * @return Nom du paramètre
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Type du paramètre
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return Valeur du paramètre
	 */
	public Object getValue() {
		return value;
	}
}
