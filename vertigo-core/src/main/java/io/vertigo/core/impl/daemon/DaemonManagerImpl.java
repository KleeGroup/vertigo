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
package io.vertigo.core.impl.daemon;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.util.ClassUtil;

/**
 * Manager of all the daemons.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
public final class DaemonManagerImpl implements DaemonManager, Activeable, SimpleDefinitionProvider {

	private final DaemonExecutor daemonExecutor = new DaemonExecutor();
	private final AnalyticsManager analyticsManager;

	/**
	 * Construct an instance of DaemonManagerImpl.
	 */
	@Inject
	public DaemonManagerImpl(final AnalyticsManager analyticsManager) {
		Assertion.check().isNotNull(analyticsManager);
		//---
		this.analyticsManager = analyticsManager;
		Node.getNode().registerPreActivateFunction(this::startAllDaemons);

	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Node.getNode().getNodeConfig().getBootConfig().getAopPlugin();
		return Node.getNode().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createDaemonDefinitions(Node.getNode().getComponentSpace().resolve(id, CoreComponent.class), aopPlugin).stream())
				.collect(Collectors.toList());
	}

	private List<DaemonDefinition> createDaemonDefinitions(final CoreComponent component, final AopPlugin aopPlugin) {
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(DaemonScheduled.class))
				.map(
						method -> {
							Assertion.check().isTrue(method.getParameterTypes().length == 0,
									"Method {0} on component {1} cannot have any parameter to be used as a daemon", method.getName(), component.getClass().getName());
							//---
							final DaemonScheduled daemonSchedule = method.getAnnotation(DaemonScheduled.class);
							final Supplier<Daemon> daemonSupplier;
							if (daemonSchedule.analytics()) {
								// if analytics is enabled (by default) we trace the execution with a tracer
								daemonSupplier = () -> () -> analyticsManager.trace(
										"daemon",
										daemonSchedule.name(),
										tracer -> ClassUtil.invoke(component, method));
							} else {
								// otherwise we just execute it
								daemonSupplier = () -> () -> ClassUtil.invoke(component, method);
							}
							return new DaemonDefinition(
									daemonSchedule.name(),
									daemonSupplier,
									daemonSchedule.periodInSeconds());
						})
				.collect(Collectors.toList());

	}

	/** {@inheritDoc} */
	@Override
	public List<DaemonStat> getStats() {
		return daemonExecutor.getStats();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		daemonExecutor.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		daemonExecutor.stop();
	}

	/**
	 * Démarre un démon.
	 * Celui-ci aura été préalablement enregistré.
	 * Il sera lancé puis réexécuté périodiquement.
	 * L'instance du démon est créée par injection de dépendances.
	 *
	 * @param daemonDefinition Le démon à lancer.
	 */
	private void startDaemon(final DaemonDefinition daemonDefinition) {
		Assertion.check().isNotNull(daemonDefinition);
		// -----
		daemonExecutor.scheduleDaemon(daemonDefinition);
	}

	/**
	 * Démarre l'ensemble des démons préalablement enregistré dans le spaceDefinition.
	 */
	private void startAllDaemons() {
		Node.getNode().getDefinitionSpace().getAll(DaemonDefinition.class).stream()
				.forEach(this::startDaemon);
	}

	@HealthChecked(name = "lastExecs", feature = "daemons")
	public HealthMeasure checkDaemonsExecs() {
		final List<DaemonStat> daemonStats = getStats();
		final long failureCount = daemonStats.stream()
				.filter(daemonStat -> daemonStat.getCount() > 0) // to have a real indicator we use only daemon that have been executed at least once
				.filter(daemonStat -> !daemonStat.isLastExecSuccess())
				.count();
		//---
		final HealthMeasureBuilder healthMeasure = HealthMeasure.builder();
		if (failureCount == 0) {
			return healthMeasure
					.withGreenStatus()
					.build();
		} else if (failureCount < daemonStats.size()) {
			return healthMeasure
					.withYellowStatus("At least one daemon failed", null)
					.build();
		}
		return healthMeasure
				.withRedStatus("All daemons failed", null)
				.build();

	}

}
