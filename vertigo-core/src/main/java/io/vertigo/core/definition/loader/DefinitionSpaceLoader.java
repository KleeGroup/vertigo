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
package io.vertigo.core.definition.loader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.component.loader.ComponentSpaceLoader;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.lang.Assertion;

/**
 * A DefinitionLoader uses all the DefinitionProviders of all the modules to register all definitions at once at the beginning.
 * Use DynamoDefinitionProvider to use the DSL.
 *
 * @author pchretien
 */
public final class DefinitionSpaceLoader {
	private final DefinitionSpaceWritable definitionSpaceWritable;
	private final ComponentSpace componentSpace;

	/**
	 * Loader of definitions
	 * @param componentSpace the componentSpace
	 */
	public static DefinitionSpaceLoader startLoading(final DefinitionSpaceWritable definitionSpaceWritable, final ComponentSpace componentSpace) {
		return new DefinitionSpaceLoader(definitionSpaceWritable, componentSpace);
	}

	/**
	 * Loader of definitions
	 * @param componentSpace the componentSpace
	 */
	private DefinitionSpaceLoader(final DefinitionSpaceWritable definitionSpaceWritable, final ComponentSpace componentSpace) {
		Assertion.checkNotNull(definitionSpaceWritable);
		Assertion.checkNotNull(componentSpace);
		//-----
		this.definitionSpaceWritable = definitionSpaceWritable;
		this.componentSpace = componentSpace;
	}

	/**
	 * Inject all the definition of the modules.
	 *
	 * @param moduleConfigs module configs
	 * @return a stream of definitions
	 */
	public DefinitionSpaceLoader loadDefinitions(final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//--
		final Stream<Definition> definitions = moduleConfigs
				.stream()
				.flatMap(moduleConfig -> provide(moduleConfig.getDefinitionProviderConfigs()))
				.map(supplier -> supplier.get(definitionSpaceWritable));

		definitions.forEach(definitionSpaceWritable::registerDefinition);
		return this;
	}

	/**
	 * Inject all the definitions provided by components.
	 */
	public DefinitionSpaceLoader loadDefinitionsFromComponents() {
		//--
		final Stream<Definition> definition = componentSpace.keySet()
				.stream()
				.map(key -> componentSpace.resolve(key, Component.class))
				.filter(component -> DefinitionProvider.class.isAssignableFrom(component.getClass()))
				.flatMap(component -> ((DefinitionProvider) component).get(definitionSpaceWritable).stream())
				.map(defitionSupplier -> defitionSupplier.get(definitionSpaceWritable));

		definition.forEach(definitionSpaceWritable::registerDefinition);
		return this;
	}

	private Stream<DefinitionSupplier> provide(final List<DefinitionProviderConfig> definitionProviderConfigs) {
		return definitionProviderConfigs
				.stream()
				.map(this::createDefinitionProvider)
				.flatMap(definitionProvider -> definitionProvider.get(definitionSpaceWritable).stream());
	}

	private DefinitionProvider createDefinitionProvider(final DefinitionProviderConfig definitionProviderConfig) {
		final DefinitionProvider definitionProvider = ComponentSpaceLoader.createInstance(definitionProviderConfig.getDefinitionProviderClass(), componentSpace, Optional.empty(),
				definitionProviderConfig.getParams());

		definitionProviderConfig.getDefinitionResourceConfigs()
				.forEach(definitionProvider::addDefinitionResourceConfig);

		return definitionProvider;
	}

	public void endLoading() {
		definitionSpaceWritable.closeRegistration();
	}
}
