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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

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
		super();
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
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.GREEN;
		myMessage = message;
		return this;
	}

	/**
	 *
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	 */
	public HealthMeasureBuilder withYellowStatus(final String message, final Exception cause) {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.YELLOW;
		myMessage = message;
		myCause = cause;
		return this;
	}

	/**
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	*/
	public HealthMeasureBuilder withRedStatus(final String message, final Exception cause) {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.RED;
		myMessage = message;
		myCause = cause;
		return this;
	}

	@Override
	public HealthMeasure build() {
		return new HealthMeasure(myStatus, myMessage, myCause);
	}
}
