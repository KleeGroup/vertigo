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
package io.vertigo.dynamo.plugins.environment.registries.search;

import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.Boolean;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.String;

import java.util.List;

import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
final class SearchGrammar implements DslGrammar {
	/** Index definition. */
	public static final DslEntity INDEX_DEFINITION_ENTITY;
	/** Facet definition. */
	public static final DslEntity FACET_DEFINITION_ENTITY;
	/** Faceted query definition. */
	public static final DslEntity FACETED_QUERY_DEFINITION_ENTITY;

	/** Search loader id. */
	public static final String SEARCH_LOADER_PROPERTY = "LOADER_ID";
	/** List filter class. */
	public static final String LIST_FILTER_BUILDER_CLASS = "LIST_FILTER_BUILDER_CLASS";
	/** List filter query. */
	public static final String LIST_FILTER_BUILDER_QUERY = "LIST_FILTER_BUILDER_QUERY";
	/** Fieldname. */
	public static final String FIELD_NAME = "FIELD_NAME";
	/** Facet order. */
	public static final String FACET_ORDER = "ORDER";

	/** MultiSelectable values. */
	public static final String FACET_MULTISELECTABLE = "multiSelectable";
	/** Range filter. */
	public static final String RANGE_FILTER_PROPERTY = "FILTER";
	/** indexCopy to. */
	public static final String INDEX_COPY_TO_PROPERTY = "indexCopyTo";
	/** indexCopy from. */
	public static final String INDEX_COPY_FROM_PROPERTY = "FROM";

	/*
	 * create IndexDefinition IDX_TEST {
	    keyConcept : DT_TEST,
	    dtResult : DT_TEST,
	    dtIndex : DT_TEST,
	    indexCopyTo FIELD_TO_1 : { from: "FIELD_FROM_1,FIELD_FROM_2" }, //use field formatters
	    indexCopyTo FIELD_TO_2 : { from: "FIELD_FROM_3" }, //use field formatters
	
	    searchLoader : com.project.domain.search.dao.SearchLoaderPeople
	}
	
	create FacetDefinition FCT_MOVIE_GENRE {
		dtDefinition : DT_TEST, fieldName : "GENRE", label : "Par genre"
	}
	
	create FacetDefinition FCT_MOVIE_ANNEE {
		dtDefinition : DT_TEST, fieldName : "YEAR", label : "Par année",
	 	range R1 { filter : "YEAR:[* TO 2000]", label : "avant 2000"}, //TODO : fieldName in filter too ?
	 	range R2 { filter : "YEAR:[2000 TO 2005]", label : "2000-2005"},
	 	range R3 { filter : "YEAR:[2005 TO *]", label : "après 2005"}
	}
	
	create FacetedQueryDefinition QRY_MOVIE {
		facet FCT_MOVIE_GENRE,
		facet FCT_MOVIE_ANNEE,
	}
	*/

	static {
		/** Index copy fields. */
		final DslEntity indexCopyEntity = DslEntity.builder("indexCopyTo")
				.addRequiredField(INDEX_COPY_FROM_PROPERTY, String)
				.build();

		INDEX_DEFINITION_ENTITY = DslEntity.builder("IndexDefinition")
				.addRequiredField("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY.getLink())
				.addRequiredField("dtIndex", DomainGrammar.DT_DEFINITION_ENTITY.getLink())
				.addManyFields(INDEX_COPY_TO_PROPERTY, indexCopyEntity)
				.addRequiredField(SEARCH_LOADER_PROPERTY, String)
				.build();

		/** Facet range. */
		final DslEntity facetRangeEntity = DslEntity.builder("range")
				.addRequiredField(RANGE_FILTER_PROPERTY, String)
				.addRequiredField(KspProperty.LABEL, String)
				.build();

		FACET_DEFINITION_ENTITY = DslEntity.builder("FacetDefinition")
				.addRequiredField("dtDefinition", DomainGrammar.DT_DEFINITION_ENTITY.getLink())
				.addRequiredField(FIELD_NAME, String)
				.addRequiredField(KspProperty.LABEL, String)
				.addOptionalField(FACET_MULTISELECTABLE, Boolean) //facultative, default to false
				.addOptionalField(FACET_ORDER, String) //facultative, default to count
				.addManyFields("range", facetRangeEntity)
				.build();

		FACETED_QUERY_DEFINITION_ENTITY = DslEntity.builder("FacetedQueryDefinition")
				.addRequiredField("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY.getLink())
				.addRequiredField("domainCriteria", DomainGrammar.DOMAIN_ENTITY.getLink())
				.addRequiredField(LIST_FILTER_BUILDER_CLASS, String)
				.addRequiredField(LIST_FILTER_BUILDER_QUERY, String)
				.addManyFields("facets", FACET_DEFINITION_ENTITY.getLink())
				.build();
	}

	@Override
	public List<DslEntity> getEntities() {
		return new ListBuilder<DslEntity>()
				.add(INDEX_DEFINITION_ENTITY)
				.add(FACET_DEFINITION_ENTITY)
				.add(FACETED_QUERY_DEFINITION_ENTITY)
				.unmodifiable()
				.build();
	}
}
