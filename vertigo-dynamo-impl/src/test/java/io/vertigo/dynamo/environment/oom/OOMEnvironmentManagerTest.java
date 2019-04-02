/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.environment.oom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.domain.metamodel.ConstraintDefinition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class OOMEnvironmentManagerTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/oom/data/domain.kpr")
								.addDefinitionResource("oom", "io/vertigo/dynamo/environment/oom/data/demo.oom")
								.build())
						.build())
				.build();
	}

	@Test
	public void testConstraint() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final ConstraintDefinition constraint = definitionSpace.resolve("CkTelephone", ConstraintDefinition.class);
		Assertions.assertEquals(DtProperty.REGEX, constraint.getProperty());
	}

	@Test
	public void testDefaultFormatter() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final FormatterDefinition formatter = definitionSpace.resolve("FmtDefault", FormatterDefinition.class);
		Assertions.assertEquals(FormatterDefault.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testDefaultBooleanFormatter() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Formatter formatter = definitionSpace.resolve("FmtDefault", FormatterDefinition.class);
		Assertions.assertEquals("MyTrue", formatter.valueToString(true, DataType.Boolean));
	}

	@Test
	public void testFormatter() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final FormatterDefinition formatter = definitionSpace.resolve("FmtTaux", FormatterDefinition.class);
		Assertions.assertEquals(FormatterNumber.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testDomain() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final io.vertigo.dynamo.domain.metamodel.Domain domain = definitionSpace.resolve("DoEmail", Domain.class);
		Assertions.assertEquals(DataType.String, domain.getDataType());
		Assertions.assertEquals(FormatterDefault.class.getName(), domain.getFormatterClassName());
	}

	@Test
	public void testDtDefinition() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final DtDefinition dtDefinition = definitionSpace.resolve("DtFamille", DtDefinition.class);
		Assertions.assertEquals("io.vertigo.dynamock.domain.famille.Famille", dtDefinition.getClassCanonicalName());
		Assertions.assertTrue(dtDefinition.isPersistent());
		Assertions.assertEquals("io.vertigo.dynamock.domain.famille", dtDefinition.getPackageName());
	}
}
