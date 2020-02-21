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
package io.vertigo.dynamo.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.collections.data.DtDefinitions;
import io.vertigo.dynamo.collections.data.domain.SmartItem;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

/**
 * @author pchretien
 */
//non final, to be overrided for previous lib version
public class CollectionsManagerTest extends AbstractTestCaseJU5 {
	private static final String Ba_aa = "Ba aa";
	private static final String aaa_ba = "aaa ba";
	private static final String bb_aa = "bb aa";
	private DtDefinition dtDefinitionItem;
	@Inject
	private CollectionsManager collectionsManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.withLocales("fr_FR")
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withMemoryCache()
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withLuceneIndex()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/collections/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.collections.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() {
		dtDefinitionItem = DtObjectUtil.findDtDefinition(SmartItem.class);
	}

	@Test
	public void testHeavySort() {
		// final DtList<Item> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		//
		for (int i = 0; i < 50000; i++) {
			final SmartItem mocka = new SmartItem();
			mocka.setLabel(String.valueOf(i % 100));
			dtc.add(mocka);
		}

		final DtList<SmartItem> sortedDtc = collectionsManager.sort(dtc, "label", false);

		nop(sortedDtc);

	}

	@Test
	public void testSort() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "label", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "label", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { null, bb_aa, Ba_aa, aaa_ba }, extractLabels(sortDtc));
	}

	@Test
	public void testNumericSort() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "id", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { Ba_aa, null, aaa_ba, bb_aa }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.sort(dtc, "id", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { bb_aa, aaa_ba, null, Ba_aa }, extractLabels(sortDtc));
	}

	@Test
	public void testFilter() {
		final DtList<SmartItem> result = createItems()
				.stream()
				.filter(Criterions.isEqualTo(DtDefinitions.Fields.label, aaa_ba).toPredicate())
				.collect(VCollectors.toDtList(SmartItem.class));
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterTwoValues() {
		final Predicate<SmartItem> filterA = Criterions.isEqualTo(DtDefinitions.Fields.label, "aaa").toPredicate();
		final Predicate<SmartItem> filterB = Criterions.isEqualTo(DtDefinitions.Fields.id, 13L).toPredicate();

		final DtList<SmartItem> result = createItemsForRangeTest()
				.stream()
				.filter(filterA.and(filterB))
				.collect(VCollectors.toDtList(SmartItem.class));
		Assertions.assertEquals(1, result.size());
	}

	@Test
	public void testFilterFullText() {
		final DtList<SmartItem> result = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.filter("aa", 1000, dtDefinitionItem.getFields())
				.build()
				.apply(createItems());
		Assertions.assertEquals(3, result.size());

	}

	@Test
	public void testFilterFullTextTokenizer() {
		final DtList<SmartItem> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();
		final SmartItem mock1 = new SmartItem();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final SmartItem mock2 = new SmartItem();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assertions.assertTrue(filter(dtc, "agence", 1000, searchedDtFields).size() == 1, "La recherche n'est pas case insensitive");//majuscule/minuscule
		Assertions.assertTrue(filter(dtc, "l'ouest", 1000, searchedDtFields).size() == 1, "La recherche n'est pas plain text");//tokenizer
		Assertions.assertTrue(filter(dtc, "hopital", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas les accents");//accents
		Assertions.assertTrue(filter(dtc, "cava", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas les caractères spéciaux fr (ç)"); //accents fr (ç)
		Assertions.assertTrue(filter(dtc, "apied", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas les caractères spéciaux latin1 (ã)"); //accents autre (ã)
		Assertions.assertTrue(filter(dtc, "apie", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas la recherche par préfix");//prefix
	}

	private List<SmartItem> filter(final DtList<SmartItem> dtc, final String query, final int nbRows, final Collection<DtField> searchedDtFields) {
		return collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.filter(query, nbRows, searchedDtFields)
				.build()
				.apply(dtc);
	}

	@Test
	public void testFilterFullTextElision() {
		final DtList<SmartItem> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();

		final SmartItem mock1 = new SmartItem();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final SmartItem mock2 = new SmartItem();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où àpied");
		dtc.add(mock2);

		Assertions.assertTrue(filter(dtc, "ouest", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas l'elision");
	}

	@Test
	public void testFilterFullTextMultiKeyword() {
		final DtList<SmartItem> dtc = createItems();
		final Collection<DtField> searchedDtFields = dtDefinitionItem.getFields();

		final SmartItem mock1 = new SmartItem();
		mock1.setId(seqId++);
		mock1.setLabel("Agence de l'Ouest");
		dtc.add(mock1);

		final SmartItem mock2 = new SmartItem();
		mock2.setId(seqId++);
		mock2.setLabel("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assertions.assertTrue(filter(dtc, "agence de", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas l'espace");//mots proches
		Assertions.assertTrue(filter(dtc, "hopital accent", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas l'utilisation de plusieurs mots");//mots séparés
		Assertions.assertTrue(filter(dtc, "accent hopital", 1000, searchedDtFields).size() == 1, "La recherche ne supporte pas l'inversion des mots");//inversés
		Assertions.assertTrue(filter(dtc, "agence hopital", 1000, searchedDtFields).size() == 0, "Les mots clés ne sont pas en 'ET'");//multi doc
	}

	/**
	 * Vérifie le comportement quand la recherche en commence par addresse trop de term du dictionnaire.
	 * Par défaut Lucene envoi une erreur TooMany...., le collectionsManager limite aux premiers terms.
	 */
	@Test
	public void testFilterFullTextBigList() {
		final UnaryOperator<DtList<SmartItem>> filterFunction = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.filter("a", 2000, dtDefinitionItem.getFields())
				.build();
		Assertions.assertNotNull(filterFunction);
		final DtList<SmartItem> bigFamillyList = new DtList<>(SmartItem.class);
		for (int i = 0; i < 50000; i++) {
			final SmartItem mocka = new SmartItem();
			mocka.setId(seqId++);
			mocka.setLabel("blabla a" + (char) ('a' + i % 26) + String.valueOf(i % 100));
			bigFamillyList.add(mocka);
		}
		final DtList<SmartItem> result = filterFunction.apply(bigFamillyList);
		Assertions.assertEquals(2000, result.size());
	}

	@Test
	public void testSortWithIndex() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		sortDtc = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.sort("label", false)
				.build()
				.apply(dtc);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));

		// ======================== Descendant
		sortDtc = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.sort("label", true)
				.build()
				.apply(dtc);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { null, bb_aa, Ba_aa, aaa_ba }, extractLabels(sortDtc));
	}

	@Test
	public void testNumericSortWithIndex() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.sort("id", false)
				.build()
				.apply(dtc);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { Ba_aa, null, aaa_ba, bb_aa }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.sort("id", true)
				.build()
				.apply(dtc);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { bb_aa, aaa_ba, null, Ba_aa }, extractLabels(sortDtc));
	}

	@Test
	public void testSubListWithIndex() {
		// on test une implémentation de référence ArrayList
		final List<String> list = Arrays.asList("a", "b");
		Assertions.assertEquals(0, list.subList(0, 0).size());
		Assertions.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assertions.assertEquals(1, list.subList(1, 2).size()); // >1
		Assertions.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		//can't test subList(0,0) : illegal argument
		Assertions.assertEquals(2, subListWithIndex(createItems(), 0, 2).size());
		Assertions.assertEquals(1, subListWithIndex(createItems(), 1, 2).size());
		//can't test subList(2,2) : illegal argument
	}

	private DtList<SmartItem> subListWithIndex(final DtList<SmartItem> dtc, final int start, final int end) {
		return collectionsManager.<SmartItem> createIndexDtListFunctionBuilder()
				.filterSubList(start, end)
				.build()
				.apply(dtc);
	}

	@Test
	public void testSubList() {
		// on test une implémentation de référence ArrayList
		final List<String> list = Arrays.asList("a", "b");
		Assertions.assertEquals(0, list.subList(0, 0).size());
		Assertions.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assertions.assertEquals(1, list.subList(1, 2).size()); // >1
		Assertions.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		Assertions.assertEquals(0, subList(createItems(), 0, 0).size());
		Assertions.assertEquals(2, subList(createItems(), 0, 2).size());
		Assertions.assertEquals(1, subList(createItems(), 1, 2).size());
		Assertions.assertEquals(0, subList(createItems(), 2, 2).size());
	}

	private DtList<SmartItem> subList(final DtList<SmartItem> dtc, final int start, final int end) {
		return dtc
				.stream()
				.skip(start)
				.limit(end - start)
				.collect(VCollectors.toDtList(dtc.getDefinition()));
	}

	/**
	 * combiner sort/filter ; filter/sort ; sublist/sort ; filter/sublist.
	 *
	 */
	@Test
	public void testChainFilterSortSubList() {

		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		final Predicate<SmartItem> predicate = Criterions.isEqualTo(DtDefinitions.Fields.label, aaa_ba).toPredicate();
		final Function<DtList<SmartItem>, DtList<SmartItem>> sort = (list) -> collectionsManager.sort(list, "label", false);

		final int sizeDtc = dtc.size();

		DtList<SmartItem> sortDtc, filterDtc, subList;
		// ======================== sort/filter
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		filterDtc = sortDtc.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sort/sublist
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		subList = subList(sortDtc, 0, sizeDtc - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sort
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		sortDtc = sort.apply(filterDtc);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sublist
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		subList = subList(filterDtc, 0, filterDtc.size() - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/sort
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		sortDtc = sort.apply(subList);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/filter
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		filterDtc = subList.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// === dtc non modifié
		assertEquals(indexDtc, extractLabels(dtc));

	}

	@Test
	public void testCreateFilterForValue() {
		final Predicate predicate = collectionsManager
				.filter(ListFilter.of("label" + ":\"aaa\""));
		Assertions.assertNotNull(predicate);
	}

	@Test
	public void testTermFilterString() {
		testTermFilter("label:\"aaa\"", 2);
		testTermFilter("label:\"aaab\"", 1);
	}

	@Test
	public void testTermFilterLong() {
		testTermFilter("id:\"1\"", 1);
		testTermFilter("id:\"11\"", 1);
		testTermFilter("id:\"2\"", 0);
	}

	@Test
	public void testCreateFilter() {
		final Predicate<DtObject> predicate = collectionsManager.filter(ListFilter.of("label" + ":[a TO b]"));
		Assertions.assertNotNull(predicate);
	}

	@Test
	public void testRangeFilter() {
		testRangeFilter("label" + ":[a TO b]", 5);
	}

	@Test
	public void testRangeFilterLong() {
		testRangeFilter("id:[1 TO 10]", 3);
		testRangeFilter("id:[1 TO 10[", 2);
		testRangeFilter("id:]1 TO 10]", 2);
		testRangeFilter("id:]1 TO 10[", 1);
		testRangeFilter("id:]1 TO *[", 9);
		testRangeFilter("id:[* TO *[", 10);
	}

	@Test
	public void testRangeFilterString() {
		testRangeFilter("label:[a TO b]", 5);
		testRangeFilter("label:[* TO c[", 7);
		testRangeFilter("label:[* TO c]", 8);
		testRangeFilter("label:[* TO cb]", 9);
		testRangeFilter("label:[aaab TO aaac]", 2);
		testRangeFilter("label:[aaab TO aaac[", 1);
	}

	private void testTermFilter(final String filterString, final int countEspected) {
		final DtList<SmartItem> result = createItemsForRangeTest()
				.stream()
				.filter(collectionsManager.filter(ListFilter.of(filterString)))
				.collect(VCollectors.toDtList(SmartItem.class));

		Assertions.assertEquals(countEspected, result.size());
	}

	private void testRangeFilter(final String filterString, final int countEspected) {
		final Predicate<SmartItem> predicate = collectionsManager
				.filter(ListFilter.of(filterString));
		Assertions.assertNotNull(predicate);
		final DtList<SmartItem> result = createItemsForRangeTest().stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		Assertions.assertEquals(countEspected, result.size());
	}

	private static DtList<SmartItem> createItemsForRangeTest() {
		final DtList<SmartItem> dtc = createItems();

		final SmartItem mock1 = new SmartItem();
		mock1.setId(1L);
		mock1.setLabel("aaab");
		dtc.add(mock1);

		final SmartItem mock2 = new SmartItem();
		mock2.setId(10L);
		mock2.setLabel("aaac");
		dtc.add(mock2);

		final SmartItem mock3 = new SmartItem();
		mock3.setId(11L);
		mock3.setLabel("caaa");
		dtc.add(mock3);

		final SmartItem mock4 = new SmartItem();
		mock4.setId(12L);
		mock4.setLabel("aaa");
		dtc.add(mock4);

		final SmartItem mock5 = new SmartItem();
		mock5.setId(13L);
		mock5.setLabel("aaa");
		dtc.add(mock5);

		final SmartItem mock6 = new SmartItem();
		mock6.setId(3L);
		mock6.setLabel("c");
		dtc.add(mock6);

		return dtc;
	}

	/**
	 * Asserts that two booleans are equal.
	 *
	 */
	private static void assertEquals(final String[] expected, final String[] actual) {
		Assertions.assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] extractLabels(final DtList<SmartItem> dtc) {
		final String[] index = new String[dtc.size()];
		for (int i = 0; i < dtc.size(); i++) {
			index[i] = dtc.get(i).getLabel();
		}
		return index;
	}

	private static long seqId = 100;

	private static DtList<SmartItem> createItems() {
		final DtList<SmartItem> dtc = new DtList<>(SmartItem.class);
		// les index sont données par ordre alpha > null à la fin >
		final SmartItem mockB = new SmartItem();
		mockB.setId(seqId++);
		mockB.setLabel(Ba_aa);
		dtc.add(mockB);

		final SmartItem mockNull = new SmartItem();
		mockNull.setId(seqId++);
		// On ne renseigne pas le Label > null
		dtc.add(mockNull);

		final SmartItem mocka = new SmartItem();
		mocka.setId(seqId++);
		mocka.setLabel(aaa_ba);
		dtc.add(mocka);

		final SmartItem mockb = new SmartItem();
		mockb.setId(seqId++);
		mockb.setLabel(bb_aa);
		dtc.add(mockb);

		// On crée et on supprimme un élément dans la liste pour vérifier
		// l'intégrité de la liste (Par rapport aux null).
		final SmartItem mockRemoved = new SmartItem();
		mockRemoved.setId(seqId++);
		mockRemoved.setLabel("mockRemoved");
		dtc.add(mockRemoved);

		dtc.remove(mockRemoved);
		return dtc;
	}
}
