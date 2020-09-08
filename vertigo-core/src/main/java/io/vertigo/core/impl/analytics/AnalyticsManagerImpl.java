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
package io.vertigo.core.impl.analytics;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.impl.analytics.health.HealthAnalyticsUtil;
import io.vertigo.core.impl.analytics.metric.MetricAnalyticsUtil;
import io.vertigo.core.impl.analytics.process.ProcessAnalyticsImpl;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager, SimpleDefinitionProvider {

	private final ProcessAnalyticsImpl processAnalyticsImpl;
	private final List<AnalyticsConnectorPlugin> processConnectorPlugins;

	private final boolean enabled;

	/**
	 * Constructor.
	 * @param processConnectorPlugins list of connectors to trace processes
	 */
	@Inject
	public AnalyticsManagerImpl(
			final List<AnalyticsConnectorPlugin> processConnectorPlugins) {
		Assertion.check().isNotNull(processConnectorPlugins);
		//---
		processAnalyticsImpl = new ProcessAnalyticsImpl();
		this.processConnectorPlugins = processConnectorPlugins;
		// by default if no connector is defined we disable the collect
		enabled = !this.processConnectorPlugins.isEmpty();
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// here all
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Node.getNode().getNodeConfig().getBootConfig().getAopPlugin();
		return Node.getNode().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> Stream.concat(
						//health
						HealthAnalyticsUtil.createHealthCheckDefinitions(id, Node.getNode().getComponentSpace().resolve(id, CoreComponent.class), aopPlugin).stream(),
						//metrics
						MetricAnalyticsUtil.createMetricDefinitions(id, Node.getNode().getComponentSpace().resolve(id, CoreComponent.class), aopPlugin).stream()))
				.collect(Collectors.toList());
	}

	/*----------------- Process ------------------*/

	/** {@inheritDoc} */
	@Override
	public void trace(final String category, final String name, final Consumer<ProcessAnalyticsTracer> consumer) {
		processAnalyticsImpl.trace(category, name, consumer, this::onClose);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O traceWithReturn(final String category, final String name, final Function<ProcessAnalyticsTracer, O> function) {
		return processAnalyticsImpl.traceWithReturn(category, name, function, this::onClose);
	}

	/** {@inheritDoc} */
	@Override
	public void addProcess(final AProcess process) {
		onClose(process);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ProcessAnalyticsTracer> getCurrentTracer() {
		if (!enabled) {
			return Optional.empty();
		}
		// When collect feature is enabled
		return processAnalyticsImpl.getCurrentTracer();
	}

	private void onClose(final AProcess process) {
		Assertion.check().isNotNull(process);
		//---
		processConnectorPlugins.forEach(
				processConnectorPlugin -> processConnectorPlugin.add(process));
	}

	/*----------------- Health ------------------*/

	/**
	 * Daemon to retrieve healthChecks and add them to the connectors
	 */
	@DaemonScheduled(name = "DmnAnalyticsHealth", periodInSeconds = 60 * 60) //every hour
	public void sendHealthChecks() {
		if (enabled) {
			final List<HealthCheck> healthChecks = getHealthChecks();
			processConnectorPlugins.forEach(
					connectorPlugin -> healthChecks.forEach(connectorPlugin::add));
		}
	}

	@Override
	public List<HealthCheck> getHealthChecks() {
		return HealthAnalyticsUtil.getHealthChecks();
	}

	@Override
	public HealthStatus aggregate(final List<HealthCheck> healthChecks) {
		return HealthAnalyticsUtil.aggregate(healthChecks);
	}

	/*----------------- Metrics ------------------*/

	/**
	 * Daemon to retrieve metrics and add them to the connectors
	 */
	@DaemonScheduled(name = "DmnAnalyticsMetric", periodInSeconds = 60 * 60) //every hour
	public void sendMetrics() {
		if (enabled) {
			final List<Metric> metrics = getMetrics();
			processConnectorPlugins.forEach(
					connectorPlugin -> metrics.forEach(connectorPlugin::add));
		}
	}

	@Override
	public List<Metric> getMetrics() {
		return MetricAnalyticsUtil.getMetrics();
	}

}
