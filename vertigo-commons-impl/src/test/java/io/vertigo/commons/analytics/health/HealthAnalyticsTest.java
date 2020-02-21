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
package io.vertigo.commons.analytics.health;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.health.data.FailedComponentChecker;
import io.vertigo.commons.analytics.health.data.RedisHealthChecker;
import io.vertigo.commons.analytics.health.data.SuccessComponentChecker;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

public class HealthAnalyticsTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		final String redisHost = "redis-pic.part.klee.lan.net";
		final int redisPort = 6379;
		final int redisDatabase = 15;

		return NodeConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(new CommonsFeatures()
						.withRedisConnector(Param.of("host", redisHost), Param.of("port", Integer.toString(redisPort)), Param.of("database", Integer.toString(redisDatabase)))
						.build())
				.addModule(ModuleConfig.builder("checkers")
						.addComponent(RedisHealthChecker.class)
						.addComponent(FailedComponentChecker.class)
						.addComponent(SuccessComponentChecker.class)
						.build())
				.build();
	}

	@Test
	void testRedisChecker() {
		final List<HealthCheck> redisHealthChecks = findHealthChecksByName("ping")
				.stream()
				.filter(healthCheck -> "redisChecker".equals(healthCheck.getFeature()))
				.collect(Collectors.toList());
		//---
		Assertions.assertEquals(1, redisHealthChecks.size());
		Assertions.assertEquals(HealthStatus.GREEN, redisHealthChecks.get(0).getMeasure().getStatus());

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
		Assertion.checkArgNotEmpty(name);
		//---
		return analyticsManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> name.equals(healthCheck.getName()))
				.collect(Collectors.toList());
	}
}
