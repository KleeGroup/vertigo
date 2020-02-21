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
package io.vertigo.core.spaces.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.core.param.Param;

public final class DefinitionSpaceTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("test")
						.addDefinitionProvider(DefinitionProviderConfig.builder(TestDefinitionprovider.class)
								.addParam(Param.of("testParam", "testParamValue"))
								.addDefinitionResource("type1", "resource1")
								.build())
						.build())
				.build();
	}

	@Test
	public void testRegister() throws IOException, ClassNotFoundException {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		assertEquals(1L, definitionSpace.getAllTypes().size(), "definitionSpace must contain one element ");
		assertEquals(1L, definitionSpace.getAll(SampleDefinition.class).size(), "definitionSpace[SampleDefinition.class] must contain one element ");

		final SampleDefinition sampleDefinition = definitionSpace.resolve("SampleTheDefinition", SampleDefinition.class);
		assertNotNull(sampleDefinition);
		assertEquals("TheDefinition", DefinitionUtil.getLocalName(sampleDefinition.getName(), SampleDefinition.class), "localName must be TheDefinition");
		assertEquals(sampleDefinition.getName(), DefinitionUtil.getPrefix(SampleDefinition.class) + DefinitionUtil.getLocalName(sampleDefinition.getName(), SampleDefinition.class),
				"globalName must be SampleTheDefinition");

		final DefinitionReference<SampleDefinition> sampleDefinitionRef = new DefinitionReference<>(sampleDefinition);

		byte[] serialized;
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(); final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(sampleDefinitionRef);
			oos.flush();
			serialized = bos.toByteArray();
		}

		//---
		DefinitionReference definitionReference;
		try (final ByteArrayInputStream bis = new ByteArrayInputStream(serialized); final ObjectInputStream ios = new ObjectInputStream(bis)) {
			definitionReference = DefinitionReference.class.cast(ios.readObject());
		}

		assertNotSame(sampleDefinitionRef, definitionReference, "DefinitionReferences must be not strictly equals");
		assertSame(sampleDefinition, definitionReference.get(), "Definitions must be strictly equals");
	}

	@DefinitionPrefix("Sample")
	public static class SampleDefinition implements Definition {

		@Override
		public String getName() {
			return "SampleTheDefinition";
		}
	}
}
