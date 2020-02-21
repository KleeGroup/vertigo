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
package io.vertigo.dynamo.environment.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

/**
 * Test de lecture de class Java.
 *
 * @author npiedeloup
 */
public final class JavaParserStereotypesTest2 extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/java/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.environment.java.data.domain.*")
								.build())
						.build())
				.build();
	}

	private DtDefinition getDtDefinition(final String urn) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(urn, DtDefinition.class);
	}

	/**
	 * Test du stereotype MasterData
	 */
	@Test
	public void testStereotypeMasterData() {
		final DtDefinition dtDefinitionCity = getDtDefinition("DtCity");
		Assertions.assertNotNull(dtDefinitionCity);
		Assertions.assertEquals(DtStereotype.MasterData, dtDefinitionCity.getStereotype());

		final DtDefinition dtDefinitionCommandType = getDtDefinition("DtCommandType");
		Assertions.assertNotNull(dtDefinitionCommandType);
		Assertions.assertEquals(DtStereotype.StaticMasterData, dtDefinitionCommandType.getStereotype());
	}

	/**
	 * Test du stereotype keyConcept
	 */
	@Test
	public void testStereotypeKeyConcept() {
		final DtDefinition dtDefinitionCommand = getDtDefinition("DtCommand");
		Assertions.assertNotNull(dtDefinitionCommand);
		Assertions.assertEquals(DtStereotype.KeyConcept, dtDefinitionCommand.getStereotype());

	}

	/**
	 * Test du stereotype Data
	 */
	@Test
	public void testStereotypeEntity() {
		final DtDefinition dtDefinitionAttachment = getDtDefinition("DtAttachment");
		Assertions.assertNotNull(dtDefinitionAttachment);
		Assertions.assertEquals(DtStereotype.Entity, dtDefinitionAttachment.getStereotype());

		final DtDefinition dtDefinitionCommandValidation = getDtDefinition("DtCommandValidation");
		Assertions.assertNotNull(dtDefinitionCommandValidation);
		Assertions.assertEquals(DtStereotype.Entity, dtDefinitionCommandValidation.getStereotype());
	}

	@Test
	public void testStereotypeData() {
		final DtDefinition dtDefinitionAttachment = getDtDefinition("DtCommandCriteria");
		Assertions.assertNotNull(dtDefinitionAttachment);
		Assertions.assertEquals(DtStereotype.ValueObject, dtDefinitionAttachment.getStereotype());

	}
}
