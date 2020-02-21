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
package io.vertigo.dynamo.domain.model;

import java.io.Serializable;
import java.util.regex.Pattern;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * URI d'une DTC.
 *
 * @author pchretien
 */
public abstract class DtListURI implements Serializable {
	/**
	 * Expression régulière vérifiée par les URN.
	 */
	private static final Pattern REGEX_URN = Pattern.compile("[a-zA-Z0-9_:@$-]{5,80}");
	private static final long serialVersionUID = -1L;
	private final DefinitionReference<DtDefinition> dtDefinitionRef;
	protected static final char D2A_SEPARATOR = '@';

	/**
	 * URN de la ressource (Nom complet)
	 */
	private String urn;

	/**
	 * Constructor.
	 * @param dtDefinition Definition de la ressource
	 */
	public DtListURI(final DtDefinition dtDefinition) {
		dtDefinitionRef = new DefinitionReference<>(dtDefinition);
	}

	/**
	 * @return Définition de la ressource.
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		return urn().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean equals(final Object o) {
		if (o instanceof DtListURI) {
			return ((DtListURI) o).urn().equals(urn());
		}
		return false;
	}

	/**
	 * Construit une URN à partir de l'URI.
	 * Une URN est la  représentation unique d'une URI sous forme de chaine de caractères.
	 * Cette chaine peut s'insérer telle que dans une URL en tant que paramètre
	 * et ne contient donc aucun caractère spécial.
	 * Une URN respecte la regex exprimée ci dessus.
	 * @return URN de la ressource.
	 */
	public final synchronized String urn() {
		if (urn == null) {
			urn = buildUrn();
			Assertion.checkArgument(REGEX_URN.matcher(urn).matches(), "urn {0} doit matcher le pattern {1}", urn, REGEX_URN);
		}
		return urn;
	}

	/**
	 * Builds a urn from all the params.
	 * @param uri Uri to encode
	 * @return Urn
	 */
	protected abstract String buildUrn();

	/** {@inheritDoc} */

	@Override
	public final String toString() {
		//on surcharge le toString car il est utilisé dans les logs d'erreur. et celui par défaut utilise le hashcode.
		return "urn[" + getClass().getName() + "]::" + urn();
	}
}
