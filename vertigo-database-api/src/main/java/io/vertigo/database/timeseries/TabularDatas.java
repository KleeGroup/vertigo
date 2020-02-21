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
public final class TabularDatas implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<TabularDataSerie> tabularDataSeries;
	private final List<String> seriesNames;

	public TabularDatas(
			final List<TabularDataSerie> tabularDataSeries,
			final List<String> seriesNames) {
		Assertion.checkNotNull(tabularDataSeries);
		Assertion.checkNotNull(seriesNames);
		//---
		this.tabularDataSeries = tabularDataSeries;
		this.seriesNames = seriesNames;
	}

	public List<TabularDataSerie> getTabularDataSeries() {
		return tabularDataSeries;
	}

	public List<String> getSeriesNames() {
		return seriesNames;
	}

}
