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

import java.util.HashMap;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * @author mlaroche
 *
 */
public final class DataFilterBuilder implements Builder<DataFilter> {

	private final String myMeasurement;
	private final Map<String, String> myFilters = new HashMap<>();
	private String myAdditionalWhereClause;

	DataFilterBuilder(
			final String measurement) {
		Assertion.checkArgNotEmpty(measurement);
		//---
		myMeasurement = measurement;
	}

	public DataFilterBuilder addFilter(final String field, final String value) {
		Assertion.checkArgNotEmpty(field);
		Assertion.checkNotNull(value);
		//---
		myFilters.put(field, value);
		return this;
	}

	public DataFilterBuilder withAdditionalWhereClause(final String additionalWhereClause) {
		Assertion.checkArgNotEmpty(additionalWhereClause);
		//---
		myAdditionalWhereClause = additionalWhereClause;
		return this;
	}

	@Override
	public DataFilter build() {
		return new DataFilter(
				myMeasurement,
				myFilters,
				myAdditionalWhereClause);
	}

}
