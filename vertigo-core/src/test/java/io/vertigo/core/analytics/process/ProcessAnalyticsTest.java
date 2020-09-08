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
package io.vertigo.core.analytics.process;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.process.data.TestAProcessConnectorPlugin;
import io.vertigo.core.analytics.process.data.TestAnalyticsAspectServices;
import io.vertigo.core.impl.analytics.process.AnalyticsAspect;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.analytics.log.LoggerAnalyticsConnectorPlugin;

/**
 * Cas de Test JUNIT de l'API Analytics.
 *
 * @author pchretien, npiedeloup
 */
public final class ProcessAnalyticsTest extends AbstractTestCaseJU5 {
	private static final String PRICE = "PRICE";

	private static final String WEIGHT = "WEIGHT";

	/** Base de données gérant les articles envoyés dans une commande. */
	private static final String TEST_CATEGORY = "test";

	/** Logger. */
	private final Logger log = LogManager.getLogger(getClass());

	@Inject
	private AnalyticsManager analyticsManager;

	@Inject
	private TestAnalyticsAspectServices analyticsAspectServices;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withSmartLoggerAnalyticsConnector(Param.of("aggregatedBy", "test"))
						.withSocketLoggerAnalyticsConnector()
						.addAnalyticsConnectorPlugin(TestAProcessConnectorPlugin.class)
						.addAnalyticsConnectorPlugin(LoggerAnalyticsConnectorPlugin.class)
						.build())
				.addModule(ModuleConfig.builder("vertigo-core-aspect")
						.addAspect(AnalyticsAspect.class)
						.build())
				.addModule(ModuleConfig.builder("vertigo-test")
						.addComponent(TestAnalyticsAspectServices.class)
						.build())
				.build();

	}

	@Override
	protected void doTearDown() throws Exception {
		Thread.sleep(1050); //wait daemon run once
	}

	/**
	 * Test simple avec deux compteurs.
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 */
	@Test
	public void test1000Articles() {
		analyticsManager.trace(
				TEST_CATEGORY,
				"/create/articles",
				tracer -> {
					for (int i = 0; i < 1000; i++) {
						tracer.incMeasure(WEIGHT, 25)
								.incMeasure(PRICE, 10);
					}
				});
		try {
			Thread.sleep(1);
		} catch (final InterruptedException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		analyticsManager.getCurrentTracer().ifPresent(
				tracer -> tracer.incMeasure(WEIGHT, 25));
		//Dans le cas du dummy ça doit passer
	}

	@Test
	public void testAspect() {
		TestAProcessConnectorPlugin.reset();
		final int result = analyticsAspectServices.add(1, 2);
		Assertions.assertEquals(3, result);
		//---
		Assertions.assertEquals(1, TestAProcessConnectorPlugin.getCount());
		Assertions.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());
	}

	@Test
	public void testConnectors() {
		TestAProcessConnectorPlugin.reset();
		for (int i = 0; i < 50; i++) {
			final int result = analyticsAspectServices.add(i, 2 * i);
			Assertions.assertEquals(3 * i, result);
		}
		for (int i = 0; i < 50; i++) {
			analyticsAspectServices.checkPositive(i);
		}
		Assertions.assertEquals(100, TestAProcessConnectorPlugin.getCount());
		Assertions.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());

	}

	@Test
	public void testFail() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			TestAProcessConnectorPlugin.reset();
			try {
				analyticsAspectServices.checkPositive(-1);
			} catch (final IllegalStateException e) {
				Assertions.assertEquals(1, TestAProcessConnectorPlugin.getCount());
				Assertions.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());
				throw e;
			}
		});

	}

	@Test
	public void testSetMeasures() {
		TestAProcessConnectorPlugin.reset();
		Assertions.assertEquals(null, TestAProcessConnectorPlugin.getLastPrice());
		analyticsAspectServices.setMeasure();
		Assertions.assertEquals(100D, TestAProcessConnectorPlugin.getLastPrice().doubleValue());
	}

	@Test
	public void testSetAndIncMeasures() {
		TestAProcessConnectorPlugin.reset();
		analyticsAspectServices.setAndIncMeasure();
		Assertions.assertEquals(120D, TestAProcessConnectorPlugin.getLastPrice().doubleValue());
	}

	@Test
	public void testIncMeasures() {
		TestAProcessConnectorPlugin.reset();
		analyticsAspectServices.incMeasure();
		Assertions.assertEquals(10D, TestAProcessConnectorPlugin.getLastPrice().doubleValue());
	}

	/**
	 * Même test après désactivation.
	 */
	@Test
	public void testOff() {
		test1000Articles();
	}

	/**
	 * Test de récursivité.
	 * Test sur l'envoi de 1000 commandes contenant chacune 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 * Les frais d'envoi sont de 5€.
	 */
	@Test
	public void test1000Commandes() {
		final long start = System.currentTimeMillis();
		analyticsManager.trace(
				TEST_CATEGORY,
				"/create/orders",
				tracer -> {
					for (int i = 0; i < 1000; i++) {
						tracer.incMeasure(PRICE, 5);
						test1000Articles();
					}
				});
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
	}
}
