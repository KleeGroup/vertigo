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
package io.vertigo.core.analytics.health.definitions;

import java.util.function.Supplier;

import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * This defines a health check with
 * 			- a definition name
 * 			- a health check name
 * 			- a checker
 * 			- a way to test that provides a HealthMeasure
 * @author mlaroche, pchretien
 */
@DefinitionPrefix(HealthCheckDefinition.PREFIX)
public final class HealthCheckDefinition extends AbstractDefinition {
	public static final String PREFIX = "Hchk";

	private final String healthCheckName;
	private final String checker;
	private final String module;
	private final String feature;
	private final Supplier<HealthMeasure> checkMethod;

	/**
	 * Constructor
	 * @param name the definition name (must be unique)
	 * @param healthCheckName the name of the health check (must be unique)
	 * @param checker the name of the component that is responsible of providing the measure
	 * @param checkMethod the check method that provides a health measure
	 */
	public HealthCheckDefinition(
			final String name,
			final String healthCheckName,
			final String checker,
			final String module,
			final String feature,
			final Supplier<HealthMeasure> checkMethod) {
		super(name);
		//
		Assertion.check()
				.isNotBlank(healthCheckName)
				.isNotBlank(checker)
				.isNotBlank(module)
				.isNotBlank(feature)
				.isNotNull(checkMethod);
		//-----
		this.healthCheckName = healthCheckName;
		this.checker = checker;
		this.module = module;
		this.feature = feature;
		this.checkMethod = checkMethod;
	}

	/**
	 * @return the health check name;
	 */
	public String getHealthCheckName() {
		return healthCheckName;
	}

	/**
	 * @return the name of the health checker
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
	 * @return the check method that provides a health measure
	 */
	public Supplier<HealthMeasure> getCheckMethod() {
		return checkMethod;
	}

}
