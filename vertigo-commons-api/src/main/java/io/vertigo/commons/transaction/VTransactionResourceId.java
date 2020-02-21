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
package io.vertigo.commons.transaction;

import io.vertigo.lang.Assertion;

/**
 * Identification des ressources participant à la transaction.
 *
 * @author  pchretien
 * @param <R> Ressource transactionnelle.
 */
public final class VTransactionResourceId<R extends VTransactionResource> {
	/**
	 * Ordre dans lequel les ressources sont commitées.
	 * @author pchretien
	 */
	public enum Priority {
		/**
		 * Priorité maximale.
		 * Doit être utilisée pour la ressource critique.
		 */
		TOP,
		/**
		 * Priorité normale.
		 */
		NORMAL
	}

	private final Priority priority;
	private final String name;

	/**
	 * Constructor.
	 * @param priority Priorité de la ressource.
	 * @param name Nom de code de la ressource.
	 */
	public VTransactionResourceId(final Priority priority, final String name) {
		Assertion.checkNotNull(priority);
		Assertion.checkNotNull(name);
		//-----
		this.priority = priority;
		this.name = name;
	}

	/**
	 * @return Priorité de la ressource.
	 */
	public Priority getPriority() {
		return priority;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object object) {
		if (object instanceof VTransactionResourceId<?>) {
			return name.equals(((VTransactionResourceId<?>) object).name);
		}
		return false;
	}
}
