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
package io.vertigo.core.analytics.process.data;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.process.Analytics;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Component;

public class TestAnalyticsAspectServices implements Component {

	@Inject
	private AnalyticsManager analyticsManager;

	@Analytics(category = "test", name = "add")
	public int add(final int a, final int b) {

		return a + b;
	}

	@Analytics(category = "test", name = "checkPositive")
	public void checkPositive(final int a) {
		Assertion.check().isTrue(a >= 0, "The number must be positive");
	}

	@Analytics(category = "test", name = "setMeasure")
	public void setMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.setMeasure("price", 100));
	}

	@Analytics(category = "test", name = "setMeasure")
	public void setAndIncMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.setMeasure("price", 100));
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
	}

	@Analytics(category = "test", name = "incMeasure")
	public void incMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
	}

}
