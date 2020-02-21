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
package io.vertigo.database.impl.timeseries;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimeSeriesDataBaseManager;
import io.vertigo.database.timeseries.TimedDatas;
import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public class TimeSeriesDataBaseManagerImpl implements TimeSeriesDataBaseManager {

	public static final String WILDCARD_PLUGIN = "*";

	private static final String TIMESERIES_CATEGORY = "timeseries";

	private final AnalyticsManager analyticsManager;
	private final Map<String, TimeSeriesPlugin> timeSeriesPluginByDb = new HashMap<>();
	private final Optional<TimeSeriesPlugin> wildcardPluginOpt;

	@Inject
	public TimeSeriesDataBaseManagerImpl(
			final AnalyticsManager analyticsManager,
			final List<TimeSeriesPlugin> timeSeriesPlugins) {
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(timeSeriesPlugins);
		//---
		this.analyticsManager = analyticsManager;
		timeSeriesPlugins.forEach(
				plugin -> plugin.getDbNames()
						.forEach(dbName -> {
							Assertion.checkState(!timeSeriesPluginByDb.containsKey(dbName), "Db '{0}' already registered ", dbName);
							//---
							timeSeriesPluginByDb.put(dbName, plugin);

						}));
		//--- we try to find a wildcard plugins if any
		wildcardPluginOpt = Optional.ofNullable(timeSeriesPluginByDb.get(WILDCARD_PLUGIN));
	}

	@Override
	public void insertMeasure(final String dbName, final Measure measure) {
		analyticsManager.trace(
				TIMESERIES_CATEGORY,
				"/insertMeasure/" + dbName,
				tracer -> {
					tracer.setMeasure("size", 1.0);
					getPluginByDb(dbName).insertMeasure(dbName, measure);
				});

	}

	@Override
	public void insertMeasures(final String dbName, final List<Measure> measures) {
		analyticsManager.trace(
				TIMESERIES_CATEGORY,
				"/insertMeasure/" + dbName,
				tracer -> {
					tracer.setMeasure("size", measures.size());
					getPluginByDb(dbName).insertMeasures(dbName, measures);
				});

	}

	@Override
	public TimedDatas getTimeSeries(final String dbName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkArgNotEmpty(dbName);
		Assertion.checkNotNull(measures);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter.getDim());// we check dim is not null because we need it
		//---
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/timed/" + dbName + "/" + dataFilter.getMeasurement(),
				tracer -> getPluginByDb(dbName).getTimeSeries(dbName, measures, dataFilter, timeFilter));

	}

	@Override
	public TimedDatas getClusteredTimeSeries(final String dbName, final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkArgNotEmpty(dbName);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter);
		Assertion.checkNotNull(timeFilter.getDim()); // we check dim is not null because we need it
		Assertion.checkNotNull(clusteredMeasure);
		//---
		Assertion.checkArgNotEmpty(clusteredMeasure.getMeasure());
		Assertion.checkNotNull(clusteredMeasure.getThresholds());
		Assertion.checkState(!clusteredMeasure.getThresholds().isEmpty(), "For clustering the measure '{0}' you need to provide at least one threshold", clusteredMeasure.getMeasure());
		//we use the natural order
		clusteredMeasure.getThresholds().sort(Comparator.naturalOrder());
		//---
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/clusturedTimed/" + dbName + "/" + dataFilter.getMeasurement(),
				tracer -> getPluginByDb(dbName).getClusteredTimeSeries(dbName, clusteredMeasure, dataFilter, timeFilter));
	}

	@Override
	public TimedDatas getTabularTimedData(final String dbName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/tabularTimed/" + dbName + "/" + dataFilter.getMeasurement(),
				tracer -> getPluginByDb(dbName).getTabularTimedData(dbName, measures, dataFilter, timeFilter, groupBy));
	}

	@Override
	public TabularDatas getTabularData(final String dbName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/tabular/" + dbName + "/" + dataFilter.getMeasurement(),
				tracer -> getPluginByDb(dbName).getTabularData(dbName, measures, dataFilter, timeFilter, groupBy));
	}

	@Override
	public TabularDatas getTops(final String dbName, final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/tops/" + dbName + "/" + dataFilter.getMeasurement(),
				tracer -> getPluginByDb(dbName).getTops(dbName, measure, dataFilter, timeFilter, groupBy, maxRows));
	}

	@Override
	public List<String> getTagValues(final String dbName, final String measurement, final String tag) {
		return analyticsManager.traceWithReturn(
				TIMESERIES_CATEGORY,
				"/tags/" + dbName + "/" + measurement,
				tracer -> getPluginByDb(dbName).getTagValues(dbName, measurement, tag));
	}

	private TimeSeriesPlugin getPluginByDb(final String dbName) {
		Assertion.checkArgNotEmpty(dbName);
		// ---
		final TimeSeriesPlugin adequatePlugin = timeSeriesPluginByDb.get(dbName);
		if (adequatePlugin != null) {
			return adequatePlugin;
		}
		return wildcardPluginOpt.orElseThrow(() -> new IllegalArgumentException("No timeseries plugin found for db : '" + dbName + "'"));
	}

}
