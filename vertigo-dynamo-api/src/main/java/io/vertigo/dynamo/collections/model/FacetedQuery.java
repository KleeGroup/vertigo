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

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.lang.Assertion;

/**
 * Requete de filtrage par facettes.
 * @author npiedeloup
 */
public final class FacetedQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final DefinitionReference<FacetedQueryDefinition> facetedQueryDefinitionRef;
	private final SelectedFacetValues selectedFacetValues;

	/**
	 * Constructor.
	 * @param facetedQueryDefinition Definition de la requête
	 * @param selectedFacetValue Liste des valeurs de facette selectionnées par facette
	 */
	public FacetedQuery(final FacetedQueryDefinition facetedQueryDefinition, final SelectedFacetValues selectedFacetValue) {
		Assertion.checkNotNull(facetedQueryDefinition);
		Assertion.checkNotNull(selectedFacetValue);
		//-----
		facetedQueryDefinitionRef = new DefinitionReference<>(facetedQueryDefinition);
		selectedFacetValues = selectedFacetValue;
	}

	/**
	 * @return Définition du FacetedQuery.
	 */
	public FacetedQueryDefinition getDefinition() {
		return facetedQueryDefinitionRef.get();
	}

	/**
	 * Liste des supplémentaires.
	 * @return Liste des filtres.
	 */
	public SelectedFacetValues getSelectedFacetValues() {
		return selectedFacetValues;
	}
}
