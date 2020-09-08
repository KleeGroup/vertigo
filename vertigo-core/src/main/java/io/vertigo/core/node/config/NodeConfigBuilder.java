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

import java.util.Optional;
import java.util.UUID;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.core.util.ListBuilder;

/**
 * The NodeConfigBuilder builder allows you to create an NodeConfig using a fluent, simple style .
 *
 * @author npiedeloup, pchretien
 */
public final class NodeConfigBuilder implements Builder<NodeConfig> {

	private String myAppName;
	private String myNodeId;
	private String myEndPoint;
	//---
	private final ListBuilder<ModuleConfig> myModuleConfigsBuilder = new ListBuilder<>();
	private BootConfig myBootConfig;
	private final ListBuilder<ComponentInitializerConfig> myComponentInitializerConfigsBuilder = new ListBuilder<>();

	/**
	 * Constructor.
	 */
	NodeConfigBuilder() {
	}

	/**
	 * Opens the bootConfigBuilder.
	 * There is exactly one BootConfig per NodeConfig.
	 * @return this builder
	 */
	public NodeConfigBuilder withBoot(BootConfig bootConfig) {
		Assertion.check()
				.isNull(myBootConfig, "boot is already set")
				.isNotNull(bootConfig);
		//---
		this.myBootConfig = bootConfig;
		return this;
	}

	/**
	 * Associate a common name that define the application as a whole. (ex: Facebook, Pharos...) (myApp by default)
	 * @param appName the node name
	 * @return this builder
	 */
	public NodeConfigBuilder withAppName(final String appName) {
		Assertion.check()
				.isNull(myAppName, "appName '{0}' is not allowed. appName is already defined as '{1}'", appName, myAppName)
				.isNotBlank(appName);
		// ---
		myAppName = appName;
		return this;

	}

	/**
	 * Associate an id to the current node (random UUID by default)
	 * @param nodeId the node id
	 * @return this builder
	 */
	public NodeConfigBuilder withNodeId(final String nodeId) {
		Assertion.check()
				.isNull(myNodeId, "nodeId '{0}' is not allowed. nodeId is already defined as '{1}'", nodeId, myNodeId)
				.isNotBlank(nodeId);
		// ---
		myNodeId = nodeId;
		return this;

	}

	/**
	 * Associate an optional endPoint to reach the current node.
	 * @param endPoint the endPoint to reach the node (protocol:port//host)
	 * @return this builder
	 */
	public NodeConfigBuilder withEndPoint(final String endPoint) {
		Assertion.check()
				.isNull(myEndPoint, "endPoint '{0}' is not allowed. endPoint is already defined as '{1}'", endPoint, myEndPoint)
				.isNotBlank(endPoint);
		// ---
		myEndPoint = endPoint;
		return this;

	}

	/**
	 * Adds an initializer to the current config.
	 * @param componentInitializerClass Class of the initializer
	 * @return this builder
	 */
	public NodeConfigBuilder addInitializer(final Class<? extends ComponentInitializer> componentInitializerClass) {
		myComponentInitializerConfigsBuilder.add(new ComponentInitializerConfig(componentInitializerClass));
		return this;
	}

	/**
	 * Adds a a moduleConfig.
	 * @param moduleConfig the moduleConfig
	 * @return this builder
	 */
	public NodeConfigBuilder addModule(final ModuleConfig moduleConfig) {
		Assertion.check().isNotNull(moduleConfig);
		//-----
		myModuleConfigsBuilder.add(moduleConfig);
		return this;
	}

	/**
	 * Builds the nodeConfig.
	 * @return nodeConfig.
	 */
	@Override
	public NodeConfig build() {
		if (myAppName == null) {
			myAppName = "myApp";
		}
		if (myNodeId == null) {
			myNodeId = UUID.randomUUID().toString();
		}
		if (myBootConfig == null) {
			myBootConfig = BootConfig.builder().build();
		}
		//---
		return new NodeConfig(
				myAppName,
				myNodeId,
				Optional.ofNullable(myEndPoint),
				myBootConfig,
				myModuleConfigsBuilder.unmodifiable().build(),
				myComponentInitializerConfigsBuilder.unmodifiable().build());
	}

}
