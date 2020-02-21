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

import java.time.Instant;

import io.vertigo.commons.analytics.metric.Metric.Status;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Builder de metric.
 *
 * @author pchretien
 */
public final class MetricBuilder implements Builder<Metric> {

	private Instant myMeasureInstant;
	private String myName;
	private String myModule;
	private String myFeature;
	private Double myValue;
	private Status myStatus;

	/**
	 * Constructor.
	 */
	MetricBuilder() {
		super();
	}

	public MetricBuilder withName(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---
		myName = name;
		return this;
	}

	public MetricBuilder withModule(final String module) {
		Assertion.checkArgNotEmpty(module);
		//---
		myModule = module;
		return this;
	}

	public MetricBuilder withFeature(final String feature) {
		Assertion.checkArgNotEmpty(feature);
		//---
		myFeature = feature;
		return this;
	}

	public MetricBuilder withValue(final Double value) {
		Assertion.checkNotNull(value);
		//---
		myValue = value;
		return this;
	}

	public MetricBuilder withMeasureInstant(final Instant measureInstant) {
		Assertion.checkNotNull(measureInstant);
		//---
		myMeasureInstant = measureInstant;
		return this;
	}

	public MetricBuilder withSuccess() {
		myStatus = Status.SUCCESS;
		return this;
	}

	public MetricBuilder withError() {
		myStatus = Status.ERROR;
		return this;
	}

	@Override
	public Metric build() {
		if (myMeasureInstant == null) {
			myMeasureInstant = Instant.now();
		}
		return new Metric(
				myMeasureInstant != null ? myMeasureInstant : Instant.now(),
				myName,
				myModule,
				myFeature,
				myValue,
				myStatus);
	}
}
