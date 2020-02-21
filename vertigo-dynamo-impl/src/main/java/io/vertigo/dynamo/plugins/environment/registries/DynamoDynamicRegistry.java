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
package io.vertigo.dynamo.plugins.environment.registries;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DynamicRegistry;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistry;
import io.vertigo.dynamo.plugins.environment.registries.file.FileDynamicRegistry;
import io.vertigo.dynamo.plugins.environment.registries.search.SearchDynamicRegistry;
import io.vertigo.dynamo.plugins.environment.registries.task.TaskDynamicRegistry;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
public final class DynamoDynamicRegistry implements DynamicRegistry {
	private final List<DynamicRegistry> dynamicRegistries;
	private final DslGrammar dslGrammar;

	/**
	 * Constructor.
	 */
	public DynamoDynamicRegistry() {
		dynamicRegistries = new ListBuilder()
				.add(new DomainDynamicRegistry())
				.add(new FileDynamicRegistry())
				.add(new SearchDynamicRegistry())
				.add(new TaskDynamicRegistry())
				.build();
		dslGrammar = createGrammar();
	}

	private DslGrammar createGrammar() {
		return new DslGrammar() {

			@Override
			public List<DslEntity> getEntities() {
				return dynamicRegistries
						.stream()
						.flatMap(dynamicRegistry -> dynamicRegistry.getGrammar().getEntities().stream())
						.collect(Collectors.toList());
			}

			@Override
			public List<DslDefinition> getRootDefinitions() {
				return dynamicRegistries
						.stream()
						.flatMap(dynamicRegistry -> dynamicRegistry.getGrammar().getRootDefinitions().stream())
						.collect(Collectors.toList());
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public DslGrammar getGrammar() {
		return dslGrammar;
	}

	/** {@inheritDoc} */
	@Override
	public List<DslDefinition> onNewDefinition(final DslDefinition dslDefinition) {
		//Les entités du noyaux ne sont pas à gérer par des managers spécifiques.
		if (!dslDefinition.getEntity().isProvided()) {
			return lookUpDynamicRegistry(dslDefinition)
					.onNewDefinition(dslDefinition);
		}
		return Collections.emptyList();
	}

	/** {@inheritDoc} */
	@Override
	public DefinitionSupplier supplyDefinition(final DslDefinition dslDefinition) {
		try {
			// perf: ifs ordonnés en gros par fréquence sur les projets
			return lookUpDynamicRegistry(dslDefinition)
					.supplyDefinition(dslDefinition);
		} catch (final Exception e) {
			//on catch tout (notament les assertions) car c'est ici qu'on indique l'URI de la définition posant problème
			throw WrappedException.wrap(e, "An error occurred during the creation of the following definition : {0}", dslDefinition.getName());
		}
	}

	private DynamicRegistry lookUpDynamicRegistry(final DslDefinition dslDefinition) {
		//On regarde si la grammaire contient la métaDefinition.
		return dynamicRegistries
				.stream()
				.filter(dynamicRegistry -> dynamicRegistry.getGrammar().getEntities().contains(dslDefinition.getEntity()))
				.findFirst()
				//Si on n'a pas trouvé de définition c'est qu'il manque la registry.
				.orElseThrow(() -> new IllegalArgumentException(dslDefinition.getEntity().getName() + " " + dslDefinition.getName() + " non traitée. Il manque une DynamicRegistry ad hoc."));
	}
}
