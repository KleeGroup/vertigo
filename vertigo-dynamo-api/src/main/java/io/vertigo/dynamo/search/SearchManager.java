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
package io.vertigo.dynamo.search;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;

/**
 * Gestionnaire des indexes de recherche.
 *
 * @author dchallas, npiedeloup
 */
public interface SearchManager extends Manager {

	/**
	 * Find IndexDefinition for a keyConcept. It must be one and only one IndexDefinition.
	 * @param keyConceptClass keyConcept class
	 * @return SearchIndexDefinition for this keyConcept (not null)
	 */
	SearchIndexDefinition findFirstIndexDefinitionByKeyConcept(Class<? extends KeyConcept> keyConceptClass);

	/**
	 * Find IndexDefinition for a keyConcept. It must be one and only one IndexDefinition.
	 * @param keyConceptClass keyConcept class
	 * @return SearchIndexDefinition for this keyConcept (not null)
	 * @deprecated use findFirstIndexDefinitionByKeyConcept instead
	 */
	@Deprecated
	SearchIndexDefinition findIndexDefinitionByKeyConcept(Class<? extends KeyConcept> keyConceptClass);

	/**
	 * Mark an uid list as dirty. Index of these elements will be reindexed.
	 * Reindexation isn't synchrone, strategy is dependant of plugin's parameters.
	 * @param keyConceptUIDs UID of keyConcept marked as dirty.
	 */
	void markAsDirty(List<UID<? extends KeyConcept>> keyConceptUIDs);

	/**
	 * Launch a complete reindexation of an index.
	 * @param indexDefinition Type de l'index
	 * @return Future of number elements indexed
	 */
	Future<Long> reindexAll(SearchIndexDefinition indexDefinition);

	/**
	 * Ajout de plusieurs ressources à l'index.
	 * Si les éléments étaient déjà dans l'index ils sont remplacés.
	 * @param <I> Type de l'objet représentant l'index
	 * @param <K> Type du keyConcept métier indexé
	 * @param indexDefinition Type de l'index
	 * @param indexCollection Liste des objets à pousser dans l'index
	 */
	<K extends KeyConcept, I extends DtObject> void putAll(SearchIndexDefinition indexDefinition, Collection<SearchIndex<K, I>> indexCollection);

	/**
	 * Ajout d'une ressource à l'index.
	 * Si l'élément était déjà dans l'index il est remplacé.
	 * @param <I> Type de l'objet représentant l'index
	 * @param <K> Type du keyConcept métier indexé
	 * @param indexDefinition Type de l'index
	 * @param index Objet à pousser dans l'index
	 */
	<K extends KeyConcept, I extends DtObject> void put(SearchIndexDefinition indexDefinition, SearchIndex<K, I> index);

	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param indexDefinition Type de l'index
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête
	 * @param <I> Type de l'objet resultant de la recherche
	 */
	<I extends DtObject> FacetedQueryResult<I, SearchQuery> loadList(SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState);

	/**
	 * @param indexDefinition  Type de l'index
	 * @return Nombre de document indexés
	 */
	long count(SearchIndexDefinition indexDefinition);

	/**
	 * Suppression d'une ressource de l'index.
	 * @param <K> Type du keyConcept métier indexé
	 * @param indexDefinition Type de l'index
	 * @param uid UID de la ressource à supprimer
	 */
	<K extends KeyConcept> void remove(SearchIndexDefinition indexDefinition, final UID<K> uid);

	/**
	 * Suppression des données correspondant à un filtre.
	 * @param indexDefinition Type de l'index
	 * @param listFilter Filtre des éléments à supprimer
	 */
	void removeAll(SearchIndexDefinition indexDefinition, final ListFilter listFilter);

}
