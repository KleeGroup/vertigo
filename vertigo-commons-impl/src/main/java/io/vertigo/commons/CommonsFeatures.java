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
package io.vertigo.commons;

import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.command.CommandManager;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.commons.impl.analytics.AnalyticsManagerImpl;
import io.vertigo.commons.impl.app.AppManagerImpl;
import io.vertigo.commons.impl.app.AppNodeInfosPlugin;
import io.vertigo.commons.impl.app.AppNodeRegistryPlugin;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.command.CommandManagerImpl;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.commons.impl.daemon.DaemonManagerImpl;
import io.vertigo.commons.impl.eventbus.EventBusManagerImpl;
import io.vertigo.commons.impl.script.ScriptManagerImpl;
import io.vertigo.commons.impl.transaction.VTransactionAspect;
import io.vertigo.commons.impl.transaction.VTransactionManagerImpl;
import io.vertigo.commons.plugins.analytics.log.SmartLoggerAnalyticsConnectorPlugin;
import io.vertigo.commons.plugins.analytics.log.SocketLoggerAnalyticsConnectorPlugin;
import io.vertigo.commons.plugins.app.infos.http.HttpAppNodeInfosPlugin;
import io.vertigo.commons.plugins.app.registry.db.DbAppNodeRegistryPlugin;
import io.vertigo.commons.plugins.app.registry.redis.RedisAppNodeRegistryPlugin;
import io.vertigo.commons.plugins.cache.ehcache.EhCachePlugin;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.commons.plugins.cache.redis.RedisCachePlugin;
import io.vertigo.commons.plugins.script.janino.JaninoExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.param.Param;

/**
 * Defines commons module.
 * @author pchretien
 */
public final class CommonsFeatures extends Features<CommonsFeatures> {

	/**
	 * Constructor.
	 */
	public CommonsFeatures() {
		super("vertigo-commons");
	}

	/**
	 * Activates script with a default plugin.
	 *
	 * @return these features
	 */
	@Feature("script")
	public CommonsFeatures withScript() {
		getModuleConfigBuilder()
				.addComponent(ScriptManager.class, ScriptManagerImpl.class);
		return this;
	}

	/**
	 * Activates script with a default plugin.
	 *
	 * @return these features
	 */
	@Feature("script.janino")
	public CommonsFeatures withJaninoScript() {
		getModuleConfigBuilder()
				.addPlugin(JaninoExpressionEvaluatorPlugin.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache")
	public CommonsFeatures withCache() {
		getModuleConfigBuilder()
				.addComponent(CacheManager.class, CacheManagerImpl.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.redis")
	public CommonsFeatures withRedisCache() {
		getModuleConfigBuilder()
				.addPlugin(RedisCachePlugin.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.memory")
	public CommonsFeatures withMemoryCache() {
		getModuleConfigBuilder()
				.addPlugin(MemoryCachePlugin.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.eh")
	public CommonsFeatures withEhCache() {
		getModuleConfigBuilder()
				.addPlugin(EhCachePlugin.class);
		return this;
	}

	/**
	 * Adds a REDIS connector.
	 * @param host the REDIS host
	 * @param port the REDIS port
	 * @param passwordOpt the REDIS password
	 * @param database the index of the REDIS database
	 * @return these features
	 */
	@Feature("redis")
	public CommonsFeatures withRedisConnector(final Param... params) {
		getModuleConfigBuilder()
				.addComponent(RedisConnector.class, params);
		return this;

	}

	@Feature("analytics.socketLoggerConnector")
	public CommonsFeatures withSocketLoggerAnalyticsConnector(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SocketLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	@Feature("analytics.smartLoggerConnector")
	public CommonsFeatures withSmartLoggerAnalyticsConnector(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SmartLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	@Feature("app.dbRegistry")
	public CommonsFeatures withDbAppNodeRegistryPlugin(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DbAppNodeRegistryPlugin.class, params);
		return this;

	}

	@Feature("app.redisRegistry")
	public CommonsFeatures withRedisAppNodeRegistryPlugin() {
		getModuleConfigBuilder()
				.addPlugin(RedisAppNodeRegistryPlugin.class);
		return this;

	}

	@Feature("app.httpInfos")
	public CommonsFeatures withHttpAppNodeInfosPlugin() {
		getModuleConfigBuilder()
				.addPlugin(HttpAppNodeInfosPlugin.class);
		return this;

	}

	@Feature("command")
	public CommonsFeatures withCommand() {
		getModuleConfigBuilder()
				.addComponent(CommandManager.class, CommandManagerImpl.class);
		return this;

	}

	/**
	 * Adds a NodeRegistryPlugin
	 * @param nodeRegistryPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeRegistryPlugin(final Class<? extends AppNodeRegistryPlugin> nodeRegistryPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeRegistryPluginClass, params);
		return this;

	}

	/**
	 * Adds a NodeInfosPlugin
	 * @param nodeInfosPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeInfosPlugin(final Class<? extends AppNodeInfosPlugin> nodeInfosPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeInfosPluginClass, params);
		return this;

	}

	/**
	 * Adds a AnalyticsConnectorPlugin
	 * @param analyticsConnectorPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures addAnalyticsConnectorPlugin(final Class<? extends AnalyticsConnectorPlugin> analyticsConnectorPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(analyticsConnectorPluginClass, params);
		return this;

	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class)
				.addComponent(CodecManager.class, CodecManagerImpl.class)
				.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.addComponent(EventBusManager.class, EventBusManagerImpl.class)
				.addComponent(AppManager.class, AppManagerImpl.class)
				.addComponent(VTransactionManager.class, VTransactionManagerImpl.class)
				.addAspect(VTransactionAspect.class);
	}
}
