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
package io.vertigo.commons.node;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.Home;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.app.Node;
import io.vertigo.app.config.ModuleConfig;

public abstract class AbstractNodeManagerTest extends AbstractTestCaseJU5 {

	protected NodeConfigBuilder buildRootNodeConfig() {
		return NodeConfig.builder()
				.withAppName("nodeTestApp")
				.withNodeId("nodeTest1")
				.addModule(ModuleConfig.builder("db")
						.build());
	}

	@Test
	void testRegisterNode() {
		final AppManager nodeManager = Home.getApp().getComponentSpace().resolve(AppManager.class);

		final List<Node> nodesWithDbSkill = nodeManager.locateSkills("db");
		final List<Node> nodesWithOtherSkill = nodeManager.locateSkills("other");

		// ---
		Assertions.assertEquals(1, nodesWithDbSkill.size());
		Assertions.assertEquals(0, nodesWithOtherSkill.size());

	}

	@Disabled // ignored for now we need heartbeat of node update to be parametized for shorter tests
	void testUpdate() throws InterruptedException {
		final AppManager nodeManager = Home.getApp().getComponentSpace().resolve(AppManager.class);
		// ---
		final Instant firstTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		Thread.sleep(7 * 1000L);
		final Instant secondTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		// ---
		Assertions.assertTrue(secondTouch.isAfter(firstTouch));

	}

}
