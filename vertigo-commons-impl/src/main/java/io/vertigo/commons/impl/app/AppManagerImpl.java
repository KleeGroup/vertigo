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
package io.vertigo.commons.impl.app;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.App;
import io.vertigo.app.Home;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.analytics.health.HealthCheck;
import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.app.Node;
import io.vertigo.commons.daemon.DaemonScheduled;
import io.vertigo.commons.plugins.app.registry.single.SingleAppNodeRegistryPlugin;
import io.vertigo.core.component.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Implementation of the NodeManager.
 * @author mlaroche
 *
 */
public final class AppManagerImpl implements AppManager, Activeable {

	private static final int HEART_BEAT_SECONDS = 60;

	private final AppNodeRegistryPlugin nodeRegistryPlugin;
	private final Map<String, AppNodeInfosPlugin> nodeInfosPluginMap = new HashMap<>();

	@Inject
	public AppManagerImpl(
			final Optional<AppNodeRegistryPlugin> nodeRegistryPluginOpt,
			final List<AppNodeInfosPlugin> nodeInfosPlugins) {
		Assertion.checkNotNull(nodeRegistryPluginOpt);
		// ---
		nodeRegistryPlugin = nodeRegistryPluginOpt.orElseGet(() -> new SingleAppNodeRegistryPlugin());
		nodeInfosPlugins
				.forEach(plugin -> {
					Assertion.checkState(!nodeInfosPluginMap.containsKey(plugin.getProtocol()), "A plugin for the protocol {0} is already registered", plugin.getProtocol());
					//---
					nodeInfosPluginMap.put(plugin.getProtocol(), plugin);
				});

	}

	@DaemonScheduled(name = "DmnUpdateNodeStatus", periodInSeconds = HEART_BEAT_SECONDS, analytics = false)
	public void updateNodeStatus() {
		nodeRegistryPlugin.updateStatus(toAppNode(Home.getApp()));
	}

	@Override
	public void start() {
		nodeRegistryPlugin.register(toAppNode(Home.getApp()));
	}

	@Override
	public void stop() {
		nodeRegistryPlugin.unregister(toAppNode(Home.getApp()));

	}

	@Override
	public Optional<Node> find(final String nodeId) {
		return nodeRegistryPlugin.find(nodeId);
	}

	@Override
	public List<Node> locateSkills(final String... skills) {
		return getTopology()
				.stream()
				.filter(node -> node.getSkills().containsAll(Arrays.asList(skills)))
				.collect(Collectors.toList());
	}

	@Override
	public List<Node> getTopology() {
		return nodeRegistryPlugin.getTopology();
	}

	@Override
	public Node getCurrentNode() {
		final String currentNodeId = Home.getApp().getNodeConfig().getNodeId();
		return find(currentNodeId)
				.orElseThrow(() -> new VSystemException("Current node with '{0}' cannot be found in the registry", currentNodeId));
	}

	@Override
	public List<Node> getDeadNodes() {
		return getTopology()
				.stream()
				// we wait two heartbeat to decide that a node is dead
				.filter(node -> node.getLastTouch().plus(2L * HEART_BEAT_SECONDS, ChronoUnit.SECONDS).isBefore(Instant.now()))
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, List<HealthCheck>> getStatus() {
		return aggregateResults(app -> getInfosPlugin(app).getStatus(app));
	}

	@Override
	public Map<String, Object> getStats() {
		return aggregateResults(app -> getInfosPlugin(app).getStats(app));
	}

	@Override
	public Map<String, String> getConfig() {
		return aggregateResults(app -> getInfosPlugin(app).getConfig(app));
	}

	private <R> Map<String, R> aggregateResults(final Function<Node, R> functionToApply) {
		return nodeRegistryPlugin
				.getTopology()
				.stream()
				.collect(Collectors.toMap(
						Node::getId,
						app -> functionToApply.apply(app)));

	}

	private AppNodeInfosPlugin getInfosPlugin(final Node app) {
		Assertion.checkState(nodeInfosPluginMap.containsKey(app.getProtocol()), "No status plugin found for the protocol {0} when reach attempt on {1} ", app.getProtocol(), app.getEndPoint());
		//---
		return nodeInfosPluginMap.get(app.getProtocol());
	}

	private static Node toAppNode(final App app) {
		return new Node(
				app.getNodeConfig().getNodeId(),
				app.getNodeConfig().getAppName(),
				NodeStatus.UP.name(),
				Instant.now(),
				app.getStart(),
				app.getNodeConfig().getEndPoint(),
				getSkills(app));
	}

	private static List<String> getSkills(final App app) {
		return app.getNodeConfig().getModuleConfigs().stream()
				.map(ModuleConfig::getName)
				.collect(Collectors.toList());
	}

	public enum NodeStatus {
		UP, DOWN;

	}

}
