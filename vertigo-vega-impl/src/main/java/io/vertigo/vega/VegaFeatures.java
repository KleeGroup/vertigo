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
package io.vertigo.vega;

import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.PluginConfig;
import io.vertigo.app.config.PluginConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;
import io.vertigo.vega.engines.webservice.json.GoogleJsonEngine;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.impl.webservice.WebServiceManagerImpl;
import io.vertigo.vega.impl.webservice.catalog.CatalogWebServices;
import io.vertigo.vega.impl.webservice.catalog.SwaggerWebServices;
import io.vertigo.vega.plugins.webservice.handler.AccessTokenWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.AnalyticsWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.CorsAllowerWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ExceptionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.JsonConverterWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.PaginatorAndSortWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RateLimitingWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RestfulServiceWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SecurityWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ServerSideStateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionInvalidateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ValidatorWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.scanner.annotations.AnnotationsWebServiceScannerPlugin;
import io.vertigo.vega.plugins.webservice.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.plugins.webservice.webserver.sparkjava.SparkJavaServletFilterWebServerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.WebServiceManager;

/**
 * Defines module Vega.
 * @author pchretien
 */
public final class VegaFeatures extends Features<VegaFeatures> {

	private boolean webservicesEnabled;

	private boolean tokensEnabled;
	private String myTokens;
	private Param[] jsonParams = new Param[0];
	private boolean rateLimitingEnabled;
	private boolean securityEnabled;
	private String myApiPrefix;
	private String myPort;
	private String myOriginCORSFilter;

	public VegaFeatures() {
		super("vertigo-vega");
	}

	@Feature("webservices")
	public VegaFeatures withWebServices() {
		webservicesEnabled = true;
		return this;
	}

	@Feature("webservices.token")
	public VegaFeatures withWebServicesTokens(final Param... params) {
		//-----
		Assertion.checkState(params.length == 1 && "tokens".equals(params[0].getName()), "tokens param should be provided ");
		myTokens = params[0].getValue();
		Assertion.checkArgNotEmpty(myTokens);
		tokensEnabled = true;
		return this;
	}

	@Feature("webservices.rateLimiting")
	public VegaFeatures withWebServicesRateLimiting() {
		rateLimitingEnabled = true;
		return this;
	}

	@Feature("webservices.security")
	public VegaFeatures withWebServicesSecurity() {
		securityEnabled = true;
		return this;
	}

	@Feature("webservices.apiPrefix")
	public VegaFeatures withWebServicesApiPrefix(final Param... params) {
		Assertion.checkState(params.length == 1 && "apiPrefix".equals(params[0].getName()), "apiPrefix param should be provided ");
		myApiPrefix = params[0].getValue();
		return this;
	}

	@Feature("webservices.json")
	public VegaFeatures withWebServicesJson(final Param... params) {
		jsonParams = params;
		return this;
	}

	@Feature("webservices.embeddedServer")
	public VegaFeatures withWebServicesEmbeddedServer(final Param... params) {
		Assertion.checkState(params.length == 1 && "port".equals(params[0].getName()), "port param should be provided ");
		myPort = params[0].getValue();
		return this;
	}

	@Feature("webservices.cors")
	public VegaFeatures withWebServicesOriginCORSFilter(final Param... params) {
		Assertion.checkState(params.length == 1 && "originCORSFilter".equals(params[0].getName()), "originCORSFilter param should be provided ");
		myOriginCORSFilter = params[0].getValue();
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		if (webservicesEnabled) {
			final PluginConfigBuilder corsAllowerPluginConfigBuilder = PluginConfig.builder(CorsAllowerWebServiceHandlerPlugin.class);
			if (myOriginCORSFilter != null) {
				corsAllowerPluginConfigBuilder.addParam(Param.of("originCORSFilter", myOriginCORSFilter));
			}

			getModuleConfigBuilder()
					.addComponent(WebServiceManager.class, WebServiceManagerImpl.class)
					.addPlugin(AnnotationsWebServiceScannerPlugin.class)
					.addComponent(SwaggerWebServices.class)
					.addComponent(CatalogWebServices.class)

					//-- Handlers plugins
					.addPlugin(ExceptionWebServiceHandlerPlugin.class)
					.addPlugin(corsAllowerPluginConfigBuilder.build())
					.addPlugin(AnalyticsWebServiceHandlerPlugin.class)
					.addPlugin(JsonConverterWebServiceHandlerPlugin.class);

			getModuleConfigBuilder()
					.addComponent(JsonEngine.class, GoogleJsonEngine.class, jsonParams);

			if (securityEnabled) {
				getModuleConfigBuilder()
						.addPlugin(SessionInvalidateWebServiceHandlerPlugin.class)
						.addPlugin(SessionWebServiceHandlerPlugin.class)
						.addPlugin(SecurityWebServiceHandlerPlugin.class);
			}
			if (tokensEnabled) {
				getModuleConfigBuilder()
						.addPlugin(ServerSideStateWebServiceHandlerPlugin.class)
						.addPlugin(AccessTokenWebServiceHandlerPlugin.class)
						.addPlugin(PaginatorAndSortWebServiceHandlerPlugin.class)
						.addComponent(TokenManager.class, TokenManagerImpl.class,
								Param.of("collection", myTokens));
			}
			if (rateLimitingEnabled) {
				getModuleConfigBuilder()
						.addPlugin(RateLimitingWebServiceHandlerPlugin.class);
			}
			if (myPort != null) {
				final ListBuilder<Param> params = new ListBuilder()
						.add(Param.of("port", myPort));
				if (myApiPrefix != null) {
					params.add(Param.of("apiPrefix", myApiPrefix));
				}
				getModuleConfigBuilder().addPlugin(new PluginConfig(SparkJavaEmbeddedWebServerPlugin.class, params.build()));
			} else {
				final ListBuilder<Param> params = new ListBuilder<>();
				if (myApiPrefix != null) {
					params.add(Param.of("apiPrefix", myApiPrefix));
				}
				getModuleConfigBuilder().addPlugin(new PluginConfig(SparkJavaServletFilterWebServerPlugin.class, params.build()));
			}

			getModuleConfigBuilder().addPlugin(ValidatorWebServiceHandlerPlugin.class)
					.addPlugin(RestfulServiceWebServiceHandlerPlugin.class);
		}
	}
}
