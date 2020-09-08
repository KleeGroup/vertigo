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
/**
 *
 */
package io.vertigo.core.analytics.health;

import java.time.Instant;

import io.vertigo.core.lang.Assertion;

/**
 * Health check.
 *  example :
 *  dataStorePlugin.ping : Ping to a Database produces a specific health check with the actual measure of it
 *  {plugin/component}.{test} :
 *
 * @author mlaroche
 */
public final class HealthCheck {
	private final String name;
	private final String checker;
	private final String module;
	private final String feature;
	private final HealthMeasure healthMeasure;
	private final Instant checkInstant;

	/**
	 * Constructor.
	 *
	 * @param name the health check name
	 * @param checker who  created the measure
	 * @param module the module (either technical or functional) the healthcheck is relative to (ex: commons, administration...)
	 * @param feature the feature (a semantic one) to link healthchecks that concern the same subject (ex: database, billing...)
	 * @param checkInstant when the check was performed
	 * @param healthMeasure the measure
	 */
	public HealthCheck(
			final String name,
			final String checker,
			final String module,
			final String feature,
			final Instant checkInstant,
			final HealthMeasure healthMeasure) {
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(checker)
				.isNotBlank(module)
				.isNotBlank(feature)
				.isNotNull(checkInstant)
				.isNotNull(healthMeasure);
		//-----
		this.name = name;
		this.checker = checker;
		this.module = module;
		this.feature = feature;
		this.checkInstant = checkInstant;
		this.healthMeasure = healthMeasure;
	}

	/**
	 * @return the health check name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the checker
	 */
	public String getChecker() {
		return checker;
	}

	public String getModule() {
		return module;
	}

	public String getFeature() {
		return feature;
	}

	/**
	 * @return the instant when the check was performed
	 */
	public Instant getCheckInstant() {
		return checkInstant;
	}

	/**
	 * @return the measure
	 */
	public HealthMeasure getMeasure() {
		return healthMeasure;
	}

}
