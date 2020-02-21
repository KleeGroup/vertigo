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
package io.vertigo.dynamo.plugins.collections.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;

/**
 * Implémentation Ram de l'index Lucene.
 * Il existe une seule instance par JVM.
 * Il ne doit aussi exister qu'un seul writer.
 *
 * @author  pchretien, npiedeloup
 * @param <D> Type d'objet
 */
final class RamLuceneIndex<D extends DtObject> {
	/** Prefix for a created field use for sorting. */
	static final String SORT_FIELD_PREFIX = "4SORT_";

	//DtDefinition est non serializable
	private final DtDefinition dtDefinition;
	private final Map<String, D> indexedObjectPerPk = new HashMap<>();
	private final Directory directory;

	private final Analyzer indexAnalyser;
	private final RamLuceneQueryFactory luceneQueryFactory;

	/**
	 * @param dtDefinition DtDefinition des objets indexés
	 * @throws IOException Exception I/O
	 */
	RamLuceneIndex(final DtDefinition dtDefinition) throws IOException {
		Assertion.checkNotNull(dtDefinition);
		//-----
		indexAnalyser = new DefaultAnalyzer(false); //les stop word marchent mal si asymétrique entre l'indexation et la query
		luceneQueryFactory = new RamLuceneQueryFactory(indexAnalyser);
		this.dtDefinition = dtDefinition;
		directory = new RAMDirectory();

		//l'index est crée automatiquement la premiere fois.
		buildIndex();
	}

	private void buildIndex() throws IOException {
		try (final IndexWriter indexWriter = createIndexWriter()) {
			// we are creating an empty index if it does not exist
		}
	}

	private IndexWriter createIndexWriter() throws IOException {
		final IndexWriterConfig config = new IndexWriterConfig(indexAnalyser); //sur une implé mémoire on peut utiliser la dernière version
		return new IndexWriter(directory, config);
	}

	/**
	 * @param id Clé de l'objet
	 * @return Objet associé dans cet index.
	 */
	private D getDtObjectIndexed(final String id) {
		return indexedObjectPerPk.get(id);
	}

	/**
	 * Associe une clé à un objet.
	 * @param pkValue Valeur de la clé
	 * @param dto Objet associé
	 */
	private void mapDocument(final String pkValue, final D dto) {
		indexedObjectPerPk.put(pkValue, dto);
	}

	private DtList<D> executeQuery(
			final Query query,
			final int skip,
			final int top,
			final Optional<Sort> optSort) throws IOException {
		try (final IndexReader indexReader = DirectoryReader.open(directory)) {
			final IndexSearcher searcher = new IndexSearcher(indexReader);
			//1. Exécution des la Requête
			final TopDocs topDocs;
			if (optSort.isPresent()) {
				topDocs = searcher.search(query, skip + top, optSort.get());
			} else {
				topDocs = searcher.search(query, skip + top);
			}
			//2. Traduction du résultat Lucene en une Collection
			return translateDocs(searcher, topDocs, skip, top);
		} catch (final TooManyClauses e) {
			throw (VUserException) new VUserException(Resources.DYNAMO_COLLECTIONS_INDEXER_TOO_MANY_CLAUSES).initCause(e);
		}
	}

	private DtList<D> translateDocs(
			final IndexSearcher searcher,
			final TopDocs topDocs,
			final int skip,
			final int top) throws IOException {
		final DtField idField = dtDefinition.getIdField().get();

		final DtList<D> dtcResult = new DtList<>(dtDefinition);
		final int resultLength = topDocs.scoreDocs.length;
		if (resultLength > skip) {
			for (int i = skip; i < Math.min(skip + top, resultLength); i++) {
				final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				final Document document = searcher.doc(scoreDoc.doc);
				dtcResult.add(getDtObjectIndexed(document.get(idField.getName())));
			}
		}
		return dtcResult;
	}

	/**
	 * Add element to index.
	 * @param fullDtc Full Dtc to index
	 * @param storeValue if data are store in index
	 * @throws IOException Indexation error
	 */
	public void addAll(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.checkNotNull(fullDtc);
		//-----
		try (final IndexWriter indexWriter = createIndexWriter()) {
			final DtField idField = fullDtc.getDefinition().getIdField().get();
			final Collection<DtField> dtFields = fullDtc.getDefinition().getFields();

			for (final D dto : fullDtc) {
				final Document document = new Document();
				final Object pkValue = idField.getDataAccessor().getValue(dto);
				Assertion.checkNotNull(pkValue, "Indexed DtObject must have a not null primary key. {0}.{1} was null.", fullDtc.getDefinition().getName(), idField.getName());
				final String indexedPkValue = String.valueOf(pkValue);
				addKeyword(document, idField.getName(), indexedPkValue, true);
				for (final DtField dtField : dtFields) {
					final Object value = dtField.getDataAccessor().getValue(dto);
					if (value != null && !dtField.equals(idField)) {
						if (value instanceof String) {
							final String valueAsString = getStringValue(dto, dtField);
							addIndexed(document, dtField.getName(), valueAsString, storeValue);
						} else if (value instanceof Date) {
							final String valueAsString = DateTools.dateToString((Date) value, DateTools.Resolution.DAY);
							addKeyword(document, dtField.getName(), valueAsString, storeValue);
						} else {
							addKeyword(document, dtField.getName(), value.toString(), storeValue);
						}
					}
				}
				indexWriter.addDocument(document);
				mapDocument(indexedPkValue, dto);
			}
		}
	}

	private static StoreManager getStoreManager() {
		return Home.getApp().getComponentSpace().resolve(StoreManager.class);
	}

	private static String getStringValue(final DtObject dto, final DtField field) {
		final String stringValue;
		final Object value = field.getDataAccessor().getValue(dto);
		if (value != null) {
			if (field.getType() == DtField.FieldType.FOREIGN_KEY && getStoreManager().getMasterDataConfig().containsMasterData(field.getFkDtDefinition())) {
				//TODO voir pour mise en cache de cette navigation
				final DtListURIForMasterData mdlUri = getStoreManager().getMasterDataConfig().getDtListURIForMasterData(field.getFkDtDefinition());
				final DtField displayField = mdlUri.getDtDefinition().getDisplayField().get();
				final UID<Entity> uid = UID.of(field.getFkDtDefinition(), value);
				final DtObject fkDto = getStoreManager().getDataStore().readOne(uid);
				final Object displayValue = displayField.getDataAccessor().getValue(fkDto);
				stringValue = displayField.getDomain().valueToString(displayValue);
			} else {
				stringValue = String.valueOf(field.getDataAccessor().getValue(dto));
			}
			return stringValue.trim();
		}
		return null;
	}

	/**
	 * Querying index.
	 * @param keywords Keywords
	 * @param searchedFields Searched field list
	 * @param listFilters Added filters
	 * @param dtListState Sort and page list state
	 * @param boostedField Field use for boosting score
	 * @return Filtered ordered list
	 * @throws IOException Query error
	 */
	public DtList<D> getCollection(
			final String keywords,
			final Collection<DtField> searchedFields,
			final List<ListFilter> listFilters,
			final DtListState dtListState,
			final Optional<DtField> boostedField) throws IOException {
		Assertion.checkNotNull(searchedFields);
		Assertion.checkNotNull(dtListState);
		Assertion.checkArgument(dtListState.getMaxRows().isPresent(), "MaxRows is mandatory, can't get all data :(");
		//-----
		final Query filterQuery = luceneQueryFactory.createFilterQuery(keywords, searchedFields, listFilters, boostedField);
		final Optional<Sort> optSort = createSort(dtListState);
		return executeQuery(filterQuery, dtListState.getSkipRows(), dtListState.getMaxRows().get(), optSort);
	}

	private static void addKeyword(
			final Document document,
			final String fieldName,
			final String fieldValue,
			final boolean storeValue) {
		final IndexableField keywordField = new StringField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
		final IndexableField sortedDocValuesField = new SortedDocValuesField(fieldName, new BytesRef(fieldValue));
		document.add(keywordField);
		document.add(sortedDocValuesField);
	}

	private static void addIndexed(
			final Document document,
			final String fieldName,
			final String fieldValue,
			final boolean storeValue) {
		final IndexableField textField = new TextField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
		final IndexableField sortedDocValuesField = new SortedDocValuesField(fieldName, new BytesRef(fieldValue.toLowerCase(Locale.ROOT)));
		document.add(textField);
		document.add(sortedDocValuesField);
	}

	private static Optional<Sort> createSort(final DtListState dtListState) {
		if (dtListState.getSortFieldName().isPresent()) {
			final String sortFieldName = dtListState.getSortFieldName().get();
			final boolean sortDesc = dtListState.isSortDesc().get();
			final SortField.Type luceneType = SortField.Type.STRING; //TODO : check if other type are necessary
			final SortField sortField = new SortField(sortFieldName, luceneType, sortDesc);
			sortField.setMissingValue(SortField.STRING_LAST);
			return Optional.of(new Sort(sortField));
		}
		return Optional.empty();
	}
}
