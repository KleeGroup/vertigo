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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Selection de Facette.
 * @author pchretien, npiedeloup
 */
public final class SelectedFacetValues implements Serializable {
	private static final long serialVersionUID = 6031231575451388380L;
	private final Map<String, List<FacetValue>> selectedFacetValuesByFacetName;

	public static SelectedFacetValuesBuilder of(final SelectedFacetValues otherSelectedFacetValues) {
		return new SelectedFacetValuesBuilder(otherSelectedFacetValues.selectedFacetValuesByFacetName);
	}

	public static SelectedFacetValuesBuilder empty() {
		return new SelectedFacetValuesBuilder(new HashMap<>());
	}

	public static final class SelectedFacetValuesBuilder implements Builder<SelectedFacetValues> {
		private final Map<String, List<FacetValue>> selectedFacetValuesByFacetName = new HashMap<>();

		private SelectedFacetValuesBuilder() {
			//for empty builder
		}

		private SelectedFacetValuesBuilder(final Map<String, List<FacetValue>> selectedFacetValues) {
			Assertion.checkNotNull(selectedFacetValues);
			//----
			selectedFacetValues //on fait un clone (Map et List)
					.forEach((k, v) -> selectedFacetValuesByFacetName.put(k, new ArrayList<>(v)));
		}

		/**
		 * @param definition Facet definition
		 * @param facetFilter Facet filter to add
		 * @return this SelectedFacetValues
		 */
		public SelectedFacetValuesBuilder add(final FacetDefinition definition, final FacetValue facetFilter) {
			selectedFacetValuesByFacetName.computeIfAbsent(definition.getName(), k -> new ArrayList<>())
					.add(facetFilter);
			return this;
		}

		/** {@inheritDoc} */
		@Override
		public SelectedFacetValues build() {
			return new SelectedFacetValues(selectedFacetValuesByFacetName);
		}
	}

	/**
	 * Constructor.
	 * @param selectedFacetValues Liste des valeurs de facette par facette
	 */
	private SelectedFacetValues(final Map<String, List<FacetValue>> selectedFacetValues) {
		Assertion.checkNotNull(selectedFacetValues);
		//----
		selectedFacetValuesByFacetName = Collections.unmodifiableMap(selectedFacetValues);
	}

	/**
	 * Valeurs des facettes selectionnées. (Code Facet -> Liste des valeurs selectionnées)
	 * @param facetDefinition Facet
	 * @return Liste des valeurs de facette selectionnées
	 */
	public List<FacetValue> getFacetValues(final String facetDefinitionName) {
		Assertion.checkArgNotEmpty(facetDefinitionName);
		//---
		return selectedFacetValuesByFacetName.getOrDefault(facetDefinitionName, Collections.emptyList());
	}

}
