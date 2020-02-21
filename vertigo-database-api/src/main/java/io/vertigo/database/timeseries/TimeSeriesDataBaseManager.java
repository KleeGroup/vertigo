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

import java.util.List;

import io.vertigo.core.component.Manager;

/**
 * Manages connections to a time series database.
 * @author mlaroche
 */
public interface TimeSeriesDataBaseManager extends Manager {

	void insertMeasure(
			final String dbName,
			final Measure measure);

	void insertMeasures(
			final String dbName,
			final List<Measure> measures);

	TimedDatas getTimeSeries(
			final String dbName,
			final List<String> measures,
			final DataFilter dataFilter,
			final TimeFilter timeFilter);

	TimedDatas getClusteredTimeSeries(
			final String dbName,
			final ClusteredMeasure clusteredMeasure,
			final DataFilter dataFilter,
			final TimeFilter timeFilter);

	TimedDatas getTabularTimedData(
			final String dbName,
			final List<String> measures,
			final DataFilter dataFilter,
			final TimeFilter timeFilter,
			final String... groupBy);

	TabularDatas getTabularData(
			final String dbName,
			final List<String> measures,
			final DataFilter dataFilter,
			final TimeFilter timeFilter,
			final String... groupBy);

	TabularDatas getTops(
			final String dbName,
			final String measure,
			final DataFilter dataFilter,
			final TimeFilter timeFilter,
			final String groupBy,
			final int maxRows);

	List<String> getTagValues(
			final String dbName,
			final String measurement,
			final String tag);

}
