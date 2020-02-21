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
package io.vertigo.database.timeseries;

import java.io.Serializable;

import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public final class TimeFilter implements Serializable {

	private static final long serialVersionUID = -5930123598073570659L;

	private final String from;
	private final String to;
	private final String dim; // may be null

	TimeFilter(
			final String from,
			final String to,
			final String dim) {
		Assertion.checkNotNull(from);
		Assertion.checkNotNull(to);
		//---
		this.from = from;
		this.to = to;
		this.dim = dim;
	}

	public static TimeFilterBuilder builder(final String from, final String to) {
		return new TimeFilterBuilder(from, to);
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getDim() {
		return dim;
	}
}
