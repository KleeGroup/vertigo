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
package io.vertigo.commons.analytics.metric;

import java.util.List;
import java.util.function.Supplier;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * This definition defines a metric
 * 			- a definition name
 * 			- a way to get a list of metrics
 * @author mlaroche, pchretien
 */
@DefinitionPrefix("Met")
public final class MetricDefinition implements Definition {

	private final String definitionName;
	private final Supplier<List<Metric>> metricSupplier;

	/**
	 * Constructor
	 * @param definitionName Definition name
	 * @param metricSupplier the  method that provides a list of metrics
	 */
	public MetricDefinition(
			final String definitionName,
			final Supplier<List<Metric>> metricSupplier) {
		Assertion.checkArgNotEmpty(definitionName);
		Assertion.checkNotNull(metricSupplier);
		//-----
		this.definitionName = definitionName;
		this.metricSupplier = metricSupplier;
	}

	@Override
	public String getName() {
		return definitionName;
	}

	/**
	 * @return the check method that provides a health measure
	 */
	public Supplier<List<Metric>> getMetricSupplier() {
		return metricSupplier;
	}

}
