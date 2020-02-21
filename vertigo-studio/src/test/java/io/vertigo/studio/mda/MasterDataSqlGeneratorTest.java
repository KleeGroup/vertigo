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
package io.vertigo.studio.mda;

import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.studio.StudioFeatures;
import io.vertigo.studio.tools.NameSpace2Java;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 */
public class MasterDataSqlGeneratorTest {

	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures().build())
				.addModule(new StudioFeatures()
						.withMasterData()
						.withMda(
								Param.of("projectPackageName", "io.vertigo.studio"),
								Param.of("targetGenDir", "target/"))
						.withSqlDomainGenerator(
								Param.of("targetSubDir", "databasegenMasterdata"),
								Param.of("baseCible", "PostgreSql"),
								Param.of("generateDrop", "false"),
								Param.of("generateMasterData", "true"))
						.withJsonMasterDataValuesProvider(
								Param.of("fileName", "io/vertigo/studio/mda/data/masterdata/testJsonMasterDataValues.json"))
						.withJsonMasterDataValuesProvider(
								Param.of("fileName", "io/vertigo/studio/mda/data/masterdata/testJsonMasterDataValues2.json"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/studio/data/generationWTask.kpr")
								.build())
						.build())
				.build();
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerate() {
		NameSpace2Java.main(buildNodeConfig());
		try (AutoCloseableApp app = new AutoCloseableApp(SqlTestConfigurator.config())) {
			DataBaseScriptUtil.execSqlScript("target/databasegenMasterdata/crebas.sql", app);
			DataBaseScriptUtil.execSqlScript("target/databasegenMasterdata/init_masterdata_command_type.sql", app);
			DataBaseScriptUtil.execSqlScript("target/databasegenMasterdata/init_masterdata_motor_type.sql", app);
		}
	}

}
