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
package io.vertigo.database.plugins.timeseries.fake;

import java.util.Collections;
import java.util.List;

import io.vertigo.database.impl.timeseries.TimeSeriesDataBaseManagerImpl;
import io.vertigo.database.impl.timeseries.TimeSeriesPlugin;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimedDatas;

/**
 * @author mlaroche
 *
 */
public final class FakeTimeSeriesPlugin implements TimeSeriesPlugin {

	@Override
	public TimedDatas getClusteredTimeSeries(final String appName, final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TimedDatas getTabularTimedData(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TabularDatas getTabularData(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return new TabularDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public List<String> getTagValues(final String appName, final String measurement, final String tag) {
		return Collections.emptyList();
	}

	@Override
	public TimedDatas getTimeSeries(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TabularDatas getTops(final String appName, final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		return new TabularDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public void insertMeasure(final String dbName, final Measure measure) {
		// do nothing
	}

	@Override
	public void insertMeasures(final String dbName, final List<Measure> measures) {
		// do nothing
	}

	@Override
	public List<String> getDbNames() {
		return Collections.singletonList(TimeSeriesDataBaseManagerImpl.WILDCARD_PLUGIN);
	}
}
