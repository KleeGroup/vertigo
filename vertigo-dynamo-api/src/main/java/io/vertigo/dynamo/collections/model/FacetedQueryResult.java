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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Résultat de la recherche.
 * Tout résultat est facetté.
 * Eventuellement il n'y a aucune facette.
 * @author pchretien, dchallas
 * @param <R> Type de l'objet resultant de la recherche
 * @param <S> Type de l'objet source
 */
public final class FacetedQueryResult<R extends DtObject, S> implements Serializable {
	private static final long serialVersionUID = 1248453191954177054L;

	private final DtList<R> dtc;
	private final List<Facet> facets;
	private final Map<R, Map<DtField, String>> highlights;
	private final DefinitionReference<FacetDefinition> clusterFacetDefinitionRef; //nullable
	private final Map<FacetValue, DtList<R>> clusteredDtc;
	private final long count;
	private final S source;
	private final FacetedQuery facetedQueryOpt; //nullable

	/**
	 * Constructor.
	 * @param query Facettage de la requète
	 * @param count  Nombre total de résultats
	 * @param dtc DTC résultat, éventuellement tronquée à n (ex 500) si trop d'éléments.
	 * @param facets Liste des facettes. (Peut être vide jamais null)
	 * @param clusterFacetDefinition FacetDefinition du Cluster
	 * @param clusteredDtc Cluster des documents. (Peut être vide jamais null)
	 * @param highlights Liste des extraits avec mise en valeur par objet et par champs
	 * @param source Object source permettant rerentrer dans le mechanisme de filtrage
	 */
	public FacetedQueryResult(
			final Optional<FacetedQuery> query,
			final long count,
			final DtList<R> dtc,
			final List<Facet> facets,
			final Optional<FacetDefinition> clusterFacetDefinition,
			final Map<FacetValue, DtList<R>> clusteredDtc,
			final Map<R, Map<DtField, String>> highlights,
			final S source) {
		Assertion.checkNotNull(query);
		Assertion.checkNotNull(dtc);
		Assertion.checkNotNull(facets);
		Assertion.checkNotNull(source);
		Assertion.checkNotNull(clusterFacetDefinition);
		Assertion.checkNotNull(clusteredDtc);
		Assertion.checkNotNull(highlights);
		//-----
		this.facetedQueryOpt = query.orElse(null);
		this.count = count;
		this.dtc = dtc;
		this.facets = facets;
		this.clusterFacetDefinitionRef = clusterFacetDefinition.isPresent() ? new DefinitionReference<>(clusterFacetDefinition.get()) : null;
		this.clusteredDtc = clusteredDtc;
		this.highlights = highlights;
		this.source = source;
	}

	/**
	 * @return Nombre total de résultats
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Rappel des facettes de la requête initiale.
	 * @return Facettes de requète
	 */
	public Optional<FacetedQuery> getFacetedQuery() {
		return Optional.ofNullable(facetedQueryOpt);
	}

	/**
	 * @return DTC résultat, éventuellement tronquée à n (ex 500) si trop d'éléments.
	 */
	public DtList<R> getDtList() {
		return dtc;
	}

	/**
	 * @return Liste des facettes. (Peut être vide jamais null)
	 */
	public List<Facet> getFacets() {
		return facets;
	}

	/**
	 * @return FacetDefinition du cluster des documents par valeur de facette, si demandé lors de la requête.
	 */
	public Optional<FacetDefinition> getClusterFacetDefinition() {
		return clusterFacetDefinitionRef == null ? Optional.empty() : Optional.ofNullable(clusterFacetDefinitionRef.get());
	}

	/**
	 * @return Cluster des documents par valeur de facette, si demandé lors de la requête. (Peut être vide jamais null)
	 */
	public Map<FacetValue, DtList<R>> getClusters() {
		return clusteredDtc;
	}

	/**
	 * @param document Document dont on veut les highlights
	 * @return Extrait avec mise en valeur par champs. (Peut être vide jamais null)
	 */
	public Map<DtField, String> getHighlights(final R document) {
		return highlights.getOrDefault(document, Collections.emptyMap());
	}

	/**
	 * @return Object source permettant réentrer dans le mécanisme de filtrage.
	 */
	public S getSource() {
		return source;
	}
}
