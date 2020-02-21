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
package io.vertigo.dynamo.search.model;

import java.io.Serializable;
import java.util.Optional;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;

/**
 * Critères de recherche.
 * @author npiedeloup
 */
public final class SearchQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final ListFilter queryListFilter;
	private final Optional<ListFilter> securityListFilter;

	//Informations optionnelles pour booster la pertinence des documents plus récent (null si inutilisé)
	private final String boostedDocumentDateFieldName;
	private final Integer numDaysOfBoostRefDocument;
	private final Integer mostRecentBoost;
	private final Optional<FacetedQuery> facetedQuery;
	private final DefinitionReference<FacetDefinition> clusteringFacetDefinitionRef;

	/**
	 * Constructor.
	 * @param facetedQuery facetedQueryDefinition
	 * @param queryListFilter Filtre principal correspondant aux critères de la recherche
	 * @param securityListFilter Filtre de sécurité
	 * @param clusteringFacetDefinition Facet utilisée pour cluster des resultats (null si non utilisé)
	 * @param boostedDocumentDateField Nom du champ portant la date du document (null si non utilisé)
	 * @param numDaysOfBoostRefDocument Age des documents servant de référence pour le boost des plus récents par rapport à eux (null si non utilisé)
	 * @param mostRecentBoost Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1) (null si non utilisé)
	 */
	SearchQuery(
			final Optional<FacetedQuery> facetedQuery,
			final ListFilter queryListFilter,
			final Optional<ListFilter> securityListFilter,
			final FacetDefinition clusteringFacetDefinition,
			final DtField boostedDocumentDateField,
			final Integer numDaysOfBoostRefDocument,
			final Integer mostRecentBoost) {
		Assertion.checkNotNull(facetedQuery);
		Assertion.checkNotNull(queryListFilter);
		Assertion.checkNotNull(securityListFilter);
		Assertion.when(boostedDocumentDateField != null)
				.check(() -> numDaysOfBoostRefDocument != null && mostRecentBoost != null, "Lorsque le boost des documents récents est activé, numDaysOfBoostRefDocument et mostRecentBoost sont obligatoires.");
		Assertion.when(boostedDocumentDateField == null)
				.check(() -> numDaysOfBoostRefDocument == null && mostRecentBoost == null, "Lorsque le boost des documents récents est désactivé, numDaysOfBoostRefDocument et mostRecentBoost doivent être null.");
		Assertion.when(numDaysOfBoostRefDocument != null)
				.check(() -> numDaysOfBoostRefDocument.longValue() > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieur à 1.");
		Assertion.when(mostRecentBoost != null)
				.check(() -> mostRecentBoost.longValue() > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieur à 1.");
		//-----
		this.facetedQuery = facetedQuery;
		this.queryListFilter = queryListFilter;
		this.securityListFilter = securityListFilter;
		boostedDocumentDateFieldName = boostedDocumentDateField != null ? boostedDocumentDateField.getName() : null;
		this.numDaysOfBoostRefDocument = numDaysOfBoostRefDocument;
		this.mostRecentBoost = mostRecentBoost;
		clusteringFacetDefinitionRef = clusteringFacetDefinition != null ? new DefinitionReference<>(clusteringFacetDefinition) : null;
	}

	/**
	 * Static method factory for SearchQueryBuilder
	 * @param listFilter ListFilter
	 * @return SearchQueryBuilder
	 */
	public static SearchQueryBuilder builder(final ListFilter listFilter) {
		return new SearchQueryBuilder(listFilter);
	}

	/**
	 * Facets informations.
	 * @return facetedQuery.
	 */
	public Optional<FacetedQuery> getFacetedQuery() {
		return facetedQuery;
	}

	/**
	 * Filtre principal correspondant aux critères de la recherche.
	 * @return Valeur du filtre
	 */
	public ListFilter getListFilter() {
		return queryListFilter;
	}

	/**
	 * Filtre correspondant aux critères de sécurité.
	 * @return Valeur du filtre
	 */
	public Optional<ListFilter> getSecurityListFilter() {
		return securityListFilter;
	}

	/**
	 * Indique que la recherche propose un clustering des documents par une facette.
	 * Le nombre de document par valeur des facette est limité
	 * @return si le clustering est activé
	 */
	public boolean isClusteringFacet() {
		return clusteringFacetDefinitionRef != null;
	}

	/**
	 * @return Facette utilisé pour le clustering
	 */
	public FacetDefinition getClusteringFacetDefinition() {
		Assertion.checkArgument(isClusteringFacet(), "Le clustering des documents par facette n'est pas activé sur cette recherche");
		//-----
		return clusteringFacetDefinitionRef.get();
	}

	/**
	 * Indique que la recherche boost les documents les plus récents.
	 * C'est une formule de type 1/x qui est utilisée.
	 * La formule de boost est 1 / ((documentAgeDay/NumDaysOfBoostRefDocument) + (1/(MostRecentBoost-1)))
	 * @return si le boost est activé
	 */
	public boolean isBoostMostRecent() {
		return boostedDocumentDateFieldName != null;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Nom du champ portant la date du document
	 */
	public String getBoostedDocumentDateField() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent n'est pas activé sur cette recherche");
		//-----
		return boostedDocumentDateFieldName;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Age des documents servant de référence pour le boost des plus récents par rapport à eux
	 */
	public int getNumDaysOfBoostRefDocument() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//-----
		return numDaysOfBoostRefDocument;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1).
	 */
	public int getMostRecentBoost() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//-----
		return mostRecentBoost;
	}

}
