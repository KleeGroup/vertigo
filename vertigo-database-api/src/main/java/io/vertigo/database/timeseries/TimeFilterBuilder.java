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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * @author mlaroche
 *
 */
public final class TimeFilterBuilder implements Builder<TimeFilter> {

	private final String myFrom;
	private final String myTo;
	private String myDim;

	TimeFilterBuilder(
			final String from, final String to) {
		Assertion.checkArgNotEmpty(from);
		Assertion.checkArgNotEmpty(to);
		//---
		myFrom = from;
		myTo = to;
	}

	@Override
	public TimeFilter build() {
		return new TimeFilter(
				myFrom,
				myTo,
				myDim);
	}

	public TimeFilterBuilder withTimeDim(final String dim) {
		Assertion.checkArgNotEmpty(dim);
		//---
		myDim = dim;
		return this;
	}
}
