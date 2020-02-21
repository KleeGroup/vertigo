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
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertigo.app.Home;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.SelectedFacetValues;
import io.vertigo.dynamo.collections.model.SelectedFacetValues.SelectedFacetValuesBuilder;

public final class SelectedFacetValuesDeserializer implements JsonDeserializer<SelectedFacetValues> {
	private static final String EMPTY_TERM = "_empty_";

	@Override
	public SelectedFacetValues deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final SelectedFacetValuesBuilder selectedFacetValuesBuilder = SelectedFacetValues.empty();

		final JsonObject jsonObject = json.getAsJsonObject();
		for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			final FacetDefinition facetDefinition = Home.getApp().getDefinitionSpace().resolve(entry.getKey(), FacetDefinition.class);
			if (facetDefinition.isRangeFacet()) {
				appendRangeFacetValues(entry.getValue(), facetDefinition, selectedFacetValuesBuilder);
			} else {
				appendTermFacetValues(entry.getValue(), facetDefinition, selectedFacetValuesBuilder);
			}
		}
		return selectedFacetValuesBuilder.build();
	}

	private static void appendRangeFacetValues(final JsonElement value, final FacetDefinition facetDefinition, final SelectedFacetValuesBuilder selectedFacetValuesBuilder) {
		if (value.isJsonArray()) {
			for (final JsonElement label : value.getAsJsonArray()) {
				appendRangeFacetValue(label, facetDefinition, selectedFacetValuesBuilder);
			}
		} else {
			appendRangeFacetValue(value, facetDefinition, selectedFacetValuesBuilder);
		}
	}

	private static void appendRangeFacetValue(final JsonElement label, final FacetDefinition facetDefinition, final SelectedFacetValuesBuilder selectedFacetValuesBuilder) {
		for (final FacetValue facet : facetDefinition.getFacetRanges()) {
			if (facet.getLabel().getDisplay().equals(label.getAsString())
					|| facet.getCode().equals(label.getAsString())) {
				selectedFacetValuesBuilder.add(facetDefinition, facet);
				break;
			}
		}
	}

	private static void appendTermFacetValues(final JsonElement value, final FacetDefinition facetDefinition, final SelectedFacetValuesBuilder selectedFacetValuesBuilder) {
		if (value.isJsonArray()) {
			for (final JsonElement term : value.getAsJsonArray()) {
				appendTermFacetValue(term, facetDefinition, selectedFacetValuesBuilder);
			}
		} else {
			appendTermFacetValue(value, facetDefinition, selectedFacetValuesBuilder);
		}
	}

	private static void appendTermFacetValue(final JsonElement value, final FacetDefinition facetDefinition, final SelectedFacetValuesBuilder selectedFacetValuesBuilder) {
		final String code = value.getAsString();
		final String valueAsString;
		final String query;
		if (EMPTY_TERM.equals(code)) {
			valueAsString = "";
		} else {
			valueAsString = code;
		}
		if (valueAsString != null) {
			query = facetDefinition.getDtField().getName() + ":\"" + valueAsString + "\"";
		} else {
			query = "!_exists_:" + facetDefinition.getDtField().getName(); //only for null value, empty ones use FIELD:""
		}
		final FacetValue facetValue = new FacetValue(code, ListFilter.of(query), MessageText.of(code));
		selectedFacetValuesBuilder.add(facetDefinition, facetValue);
	}
}
