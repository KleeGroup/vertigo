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
package io.vertigo.core.spaces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.spaces.component.data.BioManager;
import io.vertigo.core.spaces.component.data.BioManagerImpl;
import io.vertigo.core.spaces.component.data.MathManager;
import io.vertigo.core.spaces.component.data.MathManagerImpl;
import io.vertigo.core.spaces.component.data.MathPlugin;

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
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();
	}
}
