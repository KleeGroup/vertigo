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
package io.vertigo.core.analytics.metric.definitions;

import java.util.List;
import java.util.function.Supplier;

import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * This definition defines a metric
 * 			- a definition name
 * 			- a way to get a list of metrics
 * @author mlaroche, pchretien
 */
@DefinitionPrefix(MetricDefinition.PREFIX)
public final class MetricDefinition extends AbstractDefinition {
	public static final String PREFIX = "Met";
	private final Supplier<List<Metric>> metricSupplier;

	/**
	 * Constructor
	 * @param name Definition name
	 * @param metricSupplier the  method that provides a list of metrics
	 */
	public MetricDefinition(
			final String name,
			final Supplier<List<Metric>> metricSupplier) {
		super(name);
		//---
		Assertion.check().isNotNull(metricSupplier);
		//---
		this.metricSupplier = metricSupplier;
	}

	/**
	 * @return the check method that provides a health measure
	 */
	public Supplier<List<Metric>> getMetricSupplier() {
		return metricSupplier;
	}

}
