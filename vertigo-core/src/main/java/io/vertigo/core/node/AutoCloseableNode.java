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
package io.vertigo.core.node;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.core.node.component.ComponentSpace;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.component.loader.ComponentSpaceLoader;
import io.vertigo.core.node.component.loader.ComponentSpaceWritable;
import io.vertigo.core.node.config.ComponentInitializerConfig;
import io.vertigo.core.node.config.LogConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.loader.DefinitionSpaceLoader;
import io.vertigo.core.node.definition.loader.DefinitionSpaceWritable;

/**
 * The node class is the core of vertigo.
 * @author pchretien
 */
public final class AutoCloseableNode implements Node, AutoCloseable {
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

	private static final Logger LOGGER = LogManager.getLogger(AutoCloseableNode.class);

	private static final AtomicReference<Node> CURRENT_APP_REF = new AtomicReference<>();

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
	public AutoCloseableNode(final NodeConfig nodeConfig) {
		Assertion.check()
				.isNotNull(nodeConfig);
		//-----
		start = Instant.now();
		this.nodeConfig = nodeConfig;
		setCurrentApp(this);
		state = State.STARTING;
		//--
		try {
			//-- 0. Start logger
			nodeConfig.getBootConfig().getLogConfig().ifPresent(AutoCloseableNode::initLog);

			//Dans le cas de boot il n,'y a ni initializer, ni aspects, ni definitions
			//Creates and register all components (and aspects and Proxies).
			//all components can be parameterized
			ComponentSpaceLoader.startLoading(componentSpaceWritable, nodeConfig.getBootConfig().getAopPlugin())
					//-- 1.a - BootStrap : create native components : ResourceManager, ParamManager, LocaleManager
					.loadBootComponents(nodeConfig.getBootConfig().getComponentConfigs())
					//-- 1.b - other components
					.loadAllComponentsAndAspects(nodeConfig.getModuleConfigs())
					.endLoading();
			//---- Print components
			Logo.printCredits(System.out);
			if (nodeConfig.getBootConfig().isVerbose()) {
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
			//-----3.a Starts activeable components
			componentSpaceWritable.start();
			//-----3.b Init all Initializers
			/*
			 * componentInitializers are created and the init() is called on each.
			 * Notice :
			 * these components are not registered in the componentSpace.
			 * that's why this kind of component can't be activeable.
			 */
			initializeAllComponents();

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
		Assertion.check()
				.isTrue(state == State.STARTING, "Applisteners can't be registered at runtime")
				.isNotNull(preActivateFunction);
		//-----
		preActivateFunctions.add(preActivateFunction);
	}

	private void appPreActivate() {
		preActivateFunctions
				.forEach(Runnable::run);
	}

	private void appStop() {
		componentSpaceWritable.stop();
		definitionSpaceWritable.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		Assertion.check()
				.isTrue(state == State.ACTIVE || state == State.STARTING, "App with a state '{0}' can not be be closed", state);
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
			resetCurrentApp();
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
			Assertion.check()
					.isFalse(Activeable.class.isAssignableFrom(componentInitializerConfig.getInitializerClass()),
							"The initializer '{0}' can't be activeable", componentInitializerConfig.getInitializerClass());
			final ComponentInitializer componentInitializer = DIInjector.newInstance(componentInitializerConfig.getInitializerClass(), componentSpaceWritable);
			componentInitializer.init();
		}
	}

	private static void initLog(final LogConfig log4Config) {
		Assertion.check()
				.isNotNull(log4Config);
		//-----
		final String log4jFileName = log4Config.getFileName();
		Assertion.check()
				.isTrue(log4jFileName.endsWith(".xml"), "Use the XML format for log4j configurations (instead of : {0}).", log4jFileName);
		final URL url = Node.class.getResource(log4jFileName);
		if (url != null) {
			Configurator.initialize("definedLog4jContext", Thread.currentThread().getContextClassLoader(), log4jFileName);
			LogManager.getRootLogger().info("Log4J configuration chargée (resource) : {}", url.getFile());
		} else {
			Assertion.check()
					.isTrue(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
			// Avec configureAndWatch (utilise un anonymous thread)
			// on peut modifier à chaud le fichier de conf log4j
			// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
			Configurator.initialize("definedLog4jContext", null, log4jFileName);
			LogManager.getRootLogger().info("Log4J configuration chargée (fichier) : {}", log4jFileName);
		}
	}

	private static void setCurrentApp(final Node node) {
		Assertion.check()
				.isNotNull(node);
		//--
		final boolean success = CURRENT_APP_REF.compareAndSet(null, node);
		//--
		Assertion.check()
				.isTrue(success, "current App is already set");
	}

	private static void resetCurrentApp() {
		CURRENT_APP_REF.set(null);
	}

	/**
	 * @return Application
	 */
	static Node getCurrentApp() {
		final Node node = CURRENT_APP_REF.get();
		Assertion.check()
				.isNotNull(node, "node has not been started");
		//no synchronized for perf purpose
		return node;
	}

}
