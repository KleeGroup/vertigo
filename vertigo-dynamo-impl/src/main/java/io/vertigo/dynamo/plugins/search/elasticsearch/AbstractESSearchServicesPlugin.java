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
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.health.HealthChecked;
import io.vertigo.commons.analytics.health.HealthMeasure;
import io.vertigo.commons.analytics.health.HealthMeasureBuilder;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.impl.search.SearchServicesPlugin;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas, npiedeloup
 */
public abstract class AbstractESSearchServicesPlugin implements SearchServicesPlugin, Activeable {
	private static final int DEFAULT_SCALING_FACTOR = 1000;
	private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy||strict_date_optional_time||epoch_second";
	private static final int OPTIMIZE_MAX_NUM_SEGMENT = 32;
	/** field suffix for keyword fields added by this plugin. */
	public static final String SUFFIX_SORT_FIELD = ".keyword";

	private static final Logger LOGGER = LogManager.getLogger(AbstractESSearchServicesPlugin.class);
	private final ESDocumentCodec elasticDocumentCodec;

	private Client esClient;
	private final DtListState defaultListState;
	private final int defaultMaxRows;
	private final String indexNameOrPrefix;
	private final boolean indexNameIsPrefix;
	private final Set<String> types = new HashSet<>();
	private final URL configFileUrl;
	private boolean indexSettingsValid;

	/**
	 * Constructor.
	 * @param indexNameOrPrefix ES index name
	 * @param indexNameIsPrefix indexName use as prefix
	 * @param defaultMaxRows Nombre de lignes
	 * @param codecManager Manager de codec
	 * @param configFile Fichier de configuration des indexs
	 * @param resourceManager Manager des resources
	 */
	protected AbstractESSearchServicesPlugin(
			final String indexNameOrPrefix,
			final boolean indexNameIsPrefix,
			final int defaultMaxRows,
			final String configFile,
			final CodecManager codecManager,
			final ResourceManager resourceManager) {
		Assertion.checkArgNotEmpty(indexNameOrPrefix);
		Assertion.checkNotNull(codecManager);
		//Assertion.when(indexNameIsPrefix).check(() -> indexNameOrPrefix.endsWith("_"), "When envIndex is use as prefix, it must ends with _ (current : {0})", indexNameOrPrefix);
		//Assertion.when(!indexNameIsPrefix).check(() -> !indexNameOrPrefix.endsWith("_"), "When envIndex isn't declared as prefix, it can't ends with _ (current : {0})", indexNameOrPrefix);
		//-----
		this.defaultMaxRows = defaultMaxRows;
		defaultListState = DtListState.of(defaultMaxRows);
		elasticDocumentCodec = new ESDocumentCodec(codecManager);
		//------
		this.indexNameOrPrefix = indexNameOrPrefix;
		this.indexNameIsPrefix = indexNameIsPrefix;
		configFileUrl = resourceManager.resolve(configFile);
	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		//Init ElasticSearch Client
		esClient = createClient();
		indexSettingsValid = true;
		//must wait yellow status to be sure prepareExists works fine (instead of returning false on a already exist index)
		waitForYellowStatus();
		//Init typeMapping IndexDefinition <-> Conf ElasticSearch
		for (final SearchIndexDefinition indexDefinition : Home.getApp().getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			final String myIndexName = obtainIndexName(indexDefinition);
			createIndex(myIndexName);

			updateTypeMapping(indexDefinition, hasSortableNormalizer(myIndexName));
			logMappings(myIndexName);
			types.add(indexDefinition.getName());
		}

		waitForYellowStatus();
	}

	private boolean hasSortableNormalizer(final String myIndexName) {
		try {
			final Settings currentSettings = esClient.admin()
					.indices()
					.prepareGetIndex()
					.addIndices(myIndexName)
					.get()
					.getSettings()
					.get(myIndexName);
			return !currentSettings.getAsSettings("index.analysis.normalizer.sortable").isEmpty();
		} catch (final ElasticsearchException e) {
			throw WrappedException.wrap(e, "Error on index {0}", myIndexName);
		}
	}

	private String obtainIndexName(final SearchIndexDefinition indexDefinition) {
		return StringUtil.camelToConstCase(indexNameIsPrefix ? indexNameOrPrefix + indexDefinition.getName() : indexNameOrPrefix).toLowerCase(Locale.ROOT);
	}

	private void createIndex(final String myIndexName) {
		try {
			if (!esClient.admin().indices().prepareExists(myIndexName).get().isExists()) {
				if (configFileUrl == null) {
					esClient.admin().indices().prepareCreate(myIndexName).get();
				} else {
					try (InputStream is = configFileUrl.openStream()) {
						final Settings settings = Settings.builder().loadFromStream(configFileUrl.getFile(), is, false).build();
						esClient.admin().indices().prepareCreate(myIndexName).setSettings(settings).get();
					}
				}
			} else if (configFileUrl != null) {
				// If we use local config file, we check config against ES server
				try (InputStream is = configFileUrl.openStream()) {
					final Settings settings = Settings.builder().loadFromStream(configFileUrl.getFile(), is, false).build();
					indexSettingsValid = indexSettingsValid && !isIndexSettingsDirty(myIndexName, settings);
				}
			}
		} catch (final ElasticsearchException | IOException e) {
			throw WrappedException.wrap(e, "Error on index {0}", myIndexName);
		}
	}

	private boolean isIndexSettingsDirty(final String myIndexName, final Settings settings) {
		final Settings currentSettings = esClient.admin()
				.indices()
				.prepareGetIndex()
				.addIndices(myIndexName)
				.get()
				.getSettings()
				.get(myIndexName);
		boolean indexSettingsDirty = false;
		final Set<String> settingsNames = settings.keySet();
		for (final String settingsName : settingsNames) {
			final String currentValue = currentSettings.get(settingsName);
			if (currentValue == null) {
				indexSettingsDirty = true;
				break;
			}
			final Object expectedValue = settings.get(settingsName);
			if (!currentValue.equals(expectedValue)) {
				indexSettingsDirty = true;
				LOGGER.warn("[{}] {} :  current={}, expected= {}", myIndexName, settingsName, currentValue, expectedValue);
				break;
			}
		}
		return indexSettingsDirty;
	}

	private void logMappings(final String myIndexName) {
		final IndicesAdminClient indicesAdmin = esClient.admin().indices();
		final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = indicesAdmin.prepareGetMappings(myIndexName).get().getMappings();
		for (final ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> indexMapping : indexMappings) {
			LOGGER.info("Index {} CurrentMapping:", indexMapping.key);
			for (final ObjectObjectCursor<String, MappingMetaData> dtoMapping : indexMapping.value) {
				LOGGER.info(" {} -> {}", dtoMapping.key, dtoMapping.value.source());
			}
		}
	}

	/**
	 * @return ElasticSearch client.
	 */
	protected abstract Client createClient();

	/**
	 * Close created client.
	 */
	protected abstract void closeClient();

	/** {@inheritDoc} */
	@Override
	public final void stop() {
		closeClient();
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept, I extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I>> indexCollection) {
		Assertion.checkNotNull(indexCollection);
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.putAll(indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept, I extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(index);
		Assertion.checkArgument(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.put(index);
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept> void remove(final SearchIndexDefinition indexDefinition, final UID<S> uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		//-----
		createElasticStatement(indexDefinition).remove(uri);
		markToOptimize(obtainIndexName(indexDefinition));
	}

	/** {@inheritDoc} */
	@Override
	public final <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final ESStatement<KeyConcept, R> statement = createElasticStatement(indexDefinition);
		final DtListState usedListState = listState != null ? listState : defaultListState;
		return statement.loadList(indexDefinition, searchQuery, usedListState, defaultMaxRows);
	}

	/** {@inheritDoc} */
	@Override
	public final long count(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		return createElasticStatement(indexDefinition).count();
	}

	/** {@inheritDoc} */
	@Override
	public final void remove(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(listFilter);
		//-----
		createElasticStatement(indexDefinition).remove(listFilter);
		markToOptimize(obtainIndexName(indexDefinition));
	}

	private <S extends KeyConcept, I extends DtObject> ESStatement<S, I> createElasticStatement(final SearchIndexDefinition indexDefinition) {
		Assertion.checkArgument(indexSettingsValid,
				"Index settings have changed and are no more compatible, you must recreate your index : stop server, delete your index data folder, restart server and launch indexation job.");
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkArgument(types.contains(indexDefinition.getName()), "Type {0} hasn't been registered (Registered type: {1}).", indexDefinition.getName(), types);
		//-----
		return new ESStatement<>(elasticDocumentCodec, obtainIndexName(indexDefinition), indexDefinition.getName(), esClient);
	}

	private static String obtainPkIndexDataType(final Domain domain) {
		// On peut préciser pour chaque domaine le type d'indexation
		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean:
			case Double:
			case Integer:
			case Long:
				return domain.getDataType().name().toLowerCase(Locale.ROOT);
			case String:
				return "keyword";
			case LocalDate:
			case Instant:
			case BigDecimal:
			case DataStream:
			default:
				throw new IllegalArgumentException("Type de donnée non pris en charge comme PK pour le keyconcept indexé [" + domain + "].");
		}
	}

	/**
	 * Update template definition of this type.
	 * @param indexDefinition Index concerné
	 */
	private void updateTypeMapping(final SearchIndexDefinition indexDefinition, final boolean sortableNormalizer) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		try (final XContentBuilder typeMapping = XContentFactory.jsonBuilder()) {
			typeMapping.startObject()
					.startObject("properties")
					.startObject(ESDocumentCodec.FULL_RESULT)
					.field("type", "binary")
					.endObject();

			typeMapping.startObject(ESDocumentCodec.DOC_ID)
					.field("type", obtainPkIndexDataType(indexDefinition.getKeyConceptDtDefinition().getIdField().get().getDomain()))
					.endObject();

			/* 3 : Les champs du dto index */
			final Set<DtField> copyFromFields = indexDefinition.getIndexCopyFromFields();
			final DtDefinition indexDtDefinition = indexDefinition.getIndexDtDefinition();
			for (final DtField dtField : indexDtDefinition.getFields()) {
				final IndexType indexType = IndexType.readIndexType(dtField.getDomain());
				typeMapping.startObject(dtField.getName());
				appendIndexTypeMapping(typeMapping, indexType);
				if (copyFromFields.contains(dtField)) {
					appendIndexCopyToMapping(indexDefinition, typeMapping, dtField);
				}
				if (indexType.isIndexSubKeyword()) {
					typeMapping.startObject("fields");
					typeMapping.startObject("keyword");
					typeMapping.field("type", "keyword");
					if (sortableNormalizer) {
						typeMapping.field("normalizer", "sortable");
					}
					typeMapping.endObject();
					typeMapping.endObject();
				}
				if (indexType.isIndexFieldData()) {
					typeMapping.field("fielddata", true);
				}
				typeMapping.endObject();
			}
			typeMapping.endObject().endObject(); //end properties

			final AcknowledgedResponse putMappingResponse = esClient.admin()
					.indices()
					.preparePutMapping(obtainIndexName(indexDefinition))
					.setType(indexDefinition.getName())
					.setSource(typeMapping)
					.get();
			putMappingResponse.isAcknowledged();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Serveur ElasticSearch indisponible");
		}
	}

	private static void appendIndexCopyToMapping(final SearchIndexDefinition indexDefinition, final XContentBuilder typeMapping, final DtField dtField) throws IOException {
		final List<DtField> copyToFields = indexDefinition.getIndexCopyToFields(dtField);
		if (copyToFields.size() == 1) {
			typeMapping.field("copy_to", copyToFields.get(0).getName());
		} else {
			final String[] copyToFieldNames = new String[copyToFields.size()];
			for (int i = 0; i < copyToFieldNames.length; i++) {
				copyToFieldNames[i] = copyToFields.get(i).getName();
			}
			typeMapping.field("copy_to", copyToFieldNames);
		}
	}

	private static void appendIndexTypeMapping(final XContentBuilder typeMapping, final IndexType indexType) throws IOException {
		typeMapping.field("type", indexType.getIndexDataType());
		if (indexType.getIndexAnalyzer().isPresent()) {
			typeMapping.field("keyword".equals(indexType.getIndexDataType()) ? "normalizer" : "analyzer", indexType.getIndexAnalyzer().get());
		}
		if ("scaled_float".equals(indexType.getIndexDataType())) {
			typeMapping.field("scaling_factor", DEFAULT_SCALING_FACTOR);
		}
		if ("date".equals(indexType.getIndexDataType())) {
			typeMapping.field("format", DEFAULT_DATE_FORMAT);
		}
	}

	private void markToOptimize(final String myIndexName) {
		esClient.admin()
				.indices()
				.prepareForceMerge(myIndexName)
				.setFlush(true)
				.setMaxNumSegments(OPTIMIZE_MAX_NUM_SEGMENT)//32 files : empirique
				.execute()
				.actionGet();
	}

	private void waitForYellowStatus() {
		esClient.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	@HealthChecked(name = "clusterHealth", feature = "search")
	public HealthMeasure checkClusterHealth() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try {
			final ClusterHealthResponse clusterHealthResponse = esClient
					.admin()
					.cluster()
					.health(new ClusterHealthRequestBuilder(esClient, ClusterHealthAction.INSTANCE).request())
					.get();
			switch (clusterHealthResponse.getStatus()) {
				case GREEN:
					healthMeasureBuilder.withGreenStatus();
					break;
				case YELLOW:
					healthMeasureBuilder.withYellowStatus(null, null);
					break;
				case RED:
					healthMeasureBuilder.withRedStatus(null, null);
					break;
				default:
					break;
			}
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();
	}

}
