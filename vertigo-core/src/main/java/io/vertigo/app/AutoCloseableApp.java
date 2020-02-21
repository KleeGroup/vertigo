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
package io.vertigo.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.app.config.ComponentInitializerConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.component.di.DIInjector;
import io.vertigo.core.component.loader.ComponentSpaceLoader;
import io.vertigo.core.component.loader.ComponentSpaceWritable;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.loader.DefinitionSpaceLoader;
import io.vertigo.core.definition.loader.DefinitionSpaceWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * The app class is the core of vertigo.
 * @author pchretien
 */
public final class AutoCloseableApp implements App, AutoCloseable {
	private enum State {
		/** Components are starting*/
		STARTING,
		/** Components are started*/
		ACTIVE,
		/** Components are stopping*/
		STOPPING,
		/** App is closed, good bye !*/
		CLOSED
	}

	private static final Logger LOGGER = LogManager.getLogger(AutoCloseableApp.class);

	//Start : used to have 'uptime'
	private final Instant start;
	private final NodeConfig nodeConfig;
	private State state;

	private final DefinitionSpaceWritable definitionSpaceWritable = new DefinitionSpaceWritable();
	private final ComponentSpaceWritable componentSpaceWritable = new ComponentSpaceWritable();

	//à remplacer par event ??
	private final List<Runnable> preActivateFunctions = new ArrayList<>();

	/**
	 * Constructor.
	 * @param nodeConfig Application configuration
	 */
	public AutoCloseableApp(final NodeConfig nodeConfig) {
		Assertion.checkNotNull(nodeConfig);
		//-----
		start = Instant.now();
		this.nodeConfig = nodeConfig;
		Home.setApp(this);
		state = State.STARTING;
		//--
		try {
			//--0. BootStrap : create native components : ResourceManager, ParamManager, LocaleManager
			final Boot boot = new Boot(nodeConfig.getBootConfig());
			boot.init(); //A faire créer par Boot : stratégie de chargement des composants à partir de ...

			//Dans le cas de boot il n,'y a ni initializer, ni aspects, ni definitions
			//Creates and register all components (and aspects and Proxies).
			//all components can be parameterized
			ComponentSpaceLoader.startLoading(componentSpaceWritable, nodeConfig.getBootConfig().getAopPlugin())
					.loadBootComponents(nodeConfig.getBootConfig().getComponentConfigs())
					.loadAllComponentsAndAspects(nodeConfig.getModuleConfigs())
					.endLoading();
			//---- Print components
			if (nodeConfig.getBootConfig().isVerbose()) {
				Logo.printCredits(System.out);
				nodeConfig.print(System.out);
			}
			//--2 Loads all definitions
			//-----a Loads all definitions provided by DefinitionProvider
			//-----b Loads all definitions provided by components
			DefinitionSpaceLoader.startLoading(definitionSpaceWritable, componentSpaceWritable)
					.loadDefinitions(nodeConfig.getModuleConfigs())
					.loadDefinitionsFromComponents()
					.endLoading();

			//--3. init (Init all Initializers and starts activeable components)
			//-----3.a Init all Initializers
			/*
			 * componentInitializers are created and the init() is called on each.
			 * Notice :
			 * these components are not registered in the componentSpace.
			 * that's why this kind of component can't be activeable.
			 */
			initializeAllComponents();

			//-----3.b Starts activeable components
			componentSpaceWritable.start();

			//--4. App is active with a special hook
			//-----4.a Hook  : post just in case
			appPreActivate();
			//-----4.b
			state = State.ACTIVE;
		} catch (final Exception e) {
			close();
			throw new IllegalStateException("an error occured when starting", e);
		}
	}

	@Override
	public void registerPreActivateFunction(final Runnable preActivateFunction) {
		Assertion.checkArgument(State.STARTING.equals(state), "Applisteners can't be registered at runtime");
		Assertion.checkNotNull(preActivateFunction);
		//-----
		preActivateFunctions.add(preActivateFunction);
	}

	private void appPreActivate() {
		preActivateFunctions
				.forEach(postStartFunction -> postStartFunction.run());
	}

	private void appStop() {
		componentSpaceWritable.stop();
		definitionSpaceWritable.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		Assertion.checkState(state == State.ACTIVE || state == State.STARTING, "App with a state '{0}' can not be be closed", state);
		state = State.STOPPING;
		//-----
		try {
			appStop();
		} catch (final Exception e) {
			LOGGER.error("an error occured when stopping", e);
			//Quel que soit l'état, on part en échec de l'arrét.
			throw WrappedException.wrap(e, "an error occured when stopping");
		} finally {
			state = State.CLOSED;
			Home.resetApp();
		}
	}

	@Override
	public Instant getStart() {
		return start;
	}

	@Override
	public NodeConfig getNodeConfig() {
		return nodeConfig;
	}

	@Override
	public DefinitionSpace getDefinitionSpace() {
		//We publish the Read Only version
		return definitionSpaceWritable;
	}

	@Override
	public ComponentSpace getComponentSpace() {
		//We publish the Read Only version
		return componentSpaceWritable;
	}

	private void initializeAllComponents() {
		for (final ComponentInitializerConfig componentInitializerConfig : nodeConfig.getComponentInitializerConfigs()) {
			Assertion.checkArgument(!Activeable.class.isAssignableFrom(componentInitializerConfig.getInitializerClass()),
					"The initializer '{0}' can't be activeable", componentInitializerConfig.getInitializerClass());
			final ComponentInitializer componentInitializer = DIInjector.newInstance(componentInitializerConfig.getInitializerClass(), componentSpaceWritable);
			componentInitializer.init();
		}
	}

}
