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
import java.util.Map;

import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public final class DataFilter implements Serializable {

	private static final long serialVersionUID = -5464636083784385506L;

	public static DataFilterBuilder builder(final String measurement) {
		return new DataFilterBuilder(measurement);
	}

	private final String measurement;
	private final Map<String, String> filters;

	private final String additionalWhereClause;// may be null

	DataFilter(
			final String measurement,
			final Map<String, String> filters,
			final String additionalWhereClause) {
		Assertion.checkNotNull(measurement);
		Assertion.checkNotNull(filters);
		//---
		this.measurement = measurement;
		this.filters = filters;
		this.additionalWhereClause = additionalWhereClause;
	}

	public String getAdditionalWhereClause() {
		return additionalWhereClause;
	}

	public Map<String, String> getFilters() {
		return filters;
	}

	public String getMeasurement() {
		return measurement;
	}

}
