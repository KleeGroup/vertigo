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
package io.vertigo.core.node.component.loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.Container;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.amplifier.ProxyMethod;
import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.component.di.DIReactor;
import io.vertigo.core.node.config.AspectConfig;
import io.vertigo.core.node.config.CoreComponentConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.ProxyMethodConfig;
import io.vertigo.core.param.ParamManager;

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

	/**
	* Constructor.
	* @param aopPlugin the plugin which is reponsible for the aop strategy
	*/
	private ComponentSpaceLoader(final ComponentSpaceWritable componentSpaceWritable, final AopPlugin aopPlugin) {
		Assertion.check()
				.isNotNull(componentSpaceWritable)
				.isNotNull(aopPlugin);
		//-----
		this.componentSpaceWritable = componentSpaceWritable;
		this.aopPlugin = aopPlugin;
	}

	public static ComponentSpaceLoader startLoading(final ComponentSpaceWritable componentSpaceWritable, final AopPlugin aopPlugin) {
		return new ComponentSpaceLoader(componentSpaceWritable, aopPlugin);
	}

	public ComponentSpaceLoader loadBootComponents(final List<CoreComponentConfig> componentConfigs) {
		Assertion.check()
				.isNotNull(componentConfigs);
		//--
		registerComponents(Optional.empty(), "boot", componentConfigs);
		return this;
	}

	/**
	 * Add all the components defined in the moduleConfigs into the componentSpace.
	 * @param moduleConfigs the config of the module to add.
	 */
	public ComponentSpaceLoader loadAllComponentsAndAspects(final List<ModuleConfig> moduleConfigs) {
		Assertion.check()
				.isNotNull(moduleConfigs);
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
	private void registerComponents(final Optional<ParamManager> paramManagerOpt, final String moduleName, final List<CoreComponentConfig> componentConfigs) {
		Assertion.check()
				.isNotNull(paramManagerOpt)
				.isNotNull(moduleName)
				.isNotNull(componentConfigs);
		//---- Amplifier----
		componentConfigs
				.stream()
				.filter(CoreComponentConfig::isAmplifier)
				.forEach(componentConfig -> {
					final Amplifier component = createAmplifier(/*paramManagerOpt,*/ componentConfig);
					componentSpaceWritable.registerComponent(componentConfig.getId(), component);
				});

		//---- No proxy----
		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpaceWritable.keySet()) {
			reactor.addParent(id);
		}
		//Map des composants définis par leur id
		final Map<String, CoreComponentConfig> componentConfigById = componentConfigs
				.stream()
				.filter(componentConfig -> !componentConfig.isAmplifier())
				//can't use peek (Sonar : peek is for debug purpose)
				.collect(Collectors.toMap(CoreComponentConfig::getId, Function.identity()));

		componentConfigs.stream()//if we use componentConfigById componentConfig's order may changed
				.filter(componentConfig -> !componentConfig.isAmplifier())
				.forEach(componentConfig -> reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet()));

		//Comment trouver des plugins orphenlins ?

		final List<String> ids = reactor.proceed();
		//On a récupéré la liste ordonnée des ids.
		//On positionne un proxy pour compter les plugins non utilisés
		final ComponentUnusedKeysContainer componentProxyContainer = new ComponentUnusedKeysContainer(componentSpaceWritable);

		for (final String id : ids) {
			final CoreComponentConfig componentConfig = componentConfigById.get(id);
			if (componentConfig != null) {
				//Si il s'agit d'un composant (y compris plugin)

				// 2.a On crée le composant avec AOP et autres options (elastic)
				final CoreComponent component = createComponentWithOptions(paramManagerOpt, componentProxyContainer, componentConfig);
				// 2.b. On enregistre le composant
				componentSpaceWritable.registerComponent(componentConfig.getId(), component);
			}
		}

		//--Search for unuseds plugins
		final List<String> unusedPluginIds = componentConfigs
				.stream()
				//only plugins are considered
				.filter(CoreComponentConfig::isPlugin)
				.map(CoreComponentConfig::getId)
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
		Assertion.check()
				.isNotNull(aspect.getAnnotationType());
		return aspect;
	}

	private static ProxyMethod createProxyMethod(final Container container, final ProxyMethodConfig proxyMethodConfig) {
		// création de l'instance du composant
		final ProxyMethod proxyMethod = DIInjector.newInstance(proxyMethodConfig.getProxyMethodClass(), container);
		//---
		Assertion.check()
				.isNotNull(proxyMethod.getAnnotationType());
		return proxyMethod;
	}

	private void registerAspect(final Aspect aspect) {
		Assertion.check()
				.isNotNull(aspect)
				.isTrue(aspects.stream().noneMatch(a -> a.getClass().equals(aspect.getClass())),
						"aspect {0} already registered with the same class", aspect.getClass())
				.isTrue(aspects.stream().noneMatch(a -> a.getAnnotationType().equals(aspect.getAnnotationType())),
						"aspect {0} already registered with the same annotation", aspect.getClass());
		//-----
		aspects.add(aspect);
	}

	private void registerProxyMethod(final ProxyMethod proxyMethod) {
		Assertion.check()
				.isNotNull(proxyMethod)
				.isTrue(proxyMethods.stream().noneMatch(a -> a.getClass().equals(proxyMethod.getClass())),
						"proxy {0} already registered with the same class", proxyMethod.getClass())
				.isTrue(proxyMethods.stream().noneMatch(p -> p.getAnnotationType().equals(proxyMethod.getAnnotationType())),
						"proxy {0} already registered with the same annotation", proxyMethod.getClass());
		//-----
		proxyMethods.add(proxyMethod);
	}

	private <C extends CoreComponent> C injectAspects(final C instance, final Class implClass) {
		//2. AOP , a new instance is created when aspects are injected in the previous instance
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createAspectsByMethod(implClass, aspects);
		if (!joinPoints.isEmpty()) {
			return aopPlugin.wrap(instance, joinPoints);
		}
		return instance;
	}

	private static <C extends CoreComponent> C createInstance(
			final Container container,
			final Optional<ParamManager> paramManagerOpt,
			final CoreComponentConfig componentConfig) {
		return (C) createInstance(componentConfig.getImplClass(), container, paramManagerOpt, componentConfig.getParams());
	}

	//ici
	private CoreComponent createComponentWithOptions(
			final Optional<ParamManager> paramManagerOpt,
			final ComponentUnusedKeysContainer componentContainer,
			final CoreComponentConfig componentConfig) {
		Assertion.check().isFalse(componentConfig.isAmplifier(),
				"a no-amplifier component is expected");
		//---
		// 1. An instance is created
		final CoreComponent instance = createInstance(componentContainer, paramManagerOpt, componentConfig);

		//2. AOP , a new instance is created when aspects are injected in the previous instance
		return injectAspects(instance, componentConfig.getImplClass());
	}

	private Amplifier createAmplifier(
			//	final Optional<ParamManager> paramManagerOpt,
			final CoreComponentConfig coreComponentConfig) {
		Assertion.check()
				.isTrue(coreComponentConfig.isAmplifier(), "an amplifier component is expected");
		//---
		//1. AOP : finds all aspects
		final Map<Method, List<Aspect>> aspectsByMethod = ComponentAspectUtil.createAspectsByMethod(coreComponentConfig.getApiClass().get(), aspects);

		// 2. An instance is created and all aspects are injected
		return AmplifierFactory.createAmplifier(
				(Class<? extends Amplifier>) coreComponentConfig.getApiClass().get(),
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
		Assertion.check()
				.isNotNull(paramManagerOpt)
				.isNotNull(params);
		// ---
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOpt, params);
		final Container dualContainer = new ComponentDualContainer(container, paramsContainer);
		//---
		final T component = DIInjector.newInstance(clazz, dualContainer);
		Assertion.check()
				.isTrue(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}' in component '{1}'", paramsContainer.getUnusedKeys(), clazz);
		return component;
	}

	public static void injectMembers(
			final Object instance,
			final Container container,
			final Optional<ParamManager> paramManagerOpt,
			final Map<String, String> params) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(container)
				.isNotNull(paramManagerOpt)
				.isNotNull(params);
		//-----
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOpt, params);
		final Container dualContainer = new ComponentDualContainer(container, paramsContainer);
		DIInjector.injectMembers(instance, dualContainer);
	}

	public void endLoading() {
		componentSpaceWritable.closeRegistration();
	}
}
