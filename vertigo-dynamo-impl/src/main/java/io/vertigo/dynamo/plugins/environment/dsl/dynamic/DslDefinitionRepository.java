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
package io.vertigo.dynamo.plugins.environment.dsl.dynamic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.lang.Assertion;

/**
 * Espace de nommage.
 * Les éléments de la grammaire à savoir les définitions sont ajoutées à la volée.
 * Les définitions doivent respecter une métagrammaire définie préalablement dans DynamicNameSpace.
 *
 * @author  pchretien
 */
public final class DslDefinitionRepository {

	/***
	 * On retient les définitions dans l'ordre pour
	 * créer les fichiers toujours de la même façon.
	 */
	private final Map<String, DslDefinition> dslDefinitions = new LinkedHashMap<>();
	private final List<DslDefinition> partials = new ArrayList<>();

	private final DynamicRegistry registry;
	private final DslGrammar grammar;

	/**
	 * Constructor.
	 * @param registry DynamicDefinitionHandler
	 */
	public DslDefinitionRepository(final DynamicRegistry registry) {
		Assertion.checkNotNull(registry);
		//-----
		this.registry = registry;
		grammar = registry.getGrammar();
	}

	/**
	 * @return Grammar
	 */
	public DslGrammar getGrammar() {
		return grammar;
	}

	/**
	 * Returns true if a definition to which the specified name is mapped.
	 * @param definitionName name of the definitionClé de la définition
	 * @return Si la définition a déjà été enregistrée
	 */
	public boolean containsDefinitionName(final String definitionName) {
		return dslDefinitions.containsKey(definitionName);
	}

	/**
	 * Récupération d'une définition par sa clé
	 *  -Soit la clé n'existe pas
	 *  -Soit la clé existe mais sans aucune définition
	 *  -Soit la clé raméne une définition.
	 *
	 * @param definitionName Name of the definition
	 * @return DynamicDefinition Définition correspondante ou null.
	 */
	public DslDefinition getDefinition(final String definitionName) {
		Assertion.checkArgument(dslDefinitions.containsKey(definitionName), "Aucune clé enregistrée pour :{0} parmi {1}", definitionName, dslDefinitions.keySet());
		//-----
		final DslDefinition definition = dslDefinitions.get(definitionName);
		//-----
		Assertion.checkNotNull(definition, "Clé trouvée mais pas de définition enregistrée trouvée pour {0}", definitionName);
		return definition;
	}

	/**
	 * Résolution des références de définitions.
	 * @param definitionSpace Space where all the definitions are stored
	 * @return a list of DefinitionSuppliers
	 */
	public List<DefinitionSupplier> solve(final DefinitionSpace definitionSpace) {
		mergePartials();

		final List<DslDefinition> sortedDslDefinitions = DslSolver.solve(definitionSpace, this);
		return createDefinitionStream(sortedDslDefinitions);
	}

	private void mergePartials() {
		//parts of definitions are merged
		for (final DslDefinition partial : partials) {
			final DslDefinition merged = DslDefinition.builder(partial.getName(), partial.getEntity())
					.merge(getDefinition(partial.getName()))
					.merge(partial).build();
			dslDefinitions.put(partial.getName(), merged);
		}
	}

	private List<DefinitionSupplier> createDefinitionStream(final List<DslDefinition> sortedDynamicDefinitions) {
		return sortedDynamicDefinitions
				.stream()
				.filter(dslDefinition -> !dslDefinition.getEntity().isProvided()) // provided definitions are excluded
				.map(this::createDefinition)
				.collect(Collectors.toList());
	}

	private DefinitionSupplier createDefinition(final DslDefinition dslDefinition) {
		DsValidator.check(dslDefinition);
		//The definition identified as root are not registered.
		return registry.supplyDefinition(dslDefinition);
	}

	/**
	 * Add a definition.
	 * @param dslDefinition DynamicDefinition
	 */
	public void addDefinition(final DslDefinition dslDefinition) {
		Assertion.checkNotNull(dslDefinition);
		//---
		final DslDefinition previousDefinition = dslDefinitions.put(dslDefinition.getName(), dslDefinition);
		Assertion.checkState(previousDefinition == null, "this definition '{0}' has already be registered", dslDefinition.getName());
		//---
		registry.onNewDefinition(dslDefinition)
				.stream()
				.forEach(this::addDefinition);
	}

	/**
	 * adds a partial definition.
	 * @param partial the part of a definition
	 */
	public void addPartialDefinition(final DslDefinition partial) {
		Assertion.checkNotNull(partial);
		//---
		partials.add(partial);
	}

	/**
	 *  @return Liste des clés orphelines.
	 */
	Set<String> getOrphanDefinitionKeys() {
		return dslDefinitions.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == null) //select orphans
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	/**
	 * @return Liste des définitions complètes
	 */
	Collection<DslDefinition> getDefinitions() {
		return Collections.unmodifiableCollection(dslDefinitions.values());
	}
}
