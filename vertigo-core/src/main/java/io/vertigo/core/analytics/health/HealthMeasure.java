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

import io.vertigo.core.lang.Assertion;

/**
 *  Measure.
 *  example :
 * Ping to a Database produces a specific Measure
 *
 * @author jmforhan
 */
public final class HealthMeasure {
	private final HealthStatus status;
	private final String message; //may be null
	private final Exception cause; //may be null

	/**
	 * Creates the builder.
	 * @return the builder
	 */
	public static HealthMeasureBuilder builder() {
		return new HealthMeasureBuilder();
	}

	/**
	 * Constructor.
	 *
	 * @param status the status returned by the test
	 * @param message a message that explains the status (should be required whenever the status is not GREEN)
	 * @param cause the exception thrown during status checking (Should be empty with a GREEN status)
	 */
	HealthMeasure(
			final HealthStatus status,
			final String message,
			final Exception cause) {
		Assertion.check().isNotNull(status);
		/* 
		 * message can be null
		 * cause can be null
		 */
		//---
		this.status = status;
		this.message = message;
		this.cause = cause;
	}

	/**
	 * @return the status
	 */
	public HealthStatus getStatus() {
		return status;
	}

	/**
	 * @return the message (may be null)
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the cause exception (may be null)
	 */
	public Exception getCause() {
		return cause;
	}
}
