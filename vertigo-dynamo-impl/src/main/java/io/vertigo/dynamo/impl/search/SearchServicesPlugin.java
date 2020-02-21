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
package io.vertigo.dynamo.impl.search;

import java.util.Collection;

import io.vertigo.core.component.Plugin;
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
 * Plugin offrant des services de recherche.
 *
 * @author pchretien
 */
public interface SearchServicesPlugin extends Plugin {

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
	 * @param <R> Type de l'objet resultant de la recherche
	 */
	<R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState);

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
	void remove(SearchIndexDefinition indexDefinition, final ListFilter listFilter);
}
