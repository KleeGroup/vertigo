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
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public final class TimedDatas implements Serializable {
	private static final long serialVersionUID = 1L;

	private final List<TimedDataSerie> timedDataSeries;
	private final List<String> seriesNames;

	public TimedDatas(
			final List<TimedDataSerie> timedDataSeries,
			final List<String> seriesNames) {
		Assertion.checkNotNull(timedDataSeries);
		Assertion.checkNotNull(seriesNames);
		//---
		this.timedDataSeries = timedDataSeries;
		this.seriesNames = seriesNames;
	}

	public List<TimedDataSerie> getTimedDataSeries() {
		return timedDataSeries;
	}

	public List<String> getSeriesNames() {
		return seriesNames;
	}

}
