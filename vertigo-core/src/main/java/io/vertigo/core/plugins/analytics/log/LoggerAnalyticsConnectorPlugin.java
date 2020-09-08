/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.plugins.analytics.log;

import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;

/**
 * Processes connector which only use a log4j logger.
 * @author mlaroche,pchretien,npiedeloup
 */
public final class LoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {

	private static final Logger LOGGER_HEALTH = LogManager.getLogger("health");
	private static final Logger LOGGER_METRIC = LogManager.getLogger("metric");

	private static final Gson GSON = new GsonBuilder().create();

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		final Logger logger = LogManager.getLogger(process.getCategory());
		if (logger.isInfoEnabled()) {
			final String json = GSON.toJson(Collections.singletonList(process));
			logger.info(json);
		}
	}

	@Override
	public void add(final Metric metric) {
		if (LOGGER_METRIC.isInfoEnabled()) {
			final String json = GSON.toJson(Collections.singletonList(metric));
			LOGGER_METRIC.info(json);
		}

	}

	@Override
	public void add(final HealthCheck healthCheck) {
		if (LOGGER_HEALTH.isInfoEnabled()) {
			final String json = GSON.toJson(Collections.singletonList(healthCheck));
			LOGGER_HEALTH.info(json);
		}

	}

}
