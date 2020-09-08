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
package io.vertigo.core.node.config;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;

/**
 * The NodeConfig class defines the config.
 * The node is built from this config.
 *
 * NodeConfig must be created using the NodeConfigBuilder.
 * @author pchretien
 */
public final class NodeConfig {

	private static final int PRINT_MODULE_SIZE = 24;
	private static final int PRINT_COMPONENT_SIZE = 22;
	private static final int PRINT_PLUGIN_SIZE = 44;

	private final String appName;
	private final String nodeId;
	private final Optional<String> endPointOpt;
	//---
	private final BootConfig bootConfig;
	private final List<ModuleConfig> modules;
	private final List<ComponentInitializerConfig> initializers;

	NodeConfig(
			final String appName,
			final String nodeId,
			final Optional<String> endPointOpt,
			final BootConfig bootConfig,
			final List<ModuleConfig> moduleConfigs,
			final List<ComponentInitializerConfig> componentInitializerConfigs) {
		Assertion.check()
				.isNotBlank(appName)
				.isNotBlank(nodeId)
				.isNotNull(endPointOpt)
				.isNotNull(bootConfig)
				.isNotNull(moduleConfigs)
				.isNotNull(componentInitializerConfigs);
		//---
		this.appName = appName;
		this.nodeId = nodeId;
		this.endPointOpt = endPointOpt;
		//---
		this.bootConfig = bootConfig;
		modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
		initializers = Collections.unmodifiableList(new ArrayList<>(componentInitializerConfigs));
	}

	/**
	 * Static method factory for NodeConfigBuilder
	 * @return NodeConfigBuilder
	 */
	public static NodeConfigBuilder builder() {
		return new NodeConfigBuilder();
	}

	/**
	 *
	 * @return the config of the boot
	 */
	public BootConfig getBootConfig() {
		return bootConfig;
	}

	/**
	 * @return list of the configs of the modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	/**
	 *
	 * @return List of the config of the initializers
	 */
	public List<ComponentInitializerConfig> getComponentInitializerConfigs() {
		return initializers;
	}

	/**
	 * An node is composed of multiple nodes.
	 * AppName is the common name that define the application as a whole. (ex: Facebook, Pharos...)
	 * @return the logical name of the app
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * An node is composed of multiple nodes.
	 * @return the random uuid of a node
	 */
	public String getNodeId() {
		return nodeId;
	}

	public Optional<String> getEndPoint() {
		return endPointOpt;
	}

	//=========================================================================
	//======================Gestion des affichages=============================
	//=========================================================================
	/**
	 * Allows to print a short description of the config.
	 * @param out Out
	 */
	public void print(final PrintStream out) {
		Assertion.check()
				.isNotNull(out);
		doPrint(out);
	}

	private static void doPrintLine(final PrintStream out) {
		out.println("+-------------------------+------------------------+----------------------------------------------+");
	}

	/**
	 * Affiche dans la console le logo.
	 * @param out Flux de sortie des informations
	 */
	private void doPrint(final PrintStream out) {
		doPrintLine(out);
		printComponent(out, "modules", "components", "plugins");
		doPrintLine(out);
		printComponents(out, "boot", bootConfig.getComponentConfigs());
		for (final ModuleConfig moduleConfig : modules) {
			doPrintLine(out);
			printModule(out, moduleConfig);
		}
		doPrintLine(out);
	}

	private static void printModule(final PrintStream out, final ModuleConfig moduleConfig) {
		printComponents(out, moduleConfig.getName(), moduleConfig.getComponentConfigs());
	}

	private static void printComponents(final PrintStream out, final String moduleName, final List<CoreComponentConfig> componentConfigs) {
		boolean first = true;
		for (final CoreComponentConfig componentConfig : componentConfigs) {
			final String componentClassName = componentConfig.getApiClass()
					.orElseGet(componentConfig::getImplClass)
					.getSimpleName();
			printComponent(out, first ? moduleName : null, componentClassName, null);
			first = false;
		}
	}

	private static void printComponent(final PrintStream out, final String column1, final String column2, final String column3) {
		out.println("|" + truncate(column1, PRINT_MODULE_SIZE) + " | " + truncate(column2, PRINT_COMPONENT_SIZE) + " | " + truncate(column3, PRINT_PLUGIN_SIZE) + " |");
	}

	private static String truncate(final String value, final int size) {
		final String result = (value != null ? value : "") + "                                                                  ";
		return result.substring(0, size);
	}

}
