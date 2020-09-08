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
package io.vertigo.core.node.component.aop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.aop.data.aspects.OneMoreAspect;
import io.vertigo.core.node.component.aop.data.components.ComputerImpl;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class Aspect2Test {

	protected static NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("aspects")
						.addAspect(OneMoreAspect.class)
						.build())
				.addModule(ModuleConfig.builder("components")
						.addComponent(ComputerImpl.class)
						.build())
				.build();
	}

	@Test
	public final void testLoadComponentsWithoutDeclaredAspects() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> {
					try (final AutoCloseableNode node = new AutoCloseableNode(buildNodeConfig())) {
						//nop
					}
				});
	}

}
