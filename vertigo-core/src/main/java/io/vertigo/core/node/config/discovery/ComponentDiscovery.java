/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.config.discovery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.component.amplifier.ProxyMethodAnnotation;
import io.vertigo.core.node.config.ModuleConfigBuilder;
import io.vertigo.core.util.Selector;
import io.vertigo.core.util.Selector.ClassConditions;

/**
 * Tool for registering components in an node based on discovery in a package tree.
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
		Assertion.check()
				.isNotBlank(packagePrefix)
				.isNotNull(moduleConfigBuilder);
		//---
		final Collection<Class> components = Selector
				.from(packagePrefix)
				.filterClasses(ClassConditions.subTypeOf(CoreComponent.class))
				.filterClasses(ClassConditions.isAbstract().negate())// we filter abstract classes
				// we ignore not discoverable classes
				.filterClasses(ClassConditions.annotatedWith(NotDiscoverable.class).negate())
				.findClasses();
		registerComponents(components, moduleConfigBuilder);
	}

	private static void registerComponents(final Collection<Class> components, final ModuleConfigBuilder moduleConfigBuilder) {
		Assertion.check()
				.isNotNull(components)
				.isNotNull(moduleConfigBuilder);
		// ---
		//API
		final Collection<Class> allApiClasses = Selector
				.from(components)
				.filterClasses(ClassConditions.interfaces())
				// we dont check api for plugins
				.filterClasses(ClassConditions.subTypeOf(Plugin.class).negate())
				.findClasses();

		final Predicate<Method> proxyMethodPredicate = method -> Stream.of(method.getAnnotations())
				.anyMatch(annotation -> ClassConditions.annotatedWith(ProxyMethodAnnotation.class).test(annotation.annotationType()));

		final Collection<Class> proxyClasses = Selector
				.from(allApiClasses)
				.filterClasses(clazz -> clazz.getDeclaredMethods().length != 0)// to be a proxy you need to have at least one method
				.filterMethods(proxyMethodPredicate)
				.findClasses();

		final Collection<Class> apiClasses = Selector
				.from(allApiClasses)
				.filterMethods(proxyMethodPredicate.negate())
				.findClasses();

		//Impl
		final Collection<Class> implClasses = Selector
				.from(components)
				.filterClasses(ClassConditions.interfaces().negate())
				.findClasses();

		//ComponentsImpl
		final Collection<Class> componentsImplClasses = Selector
				.from(implClasses)
				//Plugins are handled separately
				.filterClasses(ClassConditions.subTypeOf(Plugin.class).negate())
				.findClasses();

		//PluginsImpl
		final Collection<Class> pluginsImplClasses = Selector
				.from(implClasses)
				.filterClasses(ClassConditions.subTypeOf(Plugin.class))
				.findClasses();

		final Map<Class, Class> apiImplMap = new HashMap<>();
		final Collection<Class> myImplClasses = new ArrayList<>(componentsImplClasses);
		//---
		for (final Class apiClazz : apiClasses) {
			final Collection<Class> candidates = Selector
					.from(componentsImplClasses)
					.filterClasses(ClassConditions.subTypeOf(apiClazz))
					.findClasses();
			// ---
			Assertion.check()
					.isFalse(candidates.isEmpty(), "No implentation found for the api {0}", apiClazz)
					.isTrue(candidates.size() == 1, "Multiple implentations found for the api {0}", apiClazz);
			// ---
			final Class implClass = candidates.stream().findFirst().get();
			myImplClasses.remove(implClass);
			apiImplMap.put(apiClazz, implClass);
		}
		//---
		// Proxies
		proxyClasses
				.forEach(moduleConfigBuilder::addAmplifier);

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
