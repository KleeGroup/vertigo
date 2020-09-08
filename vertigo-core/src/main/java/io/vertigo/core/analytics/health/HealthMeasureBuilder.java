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
package io.vertigo.core.analytics.health;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * This builder must be used to build a health measure.
 * @author pchretien
 *
 */
public final class HealthMeasureBuilder implements Builder<HealthMeasure> {
	private HealthStatus myStatus;
	private String myMessage; //may be null
	private Exception myCause; //may be null

	HealthMeasureBuilder() {
		//package : use the builder instead
	}

	/**
	 * @return this builder
	 */
	public HealthMeasureBuilder withGreenStatus() {
		return withGreenStatus(null);
	}

	/**
	 * @return this builder
	 */
	public HealthMeasureBuilder withGreenStatus(final String message) {
		return withStatus(HealthStatus.GREEN, message, null);
	}

	/**
	 *
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	 */
	public HealthMeasureBuilder withYellowStatus(final String message, final Exception cause) {
		return withStatus(HealthStatus.YELLOW, message, cause);
	}

	/**
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	*/
	public HealthMeasureBuilder withRedStatus(final String message, final Exception cause) {
		return withStatus(HealthStatus.RED, message, cause);
	}

	private HealthMeasureBuilder withStatus(final HealthStatus healthStatus, final String message, final Exception cause) {
		Assertion.check()
				.isNull(myStatus, "status already set")
				.isNotNull(healthStatus);
		//---
		myStatus = healthStatus;
		myMessage = message;
		myCause = cause;
		return this;
	}

	@Override
	public HealthMeasure build() {
		return new HealthMeasure(myStatus, myMessage, myCause);
	}
}
