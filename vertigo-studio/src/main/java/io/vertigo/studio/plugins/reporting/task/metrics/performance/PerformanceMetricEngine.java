/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.ReportMetricEngine;
import io.vertigo.studio.reporting.ReportMetric;
import io.vertigo.studio.reporting.ReportMetric.Status;

/**
 * Plugin de calcul du temps d'exécution d'une requête.
 *
 * @author tchassagnette
 */
public final class PerformanceMetricEngine implements ReportMetricEngine<TaskDefinition> {
	private final TaskManager taskManager;

	/**
	 * Constructeur apr défaut.
	 * @param taskManager Manager des tasks
	 */
	public PerformanceMetricEngine(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//-----
		this.taskManager = taskManager;
	}

	/** {@inheritDoc} */
	@Override
	public ReportMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		try {
			return doExecute(taskDefinition);
		} catch (final Exception e) {
			//throw new RiException("Erreur du plugin perfs", e);
			return buildPerformanceMetric(Status.ERROR, null, createValueInformation(e));
		}
	}

	private static String createValueInformation(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	private static ReportMetric buildPerformanceMetric(final Status status, final Long executionTime, final String valueInformation) {
		return ReportMetric.builder()
				.withValue(executionTime)
				.withTitle("Temps d'exécution")
				.withUnit("ms")
				.withValueInformation(valueInformation)
				.withStatus(status)
				.build();
	}

	private ReportMetric doExecute(final TaskDefinition taskDefinition) {
		//System.out.println(">>>>" + currentTask.getEngineClass().getCanonicalName());
		if (TaskEngineSelect.class.isAssignableFrom(taskDefinition.getTaskEngineClass()) && !hasNotNullOutParams(taskDefinition)) {
			//	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>" + currentTask.getEngineClass().getCanonicalName());
			final TaskPopulator taskPopulator = new TaskPopulator(taskDefinition);
			final Task task = taskPopulator.populateTask();
			final long startTime = System.currentTimeMillis();
			/*TaskResult result =*/taskManager.execute(task);
			//on n'utilise pas le resultat
			final long endTime = System.currentTimeMillis();
			final long executionTime = endTime - startTime;
			return buildPerformanceMetric(Status.EXECUTED, executionTime, null);
		}
		//Le test n'a pas de sens.
		return buildPerformanceMetric(Status.REJECTETD, null, null);
	}

	private static boolean hasNotNullOutParams(final TaskDefinition taskDefinition) {
		return taskDefinition.getOutAttributeOption().isPresent()
				&& taskDefinition.getOutAttributeOption().get().isRequired();
	}
}
