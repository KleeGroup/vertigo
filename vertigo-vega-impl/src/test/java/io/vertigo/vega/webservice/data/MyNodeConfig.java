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
package io.vertigo.vega.webservice.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.plugins.authorization.loaders.JsonSecurityDefinitionProvider;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.plugins.app.infos.http.HttpAppNodeInfosPlugin;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.kvstore.delayedmemory.DelayedMemoryKVStorePlugin;
import io.vertigo.vega.VegaFeatures;
import io.vertigo.vega.engines.webservice.cmd.ComponentCmdWebServices;
import io.vertigo.vega.webservice.data.domain.Address;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactView;
import io.vertigo.vega.webservice.data.user.TestUserSession;
import io.vertigo.vega.webservice.data.ws.AdvancedTestWebServices;
import io.vertigo.vega.webservice.data.ws.AnonymousTestWebServices;
import io.vertigo.vega.webservice.data.ws.CommonWebServices;
import io.vertigo.vega.webservice.data.ws.ContactsSecuredWebServices;
import io.vertigo.vega.webservice.data.ws.ContactsWebServices;
import io.vertigo.vega.webservice.data.ws.FileDownloadWebServices;
import io.vertigo.vega.webservice.data.ws.LoginSecuredWebServices;
import io.vertigo.vega.webservice.data.ws.SearchTestWebServices;
import io.vertigo.vega.webservice.data.ws.SimplerTestWebServices;
import io.vertigo.vega.webservice.data.ws.ValidationsTestWebServices;

public final class MyNodeConfig {
	public static final int WS_PORT = 8088;

	public static final class DtDefinitions implements Iterable<Class<?>> {
		@Override
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] {
					Contact.class, ContactCriteria.class,
					Address.class, ContactView.class
			}).iterator();
		}
	}

	public static NodeConfig config(final boolean isEmbedded) {

		final VegaFeatures vegaFeatures = new VegaFeatures()
				.withWebServices()
				.withWebServicesTokens(Param.of("tokens", "tokens"))
				.withWebServicesSecurity()
				.withWebServicesRateLimiting();
		if (isEmbedded) {
			vegaFeatures.withWebServicesEmbeddedServer(Param.of("port", Integer.toString(WS_PORT)));
		}

		return NodeConfig.builder()
				.withEndPoint("http://localhost:" + WS_PORT)
				.beginBoot()
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.addPlugin(MemoryCachePlugin.class)
						.withNodeInfosPlugin(HttpAppNodeInfosPlugin.class)
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withKVStore()
						.addPlugin(DelayedMemoryKVStorePlugin.class,
								Param.of("collections", "tokens"),
								Param.of("timeToLiveSeconds", "120"))
						.build())
				.addModule(new AccountFeatures()
						.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
						.withAuthorization()
						.build())
				.addModule(vegaFeatures.build())
				//-----
				.addModule(ModuleConfig.builder("dao-app")
						.addComponent(ContactDao.class)
						.build())
				.addModule(ModuleConfig.builder("webservices-app")
						.addComponent(ComponentCmdWebServices.class)
						.addComponent(CommonWebServices.class)
						.addComponent(ContactsWebServices.class)
						.addComponent(ContactsSecuredWebServices.class)
						.addComponent(LoginSecuredWebServices.class)
						.addComponent(SimplerTestWebServices.class)
						.addComponent(ValidationsTestWebServices.class)
						.addComponent(AdvancedTestWebServices.class)
						.addComponent(AnonymousTestWebServices.class)
						.addComponent(FileDownloadWebServices.class)
						.addComponent(SearchTestWebServices.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("classes", DtDefinitions.class.getName())
								.addDefinitionResource("kpr", "io/vertigo/vega/webservice/data/execution.kpr")
								.build())
						.addDefinitionProvider(DefinitionProviderConfig.builder(JsonSecurityDefinitionProvider.class)
								.addDefinitionResource("security", "io/vertigo/vega/webservice/data/ws-auth-config.json")
								.build())
						.build())
				.build();
	}
}
