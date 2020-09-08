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
package io.vertigo.core.daemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

/**
 * @author mlaroche, pchretien, npiedeloup
 */
public final class DaemonManagerTest extends AbstractTestCaseJU5 {

	@Inject
	private DaemonManager daemonManager;
	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private FakeComponent fakeComponent;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("myAspects")
						.addAspect(DaemonFakeAspect.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakeComponent.class)
						.build())
				.build();
	}

	@Test
	public void testSimple() throws Exception {
		DaemonStat daemonStat = daemonManager.getStats().stream()
				.filter(stat -> FakeComponent.SIMPLE_DAEMON_NAME.equals(stat.getDaemonName()))
				.findFirst().get();
		assertEquals(0, daemonStat.getCount());
		assertEquals(0, daemonStat.getFailures());
		assertEquals(0, daemonStat.getSuccesses());
		assertEquals(DaemonStat.Status.pending, daemonStat.getStatus());

		assertEquals(0, fakeComponent.getExecutionCount());
		// -----
		Thread.sleep(5000); //soit deux execs

		daemonStat = daemonManager.getStats().stream()
				.filter(stat -> FakeComponent.SIMPLE_DAEMON_NAME.equals(stat.getDaemonName()))
				.findFirst().get();
		assertEquals(2, daemonStat.getCount());
		assertEquals(1, daemonStat.getFailures());
		assertEquals(1, daemonStat.getSuccesses());
		assertEquals(DaemonStat.Status.pending, daemonStat.getStatus());

		assertTrue(fakeComponent.getExecutionCount() > 0);

		final HealthCheck daemonsExecHealthCheck = analyticsManager.getHealthChecks()
				.stream()
				.filter(healtChk -> "daemons".equals(healtChk.getFeature()) && "lastExecs".equals(healtChk.getName()))
				.findFirst()
				.get();

		assertTrue(daemonsExecHealthCheck.getMeasure().getStatus() == HealthStatus.GREEN);

	}

}
