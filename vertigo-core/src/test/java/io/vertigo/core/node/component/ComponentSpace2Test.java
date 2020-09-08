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
package io.vertigo.core.node.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.component.data.BioManagerImpl;
import io.vertigo.core.node.component.data.MathManager;
import io.vertigo.core.node.component.data.MathManagerImpl;
import io.vertigo.core.node.component.data.SimpleMathPlugin;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;

public final class ComponentSpace2Test extends AbstractTestCaseJU5 {
	@Inject
	private BioManager bioManager;

	@Test
	public void testCalcul() {
		final int res = bioManager.add(1, 2, 3);
		assertEquals(366, res);
	}

	//On vérifie que les composants ont bien été démarrés
	@Test
	public void testActive() {
		assertTrue(bioManager.isActive());
	}

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();
	}
}
