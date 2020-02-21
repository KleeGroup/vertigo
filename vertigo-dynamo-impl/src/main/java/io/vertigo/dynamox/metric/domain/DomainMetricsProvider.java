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
package io.vertigo.dynamox.metric.domain;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.analytics.metric.MetricBuilder;
import io.vertigo.commons.analytics.metric.Metrics;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.component.Component;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.lang.Assertion;

/**
 * Composant to provide Metrics about domain
 *
 * @author pchretien
 */
public final class DomainMetricsProvider implements Component {
	private final VTransactionManager transactionManager;
	private final StoreManager storeManager;

	/**
	 * Constructor.
	 * @param transactionManager the transactionManager
	 * @param storeManager the storeManager
	 */
	@Inject
	public DomainMetricsProvider(final VTransactionManager transactionManager, final StoreManager storeManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(storeManager);
		//-----
		this.transactionManager = transactionManager;
		this.storeManager = storeManager;

	}

	@Metrics
	public List<Metric> getFieldMetrics() {
		return Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.map(dtDefinition -> {
					return Metric.builder()
							.withSuccess()
							.withName("definitionFieldCount")
							.withFeature(dtDefinition.getName())
							.withValue(Double.valueOf(dtDefinition.getFields().size()))
							.build();
				})
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getDependencyMetrics() {
		return Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.map(dtDefinition -> Metric.builder()
						.withSuccess()
						.withName("definitionUsageInDao")
						.withFeature(dtDefinition.getName())
						.withValue(countTaskDependencies(dtDefinition))
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getDomainUsageTasksMetrics() {
		return Home.getApp().getDefinitionSpace().getAll(Domain.class)
				.stream()
				.map(domain -> Metric.builder()
						.withSuccess()
						.withName("domainUsageInTasks")
						.withFeature(domain.getName())
						.withValue(countTaskDependencies(domain))
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getDomainUsageDtDefinitionMetrics() {
		return Home.getApp().getDefinitionSpace().getAll(Domain.class)
				.stream()
				.map(domain -> Metric.builder()
						.withSuccess()
						.withName("domainUsageInDtDefinitions")
						.withFeature(domain.getName())
						.withValue(countDtDefinitionDependencies(domain))
						.build())
				.collect(Collectors.toList());

	}

	private static double countTaskDependencies(final Domain domain) {
		Assertion.checkNotNull(domain);
		//---
		int count = 0;
		for (final TaskDefinition taskDefinition : Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getInAttributes()) {
				if (domain.equals(taskAttribute.getDomain())) {
					count++;
				}
			}
			if (taskDefinition.getOutAttributeOption().isPresent()) {
				if (domain.equals(taskDefinition.getOutAttributeOption().get().getDomain())) {
					count++;
				}
			}
		}
		return count;
	}

	private static double countDtDefinitionDependencies(final Domain domain) {
		Assertion.checkNotNull(domain);
		//---
		return Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.flatMap(dtDefinition -> dtDefinition.getFields().stream())
				.filter(field -> domain.equals(field.getDomain()))
				.count();
	}

	private static double countTaskDependencies(final DtDefinition dtDefinition) {
		int count = 0;
		for (final TaskDefinition taskDefinition : Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getInAttributes()) {
				count += count(dtDefinition, taskAttribute);
			}
			if (taskDefinition.getOutAttributeOption().isPresent()) {
				final TaskAttribute taskAttribute = taskDefinition.getOutAttributeOption().get();
				count += count(dtDefinition, taskAttribute);
			}
		}
		return count;
	}

	private static double count(final DtDefinition dtDefinition, final TaskAttribute taskAttribute) {
		if (taskAttribute.getDomain().getScope().isDataObject()) {
			if (dtDefinition.equals(taskAttribute.getDomain().getDtDefinition())) {
				return 1;
			}
		}
		return 0;
	}

	@Metrics
	public List<Metric> getEntityCountMetrics() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
					.stream()
					.filter(DtDefinition::isPersistent)
					.map(dtDefinition -> doExecute(dtDefinition, transaction))
					.collect(Collectors.toList());
		}

	}

	private Metric doExecute(final DtDefinition dtDefinition, final VTransactionWritable transaction) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkState(dtDefinition.isPersistent(), "Count can only be performed on persistent entities, DtDefinition '{0}' is not", dtDefinition.getName());
		//-----
		final MetricBuilder metricBuilder = Metric.builder()
				.withName("entityCount")
				.withFeature(dtDefinition.getName());
		try {
			final SqlConnection vTransactionResource = transaction.getResource(AbstractTaskEngineSQL.SQL_MAIN_RESOURCE_ID);
			if (vTransactionResource != null) {
				vTransactionResource.getJdbcConnection().rollback();
			}
			final double count = storeManager.getDataStore().count(dtDefinition);
			return metricBuilder
					.withSuccess()
					.withValue(count)
					.build();
		} catch (final Exception e) {
			return metricBuilder
					.withError()
					.build();
		}
	}
}
