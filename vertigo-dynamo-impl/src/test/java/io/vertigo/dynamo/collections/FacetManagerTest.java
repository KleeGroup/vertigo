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

import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.Home;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.collections.data.domain.SmartCar;
import io.vertigo.dynamo.collections.data.domain.SmartCarDataBase;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.collections.model.SelectedFacetValues;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

/**
 * @author  npiedeloup
 */
//non final, to be overrided for previous lib version
public class FacetManagerTest extends AbstractTestCaseJU5 {
	@Inject
	private CollectionsManager collectionsManager;
	private FacetedQueryDefinition carFacetQueryDefinition;
	private SmartCarDataBase smartCarDataBase;

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

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		//On construit la BDD des voitures
		smartCarDataBase = new SmartCarDataBase();
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		carFacetQueryDefinition = definitionSpace.resolve("QryCarFacet", FacetedQueryDefinition.class);
	}

	private void testFacetResultByRange(final FacetedQueryResult<SmartCar, ?> result) {
		Assertions.assertEquals(smartCarDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(3, result.getFacets().size());

		//On recherche la facette date
		final Facet yearFacet = getFacetByName(result, "FctYearCar");
		Assertions.assertTrue(yearFacet.getDefinition().isRangeFacet());

		boolean found = false;
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains("avant")) {
				found = true;
				Assertions.assertEquals(smartCarDataBase.getCarsBefore(2000), entry.getValue().longValue());
			}
		}
		Assertions.assertTrue(found);
	}

	private void testFacetResultByTerm(final FacetedQueryResult<SmartCar, ?> result) {
		Assertions.assertEquals(smartCarDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(3, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet manufacturerFacet = getFacetByName(result, "FctManufacturerCar");
		//On vérifie que l'on est sur le champ Make
		Assertions.assertEquals("manufacturer", manufacturerFacet.getDefinition().getDtField().getName());
		Assertions.assertFalse(manufacturerFacet.getDefinition().isRangeFacet());

		//On vérifie qu'il existe une valeur pour peugeot et que le nombre d'occurrences est correct
		boolean found = false;
		final String manufacturer = "peugeot";
		for (final Entry<FacetValue, Long> entry : manufacturerFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).equals(manufacturer)) {
				found = true;
				//System.out.println("manufacturer" + entry.getKey().getLabel().getDisplay());
				Assertions.assertEquals(smartCarDataBase.getCarsByManufacturer(manufacturer).size(), entry.getValue().intValue());
			}
		}
		Assertions.assertTrue(found);
	}

	private void testClusterResultByRange(final FacetedQueryResult<SmartCar, ?> result) {
		Assertions.assertEquals(smartCarDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(3, result.getClusters().size());

		//On vérifie qu'il existe une valeur pour avant 2000 et que le nombre d'occurrences est correct
		boolean found = false;
		for (final Entry<FacetValue, DtList<SmartCar>> entry : result.getClusters().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains("avant")) {
				found = true;
				Assertions.assertEquals(smartCarDataBase.getCarsBefore(2000), entry.getValue().size());
			}
		}
		Assertions.assertTrue(found);
	}

	private void testClusterResultByTerm(final FacetedQueryResult<SmartCar, ?> result) {
		Assertions.assertEquals(smartCarDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de cluster (nombre de manufacturer).
		Assertions.assertEquals(5, result.getClusters().size());

		//On vérifie qu'il existe une valeur pour peugeot et que le nombre d'occurrences est correct
		boolean found = false;
		final String manufacturer = "peugeot";
		for (final Entry<FacetValue, DtList<SmartCar>> entry : result.getClusters().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).equals(manufacturer)) {
				found = true;
				Assertions.assertEquals(smartCarDataBase.getCarsByManufacturer(manufacturer).size(), entry.getValue().size());
			}
		}
		Assertions.assertTrue(found);
	}

	private static Facet getFacetByName(final FacetedQueryResult<SmartCar, ?> result, final String facetName) {
		return result.getFacets()
				.stream()
				.filter(facet -> facetName.equals(facet.getDefinition().getName()))
				.findFirst()
				.orElseThrow(NullPointerException::new);
	}

	/**
	 * Test le facettage par range d'une liste.
	 */
	@Test
	public void testFacetListByRange() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.empty());
		testFacetResultByRange(result);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByRange() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.empty());
		//on applique une facette
		final FacetedQuery query = addFacetQuery("FctYearCar", "avant", result);
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> resultFiltered = collectionsManager.facetList(result.getSource(), query, Optional.empty());
		Assertions.assertEquals(smartCarDataBase.getCarsBefore(2000), resultFiltered.getCount());
	}

	private static FacetedQuery addFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<SmartCar, ?> result) {
		FacetValue facetFilter = null; //pb d'initialisation, et Assertions.notNull ne suffit pas
		final Facet yearFacet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains(facetValueLabel)) {
				facetFilter = entry.getKey();
				break;
			}
		}
		if (facetFilter == null) {
			throw new IllegalArgumentException("Pas de FacetValue contenant " + facetValueLabel + " dans la facette " + facetName);
		}
		final FacetedQuery previousQuery = result.getFacetedQuery().get();
		final SelectedFacetValues queryFilters = SelectedFacetValues
				.of(previousQuery.getSelectedFacetValues())
				.add(yearFacet.getDefinition(), facetFilter)
				.build();
		return new FacetedQuery(previousQuery.getDefinition(), queryFilters);
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testFacetListByTerm() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.empty());
		testFacetResultByTerm(result);
	}

	/**
	 * Test le cluster par term d'une liste.
	 */
	@Test
	public void testFacetClusterByTerm() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetDefinition clusterDefinition = obtainFacetDefinition("FctManufacturerCar");
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.of(clusterDefinition));
		testClusterResultByTerm(result);
	}

	/**
	 * Test le cluster par range d'une liste.
	 */
	@Test
	public void testFacetClusterByRange() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetDefinition clusterDefinition = obtainFacetDefinition("FctYearCar");
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.of(clusterDefinition));
		testClusterResultByRange(result);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByTerm() {
		final DtList<SmartCar> cars = smartCarDataBase.getAllCars();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, SelectedFacetValues.empty().build());
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> result = collectionsManager.facetList(cars, facetedQuery, Optional.empty());
		//on applique une facette
		final FacetedQuery query = addFacetQuery("FctManufacturerCar", "peugeot", result);
		final FacetedQueryResult<SmartCar, DtList<SmartCar>> resultFiltered = collectionsManager.facetList(result.getSource(), query, Optional.empty());
		Assertions.assertEquals(smartCarDataBase.getCarsByManufacturer("peugeot").size(), (int) resultFiltered.getCount());
	}

	private FacetDefinition obtainFacetDefinition(final String facetName) {
		return Home.getApp().getDefinitionSpace().resolve(facetName, FacetDefinition.class);
	}

}
