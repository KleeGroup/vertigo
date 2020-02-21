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
package io.vertigo.vega.webservice;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.app.Node;
import io.vertigo.util.InjectorUtil;
import io.vertigo.vega.webservice.data.MyNodeConfig;

public final class DistantAppTest {

	private static AutoCloseableApp app;

	@Inject
	private AppManager nodeManager;

	@BeforeAll
	public static void setUp() {
		app = new AutoCloseableApp(MyNodeConfig.config(true));
	}

	@BeforeEach
	public void doBefore() {
		InjectorUtil.injectMembers(this);
	}

	@AfterAll
	public static void tearDown() {
		if (app != null) {
			app.close();
		}
	}

	@Test
	public void testDistantNodeConfig() {

		final String currentNodeId = nodeManager.getCurrentNode().getId();
		final Optional<Node> appNode = nodeManager.find(currentNodeId);
		Assertions.assertTrue(appNode.isPresent());

		Assertions.assertTrue(nodeManager.getConfig().containsKey(currentNodeId));
	}

	@Test
	public void testDistantAppHealth() {

		final String currentNodeId = nodeManager.getCurrentNode().getId();
		final Optional<Node> appNode = nodeManager.find(currentNodeId);
		Assertions.assertTrue(appNode.isPresent());

		Assertions.assertTrue(nodeManager.getStatus().containsKey(currentNodeId));
	}

}
