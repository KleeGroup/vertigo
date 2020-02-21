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
package io.vertigo.dynamox.metric.task;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.analytics.metric.MetricBuilder;
import io.vertigo.commons.analytics.metric.Metrics;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.component.Component;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

/**
 * Implémentation de TaskReportingManager.
 *
 * @author tchassagnette
 */
public final class TasksMetricsProvider implements Component {

	private final VTransactionManager transactionManager;
	private final TaskManager taskManager;

	@Inject
	public TasksMetricsProvider(final VTransactionManager transactionManager, final TaskManager taskManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		//-----
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;

	}

	@Metrics
	public List<Metric> getTasksRequestSizeMetric() {
		return Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.map(taskDefinition -> Metric.builder()
						.withName("taskRequestSize")
						.withFeature(taskDefinition.getName())
						.withValue(Double.valueOf(taskDefinition.getRequest().length()))
						.withSuccess()
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getTasksJoinMetric() {
		return Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.map(taskDefinition -> {
					final double joinCount = taskDefinition.getRequest().toUpperCase(Locale.ENGLISH).split("JOIN").length - 1d;
					final double fromCount = taskDefinition.getRequest().toUpperCase(Locale.ENGLISH).split("FROM ").length - 1d;
					return Metric.builder()
							.withName("taskJoinCount")
							.withFeature(taskDefinition.getName())
							.withValue(joinCount + fromCount)
							.withSuccess()
							.build();
				})
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getTasksSubRequestMetric() {
		return Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.map(taskDefinition -> Metric.builder()
						.withName("taskSubrequestsCount")
						.withFeature(taskDefinition.getName())
						.withValue(taskDefinition.getRequest().toUpperCase(Locale.ENGLISH).split("SELECT").length - 1d)
						.withSuccess()
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getTasksPerformanceMetric() {
		return Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.filter(TasksMetricsProvider::canBeExecutedForMetric)
				.map(taskDefinition -> {
					try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
						return doExecute(taskDefinition);
					}
				})
				.collect(Collectors.toList());

	}

	private static boolean canBeExecutedForMetric(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---
		return TaskEngineSelect.class.isAssignableFrom(taskDefinition.getTaskEngineClass()) && !hasNotNullOutParams(taskDefinition);
	}

	private Metric doExecute(final TaskDefinition taskDefinition) {
		final MetricBuilder metricBuilder = Metric.builder()
				.withName("taskExecutionTime")
				.withFeature(taskDefinition.getName());

		try {
			final TaskPopulator taskPopulator = new TaskPopulator(taskDefinition);
			final Task task = taskPopulator.populateTask();
			final double startTime = System.currentTimeMillis();
			taskManager.execute(task);
			final double endTime = System.currentTimeMillis();
			final double executionTime = endTime - startTime;
			return metricBuilder
					.withSuccess()
					.withValue(executionTime)
					.build();
		} catch (final Exception e) {
			return metricBuilder
					.withError()
					.build();
		}
	}

	private static boolean hasNotNullOutParams(final TaskDefinition taskDefinition) {
		return taskDefinition.getOutAttributeOption().isPresent()
				&& taskDefinition.getOutAttributeOption().get().isRequired();
	}

}
