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
package io.vertigo.commons.analytics.metric;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.metric.data.DummyMetricsProvider;
import io.vertigo.commons.impl.analytics.AnalyticsManagerImpl;

/**
 * @author mlaroche
 */
public final class MetricAnalyticsTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("test-metric")
						.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class)
						.addComponent(DummyMetricsProvider.class)
						.build())
				.build();
	}

	@Inject
	private AnalyticsManager analyticsManager;

	@Test
	public void testAnalyze() {
		final List<Metric> metrics = analyticsManager.getMetrics();
		//---
		Assertions.assertEquals(1, metrics.size());
	}
}
