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
package io.vertigo.vega.engines.webservice.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * JsonSerializer of FacetedQueryResult.
 * {
 *   list = [ { <<indexObject>> }, { <<indexObject>> } , ...],
 *   listType:dtType,
 *   highlight : [ { <<indexFieldsWithHL>> }, { <<indexFieldsWithHL>> }, ...],
 *   facets : [
 *   		{ code:"FCT_ONE", label:"My first facet",
 *				values : [ {code:term1, count:12, label:term1}, {code:term2, count:10, label:term2}, ...] },
 *    		{ code:"FCT_TWO", label:"My second facet",
 *				values : [ {code:term20, count:15, label:term20}, {code:term21, count:8, label:term21}, ...] }
 *	 ],
 *   totalCount : 10045
 * }
 * Or if cluster :
 * {
 * 	 groups : [
 * 			{ code:term1, label:term1,
 *   			list : [ { <<indexObject>> }, { <<indexObject>> } , ...],
 *   			listType:dtType,
 *   			totalCount : 12,
 *   			highlight : [ { <<indexFieldsWithHL>> }, { <<indexFieldsWithHL>> }, ...] *
 *   		},
 *   		{ code:term2, label:term2,
 *   			list : [ { <<indexObject>> }, { <<indexObject>> } , ...],
 *   			listType:dtType,
 *   			totalCount : 53,
 *   			highlight : [ { <<indexFieldsWithHL>> }, { <<indexFieldsWithHL>> }, ...]
 *   		},
 *   ],
 *   facets : [
 *   		{ code:"FCT_ONE", label:"My first facet",
 *				values : [ {code:term1, count:12, label:term1}, {code:term2, count:10, label:term2}, ...] },
 *    		{ code:"FCT_TWO", label:"My second facet",
 *				values : [ {code:term20, count:15, label:term20}, {code:term21, count:8, label:term21}, ...] }
 *	 ],
 *   totalCount : 10045
 * }
 *
 * @author npiedeloup
 */
final class FacetedQueryResultJsonSerializerV4 implements JsonSerializer<FacetedQueryResult<?, ?>> {

	/** {@inheritDoc} */
	@Override
	public JsonElement serialize(final FacetedQueryResult<?, ?> facetedQueryResult, final Type typeOfSrc, final JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();

		//1- add result list as data, with highlight
		if (!facetedQueryResult.getClusterFacetDefinition().isPresent()) {
			final DtList<?> dtList = facetedQueryResult.getDtList();
			final JsonArray jsonList = (JsonArray) context.serialize(dtList);
			jsonObject.add("list", jsonList);
			jsonObject.addProperty("listType", dtList.getDefinition().getClassSimpleName());
			jsonObject.add("highlight", serializeHighLight(dtList, (FacetedQueryResult) facetedQueryResult));
		} else {
			//if it's a cluster add data's cluster
			final JsonArray jsonCluster = new JsonArray();
			for (final Entry<FacetValue, ?> cluster : facetedQueryResult.getClusters().entrySet()) {
				final DtList<?> dtList = (DtList<?>) cluster.getValue();
				if (!dtList.isEmpty()) {
					final JsonArray jsonList = (JsonArray) context.serialize(dtList);
					final JsonObject jsonClusterElement = new JsonObject();
					jsonClusterElement.addProperty("code", cluster.getKey().getCode());
					jsonClusterElement.addProperty("label", cluster.getKey().getLabel().getDisplay());
					jsonClusterElement.add("list", jsonList);
					jsonClusterElement.addProperty("listType", dtList.getDefinition().getClassSimpleName());
					jsonClusterElement.addProperty("totalCount", getFacetCount(cluster.getKey(), facetedQueryResult));
					jsonClusterElement.add("highlight", serializeHighLight(dtList, (FacetedQueryResult) facetedQueryResult));
					jsonCluster.add(jsonClusterElement);
				}
			}
			jsonObject.add("groups", jsonCluster);
		}

		//2- add facet list as facets
		final List<Facet> facets = facetedQueryResult.getFacets();
		final JsonArray jsonFacet = new JsonArray();
		for (final Facet facet : facets) {
			final JsonArray jsonFacetValues = new JsonArray();
			for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
				if (entry.getValue() > 0) {
					final JsonObject jsonFacetValuesElement = new JsonObject();
					jsonFacetValuesElement.addProperty("code", entry.getKey().getCode());
					jsonFacetValuesElement.addProperty("count", entry.getValue());
					jsonFacetValuesElement.addProperty("label", entry.getKey().getLabel().getDisplay());
					jsonFacetValues.add(jsonFacetValuesElement);
				}
			}
			final JsonObject jsonFacetElement = new JsonObject();
			jsonFacetElement.addProperty("code", facet.getDefinition().getName());
			jsonFacetElement.addProperty("label", facet.getDefinition().getLabel().getDisplay());
			jsonFacetElement.add("values", jsonFacetValues);
			jsonFacet.add(jsonFacetElement);
		}
		jsonObject.add("facets", jsonFacet);

		//3 -add totalCount
		jsonObject.addProperty(DtList.TOTAL_COUNT_META, facetedQueryResult.getCount());
		return jsonObject;
	}

	private static Long getFacetCount(final FacetValue key, final FacetedQueryResult<?, ?> facetedQueryResult) {
		final FacetDefinition clusterFacetDefinition = facetedQueryResult.getClusterFacetDefinition().get();
		return facetedQueryResult.getFacets()
				.stream()
				.filter(facet -> clusterFacetDefinition.equals(facet.getDefinition()))
				.flatMap(facet -> facet.getFacetValues().entrySet().stream())
				.filter(facetEntry -> key.getCode().equals(facetEntry.getKey().getCode()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Can't found facet for search cluster"))
				.getValue();
	}

	private static JsonArray serializeHighLight(final DtList<?> dtList, final FacetedQueryResult<DtObject, ?> facetedQueryResult) {
		final JsonArray jsonHighlightList = new JsonArray();
		for (final DtObject document : dtList) {
			final Map<DtField, String> highlights = facetedQueryResult.getHighlights(document);
			final JsonObject jsonHighlight = new JsonObject();
			for (final Map.Entry<DtField, String> entry : highlights.entrySet()) {
				jsonHighlight.addProperty(entry.getKey().getName(), entry.getValue());
			}
			jsonHighlightList.add(jsonHighlight);
		}
		return jsonHighlightList;
	}

}
