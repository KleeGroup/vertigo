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
package io.vertigo.commons.plugins.analytics.log;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import io.vertigo.commons.analytics.health.HealthCheck;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.analytics.process.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;

/**
 * This connector analyses the process and calculates duration and count.
 * @author mlaroche,pchretien
 */
public final class SmartLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {

	private final Optional<String> aggregatedByOpt;
	private final Long durationThreshold;

	/**
	 * Constructor.
	 * @param aggregatedByOpt optional param for aggrating subprocesses results with specific category
	 * @param durationThresholdOpt optional param for setting the error level to log in error
	 */
	@Inject
	public SmartLoggerAnalyticsConnectorPlugin(
			@ParamValue("aggregatedBy") final Optional<String> aggregatedByOpt,
			@ParamValue("durationThreshold") final Optional<Long> durationThresholdOpt) {
		Assertion.checkNotNull(aggregatedByOpt);
		Assertion.checkNotNull(durationThresholdOpt);
		//---
		this.aggregatedByOpt = aggregatedByOpt;
		// see Jakob Nielsen dhttps://www.nngroup.com/articles/response-times-3-important-limits/
		durationThreshold = durationThresholdOpt.orElse(1000L);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", process.getName());
		jsonObject.addProperty("durationMillis", process.getDurationMillis());

		if (aggregatedByOpt.isPresent()) {
			final AggregatedResult result = new AggregatedResult();
			buildCountAndFullDurationByCategory(process, aggregatedByOpt.get(), result);
			if (result.count != 0) {
				final JsonObject aggregatedObject = new JsonObject();
				aggregatedObject.addProperty("count", result.count);
				aggregatedObject.addProperty("durationMillis", result.duration);
				//--
				jsonObject.add(aggregatedByOpt.get(), aggregatedObject);
			}
		}

		final Logger logger = LogManager.getLogger(process.getCategory());
		if (process.getDurationMillis() > durationThreshold) {
			logger.error(jsonObject.toString());
		} else if (logger.isInfoEnabled()) {
			logger.info(jsonObject.toString());
		}
	}

	private static void buildCountAndFullDurationByCategory(final AProcess process, final String category, final AggregatedResult aggregatedResult) {
		process.getSubProcesses()
				.stream()
				.filter(subprocess -> category.equals(subprocess.getCategory()))
				.forEach(subprocess -> {
					aggregatedResult.count++;
					aggregatedResult.duration += subprocess.getDurationMillis();
				});

		//---
		process.getSubProcesses()
				.stream()
				.filter(subprocess -> !category.equals(subprocess.getCategory()))
				.forEach(subprocess -> buildCountAndFullDurationByCategory(subprocess, category, aggregatedResult));

	}

	static class AggregatedResult {
		int count;
		long duration;
	}

	@Override
	public void add(final Metric metric) {
		//nothing

	}

	@Override
	public void add(final HealthCheck healthCheck) {
		//nothing

	}
}
