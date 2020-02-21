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
package io.vertigo.dynamo.plugins.search.elasticsearch;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.impl.search.SearchResource;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.VUserException;
import io.vertigo.lang.WrappedException;

//vérifier
/**
 * Requête physique d'accès à ElasticSearch.
 * Le driver exécute les requêtes de façon synchrone dans le contexte transactionnelle de la ressource.
 * @author pchretien, npiedeloup
 * @param <I> Type de l'objet représentant l'index
 * @param <K> Type du keyConcept métier indexé
 */
final class ESStatement<K extends KeyConcept, I extends DtObject> {

	private static final RefreshPolicy DEFAULT_REFRESH = RefreshPolicy.NONE; //mettre a true pour TU uniquement
	private static final RefreshPolicy BULK_REFRESH = RefreshPolicy.NONE; //mettre a RefreshPolicy.IMMEDIATE pour TU uniquement
	static final String TOPHITS_SUBAGGREAGTION_NAME = "top";
	private static final Logger LOGGER = LogManager.getLogger(ESStatement.class);

	private final String indexName;
	private final String typeName;
	private final Client esClient;
	private final ESDocumentCodec esDocumentCodec;

	/**
	 * Constructor.
	 * @param esDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document
	 * @param indexName Index name
	 * @param typeName Type name in Index
	 * @param esClient Client ElasticSearch.
	 */
	ESStatement(final ESDocumentCodec esDocumentCodec, final String indexName, final String typeName, final Client esClient) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkArgNotEmpty(typeName);
		Assertion.checkNotNull(esDocumentCodec);
		Assertion.checkNotNull(esClient);
		//-----
		this.indexName = indexName;
		this.typeName = typeName;
		this.esClient = esClient;
		this.esDocumentCodec = esDocumentCodec;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<SearchIndex<K, I>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final BulkRequestBuilder bulkRequest = esClient.prepareBulk().setRefreshPolicy(BULK_REFRESH);
			for (final SearchIndex<K, I> index : indexCollection) {
				try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
					bulkRequest.add(esClient.prepareIndex()
							.setIndex(indexName)
							.setType(typeName)
							.setId(index.getUID().urn())
							.setSource(xContentBuilder));
				}
			}
			final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures()) {
				throw new VSystemException("Can't putAll {0} into {1} index.\nCause by {2}", typeName, indexName, bulkResponse.buildFailureMessage());
			}
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	private static void handleIOException(final IOException e) {
		throw WrappedException.wrap(e, "Serveur ElasticSearch indisponible");
	}

	/**
	 * @param index index à insérer
	 */
	void put(final SearchIndex<K, I> index) {
		//Injection spécifique au moteur d'indexation.
		try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
			esClient.prepareIndex().setRefreshPolicy(DEFAULT_REFRESH)
					.setIndex(indexName)
					.setType(typeName)
					.setId(index.getUID().urn())
					.setSource(xContentBuilder)
					.execute() //execute asynchrone
					.actionGet(); //get wait exec
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	/**
	 * Supprime des documents.
	 * @param query Requete de filtrage des documents à supprimer
	 */
	void remove(final ListFilter query) {
		Assertion.checkNotNull(query);
		//-----
		try {
			final QueryBuilder queryBuilder = ESSearchRequestBuilder.translateToQueryBuilder(query);
			final DeleteByQueryRequestBuilder deleteByQueryAction = new DeleteByQueryRequestBuilder(esClient, DeleteByQueryAction.INSTANCE)
					.filter(queryBuilder);
			deleteByQueryAction
					.source()
					.setIndices(indexName)
					.setTypes(typeName);
			final BulkByScrollResponse response = deleteByQueryAction.get();
			final long deleted = response.getDeleted();
			LOGGER.debug("Removed {} elements", deleted);
		} catch (final SearchPhaseExecutionException e) {
			final VUserException vue = new VUserException(SearchResource.DYNAMO_SEARCH_QUERY_SYNTAX_ERROR);
			vue.initCause(e);
			throw vue;
		}
	}

	/**
	 * Supprime un document.
	 * @param uid UID du document à supprimer
	 */
	void remove(final UID uid) {
		Assertion.checkNotNull(uid);
		//-----
		esClient.prepareDelete().setRefreshPolicy(DEFAULT_REFRESH)
				.setIndex(indexName)
				.setType(typeName)
				.setId(uid.urn())
				.execute()
				.actionGet();
	}

	/**
	 * @param indexDefinition Index de recherche
	 * @param searchQuery Mots clés de recherche
	 * @param listState Etat de la liste (tri et pagination)
	 * @param defaultMaxRows Nombre de ligne max par defaut
	 * @return Résultat de la recherche
	 */
	FacetedQueryResult<I, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final SearchRequestBuilder searchRequestBuilder = new ESSearchRequestBuilder(indexName, typeName, esClient)
				.withSearchIndexDefinition(indexDefinition)
				.withSearchQuery(searchQuery)
				.withListState(listState, defaultMaxRows)
				.build();
		LOGGER.info("loadList {}", searchRequestBuilder);
		try {
			final SearchResponse queryResponse = searchRequestBuilder.execute().actionGet();
			return new ESFacetedQueryResultBuilder(esDocumentCodec, indexDefinition, queryResponse, searchQuery)
					.build();
		} catch (final SearchPhaseExecutionException e) {
			final VUserException vue = new VUserException(SearchResource.DYNAMO_SEARCH_QUERY_SYNTAX_ERROR);
			vue.initCause(e);
			throw vue;
		}
	}

	/**
	 * @return Nombre de document indexés
	 */
	public long count() {
		final SearchResponse response = esClient.prepareSearch(indexName)
				.setTypes(typeName)
				.setSize(0) //on cherche juste à compter
				.execute()
				.actionGet();
		return response.getHits().getTotalHits().value;
	}
}
