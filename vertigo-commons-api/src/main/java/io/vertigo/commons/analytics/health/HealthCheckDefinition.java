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
package io.vertigo.commons.analytics.health;

import java.util.function.Supplier;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * This definition defines a health check
 * 			- a definition name
 * 			- a health check name
 * 			- a checker
 * 			- a way to test that provides a HealthMeasure
 * @author mlaroche, pchretien
 */
@DefinitionPrefix("Hchk")
public final class HealthCheckDefinition implements Definition {

	private final String definitionName;
	private final String healthCheckName;
	private final String checker;
	private final String module;
	private final String feature;
	private final Supplier<HealthMeasure> checkMethod;

	/**
	 * Constructor
	 * @param definitionName the definition name (must be unique)
	 * @param healthCheckName the name of the health check (must be unique)
	 * @param checker the name of the component that is responsible of providing the measure
	 * @param checkMethod the check method that provides a health measure
	 */
	public HealthCheckDefinition(
			final String definitionName,
			final String healthCheckName,
			final String checker,
			final String module,
			final String feature,
			final Supplier<HealthMeasure> checkMethod) {
		Assertion.checkArgNotEmpty(definitionName);
		Assertion.checkArgNotEmpty(healthCheckName);
		Assertion.checkArgNotEmpty(checker);
		Assertion.checkArgNotEmpty(module);
		Assertion.checkArgNotEmpty(feature);
		Assertion.checkNotNull(checkMethod);
		//-----
		this.definitionName = definitionName;
		this.healthCheckName = healthCheckName;
		this.checker = checker;
		this.module = module;
		this.feature = feature;
		this.checkMethod = checkMethod;
	}

	@Override
	public String getName() {
		return definitionName;
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
