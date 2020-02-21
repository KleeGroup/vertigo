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
package io.vertigo.account.authorization;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.authorization.model.FullSecuredServices;
import io.vertigo.account.authorization.model.PartialSecuredServices;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.plugins.authorization.loaders.JsonSecurityDefinitionProvider;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

public final class MyNodeConfig {

	public static NodeConfig config() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new AccountFeatures()
						.withSecurity(
								Param.of("userSessionClassName", TestUserSession.class.getName()))
						.withAuthorization()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addAllParams(Param.of("encoding", "utf-8"))
								.addDefinitionResource("kpr", "security/generation.kpr")
								.build())
						.addDefinitionProvider(DefinitionProviderConfig.builder(JsonSecurityDefinitionProvider.class)
								.addDefinitionResource("security", "io/vertigo/account/authorization/advanced-auth-config-v2.json")
								.build())
						.addDefinitionProvider(TestSecurityDefinitionProvider.class)
						.addComponent(FullSecuredServices.class)
						.addComponent(PartialSecuredServices.class)
						.build())

				.build();
	}
}
