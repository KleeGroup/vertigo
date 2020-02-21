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
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class JavaEnvironmentManagerTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/java/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.environment.java.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	@Test
	public void testDefaultFormatter() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final FormatterDefinition formatter = definitionSpace.resolve("FmtDefault", FormatterDefinition.class);
		Assertions.assertEquals(FormatterDefault.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testDomain() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final io.vertigo.dynamo.domain.metamodel.Domain domain = definitionSpace.resolve("DoId", Domain.class);
		Assertions.assertEquals(DataType.Long, domain.getDataType());
		Assertions.assertEquals(FormatterDefault.class.getName(), domain.getFormatterClassName());
	}

	@Test
	public void testCommand() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final DtDefinition dtDefinition = definitionSpace.resolve("DtCommand", DtDefinition.class);
		Assertions.assertTrue(dtDefinition.isPersistent());
		Assertions.assertEquals("io.vertigo.dynamo.environment.java.data.domain.Command", dtDefinition.getClassCanonicalName());
		Assertions.assertEquals("io.vertigo.dynamo.environment.java.data.domain", dtDefinition.getPackageName());
		Assertions.assertEquals("Command", dtDefinition.getClassSimpleName());
	}

	@Test
	public void testCityFragment() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final DtDefinition dtDefinition = definitionSpace.resolve("DtCityFragment", DtDefinition.class);
		Assertions.assertFalse(dtDefinition.isPersistent());
		Assertions.assertTrue(dtDefinition.getFragment().isPresent());
		Assertions.assertTrue("City".equals(dtDefinition.getFragment().get().getClassSimpleName()));
		Assertions.assertEquals("io.vertigo.dynamo.environment.java.data.domain.CityFragment", dtDefinition.getClassCanonicalName());
		Assertions.assertEquals("io.vertigo.dynamo.environment.java.data.domain", dtDefinition.getPackageName());
		Assertions.assertEquals("CityFragment", dtDefinition.getClassSimpleName());
		Assertions.assertTrue("City".equals(dtDefinition.getField("citId").getFkDtDefinition().getClassSimpleName()));
	}

	//	@Test
	//	public void testCreateFamille() {
	//		final Famille famille = new Famille();
	//		famille.setFamId(45L);
	//		famille.setLibelle("Armes");
	//
	//		Assertions.assertEquals(45L, famille.getFamId().longValue());
	//		Assertions.assertEquals("Armes", famille.getLibelle());
	//		Assertions.assertEquals("Armes[45]", famille.getDescription());
	//
	//		//--Vérification des appels dynamiques--
	//		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
	//
	//		final DtField libelleDtField = dtFamille.getField("LIBELLE");
	//		Assertions.assertEquals("Armes", libelleDtField.getDataAccessor().getValue(famille));
	//		//-cas du id
	//		final DtField idDtField = dtFamille.getField("FAM_ID");
	//		Assertions.assertEquals(45L, idDtField.getDataAccessor().getValue(famille));
	//		//-cas du computed
	//		final DtField descriptionDtField = dtFamille.getField("DESCRIPTION");
	//		Assertions.assertEquals("Armes[45]", descriptionDtField.getDataAccessor().getValue(famille));
	//	}
}
