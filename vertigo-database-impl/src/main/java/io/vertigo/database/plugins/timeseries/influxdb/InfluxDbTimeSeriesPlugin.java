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
package io.vertigo.database.plugins.timeseries.influxdb;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;

import io.vertigo.core.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.database.impl.timeseries.TimeSeriesDataBaseManagerImpl;
import io.vertigo.database.impl.timeseries.TimeSeriesPlugin;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TabularDataSerie;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimedDataSerie;
import io.vertigo.database.timeseries.TimedDatas;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuple;

/**
 * @author mlaroche
 *
 */
public final class InfluxDbTimeSeriesPlugin implements TimeSeriesPlugin, Activeable {

	private final InfluxDB influxDB;
	private List<String> dbNames;

	@Inject
	public InfluxDbTimeSeriesPlugin(
			@ParamValue("host") final String host,
			@ParamValue("user") final String user,
			@ParamValue("password") final String password,
			@ParamValue("dbNames") final Optional<String> dbNamesOpt) {
		Assertion.checkArgNotEmpty(host);
		Assertion.checkArgNotEmpty(user);
		Assertion.checkArgNotEmpty(password);
		//---
		influxDB = InfluxDBFactory.connect(host, user, password);
		influxDB.enableBatch();
		if (dbNamesOpt.isPresent()) {
			dbNames = Arrays.asList(dbNamesOpt.get().split(";"));
			createDatabases();
		} else {
			dbNames = Collections.singletonList(TimeSeriesDataBaseManagerImpl.WILDCARD_PLUGIN);
			// we do not create databases because we are the wildcard one...
		}
	}

	@Override
	public void start() {
		// nothing

	}

	@Override
	public void stop() {
		influxDB.disableBatch();
	}

	public void createDatabases() {
		final Set<String> existingDatabases = influxDB.query(new Query("SHOW DATABASES", null))
				.getResults()
				.get(0)
				.getSeries()
				.get(0)
				.getValues()
				.stream()
				.map(values -> (String) values.get(0))
				.collect(Collectors.toSet());

		for (final String dbName : dbNames) {
			if (TimeSeriesDataBaseManagerImpl.WILDCARD_PLUGIN != dbName && !existingDatabases.contains(dbName)) {
				influxDB.query(new Query("CREATE DATABASE \"" + dbName + "\"", dbName));
			}
		}

	}

	private TimedDatas executeTimedTabularQuery(final String appName, final String queryString) {
		final Query query = new Query(queryString, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> series = queryResult.getResults().get(0).getSeries();

		if (series != null && !series.isEmpty()) {
			//all columns are the measures
			final List<String> seriesName = new ArrayList<>();
			seriesName.addAll(series.get(0).getColumns().subList(1, series.get(0).getColumns().size()));//we remove the first one
			seriesName.addAll(series.get(0).getTags().keySet());// + all the tags names (the group by)

			final List<TimedDataSerie> dataSeries = series
					.stream()
					.map(mySeries -> {
						final Map<String, Object> mapValues = buildMapValue(mySeries.getColumns(), mySeries.getValues().get(0));
						mapValues.putAll(mySeries.getTags());
						return new TimedDataSerie(LocalDateTime.parse(mySeries.getValues().get(0).get(0).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(ZoneOffset.UTC), mapValues);
					})
					.collect(Collectors.toList());

			return new TimedDatas(dataSeries, seriesName);
		}
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	private TabularDatas executeTabularQuery(final String appName, final String queryString) {
		final Query query = new Query(queryString, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> series = queryResult.getResults().get(0).getSeries();

		if (series != null && !series.isEmpty()) {
			//all columns are the measures
			final List<String> seriesName = new ArrayList<>();
			seriesName.addAll(series.get(0).getColumns().subList(1, series.get(0).getColumns().size()));//we remove the first one
			if (series.get(0).getTags() != null) {
				seriesName.addAll(series.get(0).getTags().keySet());// + all the tags names (the group by)
			}

			final List<TabularDataSerie> dataSeries = series
					.stream()
					.map(mySeries -> {
						final Map<String, Object> mapValues = buildMapValue(mySeries.getColumns(), mySeries.getValues().get(0));
						if (mySeries.getTags() != null) {
							mapValues.putAll(mySeries.getTags());
						}
						return new TabularDataSerie(mapValues);
					})
					.collect(Collectors.toList());

			return new TabularDatas(dataSeries, seriesName);
		}
		return new TabularDatas(Collections.emptyList(), Collections.emptyList());
	}

	private TimedDatas executeTimedQuery(final String appName, final String q) {
		final Query query = new Query(q, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> seriesList = queryResult.getResults().get(0).getSeries();
		if (seriesList != null && !seriesList.isEmpty()) {

			final Series series = seriesList.get(0);
			final List<TimedDataSerie> dataSeries = series
					.getValues()
					.stream()
					.map(values -> new TimedDataSerie(LocalDateTime.parse(values.get(0).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(ZoneOffset.UTC), buildMapValue(series.getColumns(), values)))
					.collect(Collectors.toList());
			return new TimedDatas(dataSeries, new ArrayList<>(series.getColumns().subList(1, series.getColumns().size())));//we remove the first one
		}
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TimedDatas getClusteredTimeSeries(final String appName, final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
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
		final String fieldName = clusteredMeasure.getMeasure().split(":")[0];
		final String standardwhereClause = buildWhereClause(dataFilter, timeFilter);// the where clause is almost the same for each cluster
		final StringBuilder selectClause = new StringBuilder();
		final StringBuilder fromClause = new StringBuilder();
		Integer minThreshold = null;

		// for each cluster defined by the thresholds we add a subquery (after benchmark it's the fastest solution)
		for (int i = 0; i <= clusteredMeasure.getThresholds().size(); i++) {
			Integer maxThreshold = null;
			if (i < clusteredMeasure.getThresholds().size()) {
				maxThreshold = clusteredMeasure.getThresholds().get(i);
			}
			// we add the where clause of the cluster value > threshold_1 and value <= threshold_2
			appendMeasureThreshold(
					minThreshold,
					maxThreshold,
					fieldName,
					clusteredMeasure.getMeasure(),
					dataFilter.getMeasurement(),
					standardwhereClause,
					timeFilter.getDim(),
					fromClause,
					i);

			// we construct the top select clause. we use the max as the aggregate function. No conflict possible
			selectClause.append(" max(\"").append(fieldName).append('_').append(i)
					.append("\") as \"").append(clusterName(minThreshold, maxThreshold, clusteredMeasure.getMeasure())).append('"');
			if (i < clusteredMeasure.getThresholds().size()) {
				selectClause.append(',');
				fromClause.append(", ");
			}

			minThreshold = maxThreshold;
		}

		// the global query
		final String request = new StringBuilder()
				.append("select ").append(selectClause)
				.append(" from ").append(fromClause)
				.append(" where time > ").append(timeFilter.getFrom()).append(" and time <").append(timeFilter.getTo())
				.append(" group by time(").append(timeFilter.getDim()).append(')')
				.toString();

		return executeTimedQuery(appName, request);
	}

	@Override
	public TimedDatas getTabularTimedData(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		final StringBuilder queryBuilder = buildQuery(measures, dataFilter, timeFilter);

		final String groupByClause = Stream.of(groupBy)
				.collect(Collectors.joining("\", \"", "\"", "\""));

		queryBuilder.append(" group by ").append(groupByClause);
		final String queryString = queryBuilder.toString();

		return executeTimedTabularQuery(appName, queryString);
	}

	@Override
	public TabularDatas getTabularData(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		final StringBuilder queryBuilder = buildQuery(measures, dataFilter, timeFilter);

		final String groupByClause = Stream.of(groupBy)
				.collect(Collectors.joining("\", \"", "\"", "\""));

		queryBuilder.append(" group by ").append(groupByClause);
		final String queryString = queryBuilder.toString();

		return executeTabularQuery(appName, queryString);
	}

	@Override
	public List<String> getTagValues(final String appName, final String measurement, final String tag) {
		final String queryString = new StringBuilder("show tag values on ")
				.append("\"").append(appName).append("\"")
				.append(" from ").append("\"").append(measurement).append("\"")
				.append("  with key= ").append("\"").append(tag).append("\"")
				.toString();

		final Query query = new Query(queryString, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> seriesList = queryResult.getResults().get(0).getSeries();
		if (seriesList != null && !seriesList.isEmpty()) {
			final Series series = seriesList.get(0);
			return series
					.getValues()
					.stream()
					.map(values -> values.get(1).toString()) //always the second columns
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public TimedDatas getTimeSeries(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkNotNull(measures);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter.getDim());// we check dim is not null because we need it
		//---
		final String q = buildQuery(measures, dataFilter, timeFilter)
				.append(" group by time(").append(timeFilter.getDim()).append(')')
				.toString();

		return executeTimedQuery(appName, q);

	}

	@Override
	public TabularDatas getTops(final String appName, final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		final String queryString = new StringBuilder()
				.append("select top(").append("\"top_").append(measure).append("\", \"").append(groupBy).append("\", ").append(maxRows).append(") as \"").append(measure).append('"')
				.append(" from ( select ").append(buildMeasureQuery(measure, "top_" + measure))
				.append(" from ").append(dataFilter.getMeasurement())
				.append(buildWhereClause(dataFilter, timeFilter))
				.append(" group by \"").append(groupBy).append('"')
				.append(')')
				.toString();

		return executeTabularQuery(appName, queryString);
	}

	@Override
	public void insertMeasure(final String dbName, final Measure measure) {
		Assertion.checkArgNotEmpty(dbName);
		Assertion.checkNotNull(measure);
		//---
		influxDB.setDatabase(dbName);
		influxDB.write(Point.measurement(measure.getMeasurement())
				.time(measure.getInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
				.fields(measure.getFields())
				.tag(measure.getTags())
				.build());

	}

	@Override
	public void insertMeasures(final String dbName, final List<Measure> measures) {
		// with influxdb we make the choice to always be in batch mode for performance so we can safely iterate.
		measures.forEach(measure -> insertMeasure(dbName, measure));
	}

	private static void appendMeasureThreshold(
			final Integer previousThreshold,
			final Integer currentThreshold,
			final String clusteredField,
			final String clusteredMeasure,
			final String measurement,
			final String standardwhereClause,
			final String timeDimension,
			final StringBuilder fromClauseBuilder,
			final int i) {
		fromClauseBuilder.append("(select ")
				.append(buildMeasureQuery(clusteredMeasure, clusteredField + "_" + i))
				.append(" from ").append(measurement)
				.append(standardwhereClause);
		if (previousThreshold != null) {
			fromClauseBuilder.append(" and \"").append(clusteredField).append('"').append(" > ").append(previousThreshold);
		}
		if (currentThreshold != null) {
			fromClauseBuilder.append(" and \"").append(clusteredField).append('"').append(" <= ").append(currentThreshold);
		}
		fromClauseBuilder.append(" group by time(").append(timeDimension).append(')');
		fromClauseBuilder.append(')');
	}

	private static Map<String, Object> buildMapValue(final List<String> columns, final List<Object> values) {
		final Map<String, Object> valueMap = new HashMap<>();
		// we start at 1 because time is always the first row
		for (int i = 1; i < columns.size(); i++) {
			valueMap.put(columns.get(i), values.get(i));
		}
		return valueMap;
	}

	private static String buildMeasureQuery(final String measure, final String alias) {
		Assertion.checkArgNotEmpty(measure);
		Assertion.checkArgNotEmpty(alias);
		//----
		final String[] measureDetails = measure.split(":");
		final Tuple<String, List<String>> aggregateFunction = parseAggregateFunction(measureDetails[1]);
		// append function name
		final StringBuilder measureQueryBuilder = new java.lang.StringBuilder(aggregateFunction.getVal1()).append("(\"").append(measureDetails[0]).append("\"");
		// append parameters
		if (!aggregateFunction.getVal2().isEmpty()) {
			measureQueryBuilder.append(aggregateFunction.getVal2()
					.stream()
					.collect(Collectors.joining(",", ", ", "")));
		}
		// end measure and add alias
		measureQueryBuilder.append(") as \"").append(alias).append('"');
		return measureQueryBuilder.toString();
	}

	private static StringBuilder buildQuery(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkNotNull(measures);
		//---
		final StringBuilder queryBuilder = new StringBuilder("select ");
		String separator = "";
		for (final String measure : measures) {
			queryBuilder
					.append(separator)
					.append(buildMeasureQuery(measure, measure));
			separator = " ,";
		}
		queryBuilder.append(" from ").append(dataFilter.getMeasurement());
		queryBuilder.append(buildWhereClause(dataFilter, timeFilter));
		return queryBuilder;
	}

	private static String buildWhereClause(final DataFilter dataFilter, final TimeFilter timeFilter) {
		final StringBuilder queryBuilder = new StringBuilder()
				.append(" where time > ").append(timeFilter.getFrom()).append(" and time <").append(timeFilter.getTo());

		for (final Map.Entry<String, String> filter : dataFilter.getFilters().entrySet()) {
			if (filter.getValue() != null && !"*".equals(filter.getValue())) {
				queryBuilder.append(" and \"").append(filter.getKey()).append("\"='").append(filter.getValue()).append('\'');
			}
		}
		if (dataFilter.getAdditionalWhereClause() != null) {
			queryBuilder.append(" and ").append(dataFilter.getAdditionalWhereClause());
		}
		return queryBuilder.toString();
	}

	private static String clusterName(
			final Integer minThreshold,
			final Integer maxThreshold,
			final String measure) {
		if (minThreshold == null) {
			return measure + '<' + maxThreshold;
		} else if (maxThreshold == null) {
			return measure + '>' + minThreshold;
		} else {
			return measure + '_' + maxThreshold;
		}
	}

	private static Tuple<String, List<String>> parseAggregateFunction(final String aggregateFunction) {
		final int firstSeparatorIndex = aggregateFunction.indexOf('_');
		if (firstSeparatorIndex > -1) {
			return Tuple.of(
					aggregateFunction.substring(0, firstSeparatorIndex),
					Arrays.asList(aggregateFunction.substring(firstSeparatorIndex + 1).split("_")));
		}
		return Tuple.of(aggregateFunction, Collections.emptyList());
	}

	@Override
	public List<String> getDbNames() {
		return dbNames;
	}

}
