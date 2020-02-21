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
package io.vertigo.dynamo.collections.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.lang.Assertion;

/**
 * Facette.
 * Valeur d'une définition de facette.
 * la facette est soit constituée
 * - de catégories (range) et dénombre alors le nombre d'éléments par Range
 *  ex : prix de [0-10[ [10-100[ [100-*]
 * - de terms distincts et dénombre alors le nombre d'éléments par term
 *  ex : marques de voiture renault, peugeot, ford
 *  ex : villes ou départements
 * @author pchretien, npiedeloup
 */
public final class Facet implements Serializable {
	private static final long serialVersionUID = -6496651592068817414L;

	private final DefinitionReference<FacetDefinition> facetDefinitionRef;
	private final Map<FacetValue, Long> facetValues;

	/**
	 * Constructor.
	 * @param facetDefinition Definition de la facette
	 * @param facetValues Liste des valeurs de facette (ordonnée)
	 */
	public Facet(final FacetDefinition facetDefinition, final Map<FacetValue, Long> facetValues) {
		Assertion.checkNotNull(facetDefinition);
		Assertion.checkNotNull(facetValues);
		Assertion.checkArgument(facetValues instanceof LinkedHashMap || facetValues instanceof SortedMap,
				"FacetValues must be sorted, shoud implements SortedMap or LinkedHashMap ({0})", facetValues.getClass().getSimpleName());
		//-----
		facetDefinitionRef = new DefinitionReference<>(facetDefinition);
		this.facetValues = Collections.unmodifiableMap(facetValues);
	}

	/**
	 * @return Définition de la facette.
	 */
	public FacetDefinition getDefinition() {
		return facetDefinitionRef.get();
	}

	/**
	 * Valeurs des facettes ordonnées. (Range ou Term)
	 * @return Map (range | term ; count)
	 */
	public Map<FacetValue, Long> getFacetValues() {
		return facetValues;
	}
}
