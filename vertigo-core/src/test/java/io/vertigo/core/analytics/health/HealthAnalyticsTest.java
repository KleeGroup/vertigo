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
package io.vertigo.core.analytics.health;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.data.FailedComponentChecker;
import io.vertigo.core.analytics.health.data.SuccessComponentChecker;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public class HealthAnalyticsTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("checkers")
						.addComponent(FailedComponentChecker.class)
						.addComponent(SuccessComponentChecker.class)
						.build())
				.build();
	}

	@Test
	void testFailComponent() {
		final List<HealthCheck> failedHealthChecks = findHealthChecksByName("failure");
		//---
		Assertions.assertEquals(1, failedHealthChecks.size());
		Assertions.assertEquals(HealthStatus.RED, failedHealthChecks.get(0).getMeasure().getStatus());
		Assertions.assertTrue(failedHealthChecks.get(0).getMeasure().getCause() instanceof VSystemException);
	}

	@Test
	void testSuccessComponent() {
		final List<HealthCheck> successHealthChecks = findHealthChecksByName("success");
		//---
		Assertions.assertEquals(1, successHealthChecks.size());
		Assertions.assertEquals(HealthStatus.GREEN, successHealthChecks.get(0).getMeasure().getStatus());
	}

	@Test
	void testAggregate() {
		final List<HealthCheck> successHealthChecks = findHealthChecksByName("success");
		final List<HealthCheck> failedHealthChecks = findHealthChecksByName("failure");
		//---
		Assertions.assertEquals(HealthStatus.GREEN, analyticsManager.aggregate(successHealthChecks));
		Assertions.assertEquals(HealthStatus.RED, analyticsManager.aggregate(failedHealthChecks));

	}

	private List<HealthCheck> findHealthChecksByName(final String name) {
		Assertion.check().isNotBlank(name);
		//---
		return analyticsManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> name.equals(healthCheck.getName()))
				.collect(Collectors.toList());
	}
}
