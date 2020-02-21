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
package io.vertigo.studio.plugins.mda.search.model;

import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author pchretien
 */
public final class FacetedQueryDefinitionModel {
	private final FacetedQueryDefinition facetedQueryDefinition;

	private final String simpleName;
	private final String criteriaClassCanonicalName;

	public FacetedQueryDefinitionModel(final FacetedQueryDefinition facetedQueryDefinition) {
		Assertion.checkNotNull(facetedQueryDefinition);
		//-----
		this.facetedQueryDefinition = facetedQueryDefinition;
		simpleName = DefinitionUtil.getLocalName(facetedQueryDefinition.getName(), FacetedQueryDefinition.class);
		criteriaClassCanonicalName = obtainCriteriaClassCanonicalName();

	}

	/**
	 * @return Nom local CamelCase de la facetedQueryDefinition
	 */
	public String getSimpleName() {
		return simpleName;
	}

	/**
	 * @return Urn de la facetedQueryDefinition
	 */
	public String getUrn() {
		return facetedQueryDefinition.getName();
	}

	/**
	 * @return Nom de la classe du criteria
	 */
	public String getCriteriaClassCanonicalName() {
		return criteriaClassCanonicalName;
	}

	private String obtainCriteriaClassCanonicalName() {
		final Domain domain = facetedQueryDefinition.getCriteriaDomain();
		Assertion.checkArgument(!domain.isMultiple(), "only not multiple domain are accepted");
		//---
		return DomainUtil.buildJavaType(domain);
	}

}
