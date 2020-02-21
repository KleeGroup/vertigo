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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition.FacetOrder;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Search faceted result merger.
 * First element is use for source, the empty result list's type (result's elements are in cluster not in list)
 * Build a cluster in result for each merged list.
 * List are merge if use the same filter when merging, in this case they must use the same type.
 *
 * @author npiedeloup
 * @param <R> Result object's type
 * @param <S> Source object's type
 */
public final class FacetedQueryResultMerger<R extends DtObject, S> {

	private final Map<String, FacetValue> facetValuePerFilter = new HashMap<>();
	private final Map<FacetValue, List<FacetedQueryResult<?, S>>> otherResults = new LinkedHashMap<>();

	private Optional<String> facetDefinitionNameOpt = Optional.empty();
	private FacetedQueryResult<?, S> firstResult;

	/**
	 * Constructor merger should create a facet for this cluster.
	 */
	public FacetedQueryResultMerger() {
		//no param for easier code when results size is variable.
		super();

	}

	/**
	 * Merger should create a facet for this cluster.
	 * @param result Result to merge
	 * @param resultcode Code for result
	 * @param resultFilter SearchQuery filter for result
	 * @param resultLabel Default string label for result
	 * @param resultLabelKey MessageKey label for result
	 * @return this builder
	 */
	public FacetedQueryResultMerger<R, S> add(
			final FacetedQueryResult<?, S> result,
			final String resultcode,
			final String resultFilter,
			final String resultLabel,
			final MessageKey resultLabelKey) {
		Assertion.checkArgNotEmpty(resultcode);
		Assertion.checkNotNull(result);
		Assertion.checkArgNotEmpty(resultFilter);
		//---
		if (firstResult == null) {
			firstResult = result;
		}
		//---
		final FacetValue otherFacetValue = facetValuePerFilter.computeIfAbsent(resultFilter,
				rf -> new FacetValue(resultcode, ListFilter.of(rf), MessageText.ofDefaultMsg(resultLabel, resultLabelKey)));

		otherResults.computeIfAbsent(otherFacetValue, k -> new ArrayList<>())
				.add(result);
		return this;
	}

	/**
	 * Merger should create a facet for this cluster.
	 * @param facetDefinitionName FacetDefinitionName
	 * @return this builder
	 */
	public FacetedQueryResultMerger<R, S> withFacet(final String facetDefinitionName) {
		Assertion.checkArgNotEmpty(facetDefinitionName);
		//-----
		this.facetDefinitionNameOpt = Optional.of(facetDefinitionName);
		return this;
	}

	/**
	 * Merge data.
	 * @return get merged result as a new result
	 */
	public FacetedQueryResult<R, S> toFacetedQueryResult() {
		Assertion.checkArgument(otherResults.size() > 0, "You need at least one FacetedQueryResult in order to build a FacetedQueryResult");
		//On accepte de ne pas avoir de FacetedQueryResults pour les cas ou les resultats sont filtrés par la sécurité, certains éléments à merger sont peut-être absent.

		//-----
		long totalCount = 0;
		final Map<FacetValue, DtList<R>> clustersDtc = new LinkedHashMap<>(otherResults.size());
		final Map<FacetValue, Long> clustersCount = new LinkedHashMap<>(otherResults.size());
		final List<Facet> facets = new ArrayList<>();
		final Map<R, Map<DtField, String>> highlights = Collections.emptyMap();

		for (final Entry<FacetValue, List<FacetedQueryResult<?, S>>> otherResult : otherResults.entrySet()) {
			long clusterCount = 0;
			//merge count
			for (final FacetedQueryResult<?, S> result : otherResult.getValue()) {
				clusterCount += result.getCount();
			}
			//cluster result
			final DtList clusterDtList = new DtList(otherResult.getValue().get(0).getDtList().getDefinition());
			for (final FacetedQueryResult<?, S> result : otherResult.getValue()) {
				clusterDtList.addAll(result.getDtList());
			}
			clustersDtc.put(otherResult.getKey(), clusterDtList);
			clustersCount.put(otherResult.getKey(), clusterCount);
			totalCount += clusterCount;
			//TODO merge facets
			//TODO merge highlights
		}

		//On garde les infos qui sont basés sur le premier élément
		final Optional<FacetedQuery> facetedQueryOpt = firstResult.getFacetedQuery();
		final DtList<R> results = new DtList<>(firstResult.getDtList().getDefinition()); //faux : le type de la liste est incorrect, mais heureusement elle est vide.
		final S source = firstResult.getSource();

		final Optional<FacetDefinition> clusterFacetDefinitionOpt;
		if (facetDefinitionNameOpt.isPresent()) {
			final FacetDefinition clusterFacetDefinition = FacetDefinition.createFacetDefinitionByTerm(
					facetDefinitionNameOpt.get(),
					results.getDefinition().getFields().get(0),
					MessageText.of("cluster"),
					false,
					FacetOrder.definition);
			final Facet clusterFacet = new Facet(clusterFacetDefinition, clustersCount);
			facets.add(clusterFacet);
			clusterFacetDefinitionOpt = Optional.of(clusterFacetDefinition);
		} else {
			clusterFacetDefinitionOpt = Optional.empty();
		}
		return new FacetedQueryResult<>(
				facetedQueryOpt,
				totalCount,
				results,
				facets,
				clusterFacetDefinitionOpt,
				clustersDtc,
				highlights,
				source);
	}
}
