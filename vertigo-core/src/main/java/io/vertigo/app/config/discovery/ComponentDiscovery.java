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
package io.vertigo.app.config.discovery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.vertigo.app.config.ModuleConfigBuilder;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.component.proxy.ProxyMethodAnnotation;
import io.vertigo.lang.Assertion;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.ClassConditions;

/**
 * Tool for registering components in an app based on discovery in a package tree.
 * @author mlaroche
 *
 */
final class ComponentDiscovery {

	private ComponentDiscovery() {
		//private
	}

	/**
	 * Register all components of a kind discovered in a package tree.
	 * If component has API we must find one and only one Impl.
	 * If component hasn't API we don't care.
	 * @param componentType the kind of components to discover
	 * @param packagePrefix the package we to look
	 * @param moduleConfigBuilder the module where components will be added.
	 */
	static void registerComponents(final String packagePrefix, final ModuleConfigBuilder moduleConfigBuilder) {
		Assertion.checkArgNotEmpty(packagePrefix);
		Assertion.checkNotNull(moduleConfigBuilder);
		//---
		final Collection<Class> components = new Selector()
				.from(packagePrefix)
				.filterClasses(ClassConditions.subTypeOf(Component.class))
				.filterClasses(ClassConditions.isAbstract().negate())// we filter abstract classes
				// we ignore not discoverable classes
				.filterClasses(ClassConditions.annotatedWith(NotDiscoverable.class).negate())
				.findClasses();
		registerComponents(components, moduleConfigBuilder);
	}

	private static void registerComponents(final Collection<Class> components, final ModuleConfigBuilder moduleConfigBuilder) {
		Assertion.checkNotNull(components);
		Assertion.checkNotNull(moduleConfigBuilder);
		// ---
		//API
		final Collection<Class> allApiClasses = new Selector()
				.from(components)
				.filterClasses(ClassConditions.interfaces())
				// we dont check api for plugins
				.filterClasses(ClassConditions.subTypeOf(Plugin.class).negate())
				.findClasses();

		final Predicate<Method> proxyMethodPredicate = method -> Stream.of(method.getAnnotations())
				.anyMatch(annotation -> ClassConditions.annotatedWith(ProxyMethodAnnotation.class).test(annotation.annotationType()));

		final Collection<Class> proxyClasses = new Selector()
				.from(allApiClasses)
				.filterClasses(clazz -> clazz.getDeclaredMethods().length != 0)// to be a proxy you need to have at least one method
				.filterMethods(proxyMethodPredicate)
				.findClasses();

		final Collection<Class> apiClasses = new Selector()
				.from(allApiClasses)
				.filterMethods(proxyMethodPredicate.negate())
				.findClasses();

		//Impl
		final Collection<Class> implClasses = new Selector()
				.from(components)
				.filterClasses(ClassConditions.interfaces().negate())
				.findClasses();

		//ComponentsImpl
		final Collection<Class> componentsImplClasses = new Selector()
				.from(implClasses)
				//Plugins are handled separately
				.filterClasses(ClassConditions.subTypeOf(Plugin.class).negate())
				.findClasses();

		//PluginsImpl
		final Collection<Class> pluginsImplClasses = new Selector()
				.from(implClasses)
				.filterClasses(ClassConditions.subTypeOf(Plugin.class))
				.findClasses();

		final Map<Class, Class> apiImplMap = new HashMap<>();
		final Collection<Class> myImplClasses = new ArrayList<>(componentsImplClasses);
		//---
		for (final Class apiClazz : apiClasses) {
			final Collection<Class> candidates = new Selector()
					.from(componentsImplClasses)
					.filterClasses(ClassConditions.subTypeOf(apiClazz))
					.findClasses();
			// ---
			Assertion.checkState(!candidates.isEmpty(), "No implentation found for the api {0}", apiClazz);
			Assertion.checkState(candidates.size() == 1, "Multiple implentations found for the api {0}", apiClazz);
			// ---
			final Class implClass = candidates.stream().findFirst().get();
			myImplClasses.remove(implClass);
			apiImplMap.put(apiClazz, implClass);
		}
		//---
		// Proxies
		proxyClasses
				.forEach(moduleConfigBuilder::addProxy);

		// With API
		apiImplMap
				.forEach((key, value) -> moduleConfigBuilder.addComponent(key, value));

		// Without API
		myImplClasses
				.forEach(moduleConfigBuilder::addComponent);

		//Plugins
		pluginsImplClasses
				.forEach(moduleConfigBuilder::addPlugin);
	}
}
