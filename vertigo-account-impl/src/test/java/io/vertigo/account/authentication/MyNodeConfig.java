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
package io.vertigo.account.authentication;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.authentication.model.DtDefinitions;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;

public final class MyNodeConfig {
	private static final String REDIS_HOST = "redis-pic.part.klee.lan.net";
	private static final int REDIS_PORT = 6379;
	private static final int REDIS_DATABASE = 15;

	enum AuthentPlugin {
		ldap, text, store, mock
	}

	public static NodeConfig config(final AuthentPlugin authentPlugin, final boolean redis) {
		final CommonsFeatures commonsFeatures = new CommonsFeatures()
				.withScript()
				.withJaninoScript()
				.withCache()
				.withMemoryCache();

		final DatabaseFeatures databaseFeatures = new DatabaseFeatures();
		final DynamoFeatures dynamoFeatures = new DynamoFeatures();
		final AccountFeatures accountFeatures = new AccountFeatures()
				.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
				.withAccount()
				.withAuthentication();

		if (redis) {
			commonsFeatures
					.withRedisConnector(Param.of("host", REDIS_HOST), Param.of("port", Integer.toString(REDIS_PORT)), Param.of("database", Integer.toString(REDIS_DATABASE)));
			accountFeatures
					.withRedisAccountCache();
		}
		accountFeatures
				.withTextAccount(
						Param.of("accountFilePath", "io/vertigo/account/data/identities.txt"),
						Param.of("accountFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<email>(?<authToken>[^;@]+)@[^;]+);(?<photoUrl>.*)$"),
						Param.of("groupFilePath", "io/vertigo/account/data/groups.txt"),
						Param.of("groupFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<accountIds>.*)$"));

		if (authentPlugin == AuthentPlugin.ldap) {
			accountFeatures.withLdapAuthentication(
					Param.of("userLoginTemplate", "cn={0},dc=vertigo,dc=io"),
					Param.of("ldapServerHost", "docker-vertigo.part.klee.lan.net"),
					Param.of("ldapServerPort", "389"));
		} else if (authentPlugin == AuthentPlugin.text) {
			accountFeatures.withTextAuthentication(Param.of("filePath", "io/vertigo/account/data/userAccounts.txt"));
		} else if (authentPlugin == AuthentPlugin.store) {
			databaseFeatures
					.withSqlDataBase()
					.withC3p0(
							Param.of("dataBaseClass", H2DataBase.class.getName()),
							Param.of("jdbcDriver", "org.h2.Driver"),
							Param.of("jdbcUrl", "jdbc:h2:mem:database"));
			dynamoFeatures
					.withStore()
					.addPlugin(SqlDataStorePlugin.class);
			accountFeatures.withStoreAuthentication(
					Param.of("userCredentialEntity", "DtUserCredential"),
					Param.of("userLoginField", "login"),
					Param.of("userPasswordField", "password"),
					Param.of("userTokenIdField", "login"));
		} else if (authentPlugin == AuthentPlugin.mock) {
			accountFeatures.withMockAuthentication();
		}

		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(commonsFeatures.build())
				.addModule(databaseFeatures.build())
				.addModule(dynamoFeatures.build())
				.addModule(accountFeatures.build())
				.addModule(ModuleConfig.builder("app")
						.addDefinitionProvider(
								DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
										.addDefinitionResource("classes", DtDefinitions.class.getName())
										.addDefinitionResource("kpr", "account/domains.kpr")
										.build())
						.build())
				.build();
	}
}
