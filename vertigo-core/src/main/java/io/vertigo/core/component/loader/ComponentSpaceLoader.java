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
package io.vertigo.core.component.loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.app.config.AspectConfig;
import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.ProxyMethodConfig;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.Container;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.di.DIInjector;
import io.vertigo.core.component.di.DIReactor;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * The componentLoader class defines the way to load the components defined in the config into componentSpace.
 * @author pchretien
 */
public final class ComponentSpaceLoader {
	private final AopPlugin aopPlugin;
	/** Aspects.*/
	private final List<Aspect> aspects = new ArrayList<>();

	/** Proxies.*/
	private final List<ProxyMethod> proxyMethods = new ArrayList<>();
	private final ComponentSpaceWritable componentSpaceWritable;

	public static ComponentSpaceLoader startLoading(final ComponentSpaceWritable componentSpaceWritable, final AopPlugin aopPlugin) {
		return new ComponentSpaceLoader(componentSpaceWritable, aopPlugin);
	}

	/**
	* Constructor.
	* @param aopPlugin the plugin which is reponsible for the aop strategy
	*/
	private ComponentSpaceLoader(final ComponentSpaceWritable componentSpaceWritable, final AopPlugin aopPlugin) {
		Assertion.checkNotNull(componentSpaceWritable);
		Assertion.checkNotNull(aopPlugin);
		//-----
		this.componentSpaceWritable = componentSpaceWritable;
		this.aopPlugin = aopPlugin;
	}

	public ComponentSpaceLoader loadBootComponents(final List<ComponentConfig> componentConfigs) {
		Assertion.checkNotNull(componentConfigs);
		//--
		registerComponents(Optional.empty(), "boot", componentConfigs);
		return this;
	}

	/**
	 * Add all the components defined in the moduleConfigs into the componentSpace.
	 * @param moduleConfigs the config of the module to add.
	 */
	public ComponentSpaceLoader loadAllComponentsAndAspects(final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		final ParamManager paramManager = componentSpaceWritable.resolve(ParamManager.class);
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			registerComponents(Optional.of(paramManager),
					moduleConfig.getName(),
					moduleConfig.getComponentConfigs());
			registerAspects(moduleConfig.getAspectConfigs());
			registerProxyMethods(moduleConfig.getProxyMethodConfigs());
		}
		return this;
	}

	/**
	 * registers all the components defined by their configs.
	 * @param paramManagerOpt the optional manager of params
	 * @param moduleName the name of the module
	 * @param componentConfigs the configs of the components
	 */
	private void registerComponents(final Optional<ParamManager> paramManagerOpt, final String moduleName, final List<ComponentConfig> componentConfigs) {
		Assertion.checkNotNull(paramManagerOpt);
		Assertion.checkNotNull(moduleName);
		Assertion.checkNotNull(componentConfigs);
		//---- Proxies----
		componentConfigs
				.stream()
				.filter(ComponentConfig::isProxy)
				.forEach(componentConfig -> {
					final Component component = createProxyWithOptions(/*paramManagerOpt,*/ componentConfig);
					componentSpaceWritable.registerComponent(componentConfig.getId(), component);
				});

		//---- No proxy----
		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpaceWritable.keySet()) {
			reactor.addParent(id);
		}
		//Map des composants définis par leur id
		final Map<String, ComponentConfig> componentConfigById = componentConfigs
				.stream()
				.filter(componentConfig -> !componentConfig.isProxy())
				.peek(componentConfig -> reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet()))
				.collect(Collectors.toMap(ComponentConfig::getId, Function.identity()));

		//Comment trouver des plugins orphenlins ?

		final List<String> ids = reactor.proceed();
		//On a récupéré la liste ordonnée des ids.
		//On positionne un proxy pour compter les plugins non utilisés
		final ComponentUnusedKeysContainer componentProxyContainer = new ComponentUnusedKeysContainer(componentSpaceWritable);

		for (final String id : ids) {
			final ComponentConfig componentConfig = componentConfigById.get(id);
			if (componentConfig != null) {
				//Si il s'agit d'un composant (y compris plugin)

				// 2.a On crée le composant avec AOP et autres options (elastic)
				final Component component = createComponentWithOptions(paramManagerOpt, componentProxyContainer, componentConfig);
				// 2.b. On enregistre le composant
				componentSpaceWritable.registerComponent(componentConfig.getId(), component);
			}
		}

		//--Search for unuseds plugins
		final List<String> unusedPluginIds = componentConfigs
				.stream()
				.filter(componentConfig -> !componentConfig.isProxy())
				.filter(componentConfig -> Plugin.class.isAssignableFrom(componentConfig.getImplClass()))
				//only plugins are considered
				.map(ComponentConfig::getId)
				//used keys are removed
				.filter(pluginId -> !componentProxyContainer.getUsedKeys().contains(pluginId))
				.collect(Collectors.toList());

		if (!unusedPluginIds.isEmpty()) {
			throw new VSystemException("plugins '{0}' in module'{1}' are not used by injection", unusedPluginIds, moduleName);
		}
	}

	private void registerAspects(final List<AspectConfig> aspectConfigs) {
		//. We build then register all the aspects
		aspectConfigs
				.stream()
				.map(aspectConfig -> createAspect(componentSpaceWritable, aspectConfig))
				.forEach(this::registerAspect);
	}

	private void registerProxyMethods(final List<ProxyMethodConfig> proxyMethodConfigs) {
		proxyMethodConfigs
				.stream()
				.map(proxyMethodConfig -> createProxyMethod(componentSpaceWritable, proxyMethodConfig))
				.forEach(this::registerProxyMethod);
	}

	private static Aspect createAspect(final Container container, final AspectConfig aspectConfig) {
		// création de l'instance du composant
		final Aspect aspect = DIInjector.newInstance(aspectConfig.getAspectClass(), container);
		//---
		Assertion.checkNotNull(aspect.getAnnotationType());
		return aspect;
	}

	private static ProxyMethod createProxyMethod(final Container container, final ProxyMethodConfig proxyMethodConfig) {
		// création de l'instance du composant
		final ProxyMethod proxyMethod = DIInjector.newInstance(proxyMethodConfig.getProxyMethodClass(), container);
		//---
		Assertion.checkNotNull(proxyMethod.getAnnotationType());
		return proxyMethod;
	}

	private void registerAspect(final Aspect aspect) {
		Assertion.checkNotNull(aspect);
		Assertion.checkArgument(aspects.stream().noneMatch(a -> a.getClass().equals(aspect.getClass())),
				"aspect {0} already registered with the same class", aspect.getClass());
		Assertion.checkArgument(aspects.stream().noneMatch(a -> a.getAnnotationType().equals(aspect.getAnnotationType())),
				"aspect {0} already registered with the same annotation", aspect.getClass());
		//-----
		aspects.add(aspect);
	}

	private void registerProxyMethod(final ProxyMethod proxyMethod) {
		Assertion.checkNotNull(proxyMethod);
		Assertion.checkArgument(proxyMethods.stream().noneMatch(a -> a.getClass().equals(proxyMethod.getClass())),
				"proxy {0} already registered with the same class", proxyMethod.getClass());
		Assertion.checkArgument(proxyMethods.stream().noneMatch(p -> p.getAnnotationType().equals(proxyMethod.getAnnotationType())),
				"proxy {0} already registered with the same annotation", proxyMethod.getClass());
		//-----
		proxyMethods.add(proxyMethod);
	}

	private <C extends Component> C injectAspects(final C instance, final Class implClass) {
		//2. AOP , a new instance is created when aspects are injected in the previous instance
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createAspectsByMethod(implClass, aspects);
		if (!joinPoints.isEmpty()) {
			return aopPlugin.wrap(instance, joinPoints);
		}
		return instance;
	}

	private static <C extends Component> C createInstance(
			final Container container,
			final Optional<ParamManager> paramManagerOpt,
			final ComponentConfig componentConfig) {
		return (C) createInstance(componentConfig.getImplClass(), container, paramManagerOpt, componentConfig.getParams());
	}

	//ici
	private Component createComponentWithOptions(
			final Optional<ParamManager> paramManagerOpt,
			final ComponentUnusedKeysContainer componentContainer,
			final ComponentConfig componentConfig) {
		Assertion.checkArgument(!componentConfig.isProxy(), "a no-proxy component is expected");
		//---
		// 1. An instance is created
		final Component instance = createInstance(componentContainer, paramManagerOpt, componentConfig);

		//2. AOP , a new instance is created when aspects are injected in the previous instance
		return injectAspects(instance, componentConfig.getImplClass());
	}

	private Component createProxyWithOptions(
			//	final Optional<ParamManager> paramManagerOpt,
			final ComponentConfig componentConfig) {
		Assertion.checkArgument(componentConfig.isProxy(), "a proxy component is expected");
		//---
		//1. AOP : finds all aspects
		final Map<Method, List<Aspect>> aspectsByMethod = ComponentAspectUtil.createAspectsByMethod(componentConfig.getApiClass().get(), aspects);

		// 2. An instance is created and all aspects are injected
		return ComponentProxyFactory.createProxy(
				componentConfig.getApiClass().get(),
				proxyMethods,
				aspectsByMethod);
	}

	/**
	 * Creates a component that use the injector but adds params support.
	 * @param clazz the clazz of the object to create
	 * @param container the container of the known components
	 * @param paramManagerOpt the optional ParamManager needed to use global params resolution
	 * @param params the local params
	 * @return the component created
	 */
	public static <T> T createInstance(
			final Class<T> clazz,
			final Container container,
			final Optional<ParamManager> paramManagerOpt,
			final Map<String, String> params) {
		Assertion.checkNotNull(paramManagerOpt);
		Assertion.checkNotNull(params);
		// ---
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOpt, params);
		final Container dualContainer = new ComponentDualContainer(container, paramsContainer);
		//---
		final T component = DIInjector.newInstance(clazz, dualContainer);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}' in component '{1}'", paramsContainer.getUnusedKeys(), clazz);
		return component;
	}

	public static void injectMembers(
			final Object instance,
			final Container container,
			final Optional<ParamManager> paramManagerOpt,
			final Map<String, String> params) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(container);
		Assertion.checkNotNull(paramManagerOpt);
		Assertion.checkNotNull(params);
		//-----
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOpt, params);
		final Container dualContainer = new ComponentDualContainer(container, paramsContainer);
		DIInjector.injectMembers(instance, dualContainer);
	}

	public void endLoading() {
		componentSpaceWritable.closeRegistration();
	}
}
