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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.url.URLResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.plugins.timeseries.influxdb.InfluxDbTimeSeriesPlugin;

/**
 * Test of the IoT services.
 *
 * @author mlaroche
 */
public final class TimeSeriesDataBaseTest extends AbstractTestCaseJU5 {

	@Inject
	private TimeSeriesDataBaseManager timeSeriesDataBaseManager;

	@Test
	public void testInsertMeasure() {
		final Measure measure = Measure.builder("test")
				.time(Instant.now())
				.addField("temp", 12)
				.build();
		timeSeriesDataBaseManager.insertMeasure("vertigo-test", measure);
	}

	@Test
	public void testInsertMeasureBatch() {
		final Measure measure1 = Measure.builder("test")
				.time(Instant.now())
				.addField("temp", 11)
				.build();
		final Measure measure2 = Measure.builder("test")
				.time(Instant.now())
				.addField("temp", 12)
				.build();
		timeSeriesDataBaseManager.insertMeasures("vertigo-test", Arrays.asList(measure1, measure2));
	}

	@Test
	public void testReadMeasures() {
		timeSeriesDataBaseManager.getTimeSeries(
				"vertigo-test",
				Collections.singletonList("temp:mean"),
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build());
	}

	@Test
	public void testReadMeasuresClusteredTimeSeries() {
		timeSeriesDataBaseManager.getClusteredTimeSeries(
				"vertigo-test",
				new ClusteredMeasure("test:mean", Collections.singletonList(10)),
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build());
	}

	@Test
	public void testReadMeasuresTimedTabular() {

		timeSeriesDataBaseManager.getTabularTimedData(
				"vertigo-test",
				Collections.singletonList("temp:mean"),
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build());
	}

	@Test
	public void testReadMeasuresTabular() {
		timeSeriesDataBaseManager.getTabularData(
				"vertigo-test",
				Collections.singletonList("temp:mean"),
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build());
	}

	@Test
	public void testReadMeasuresTags() {
		timeSeriesDataBaseManager.getTagValues(
				"vertigo-test",
				"temp",
				"myTag");
	}

	@Test
	public void testReadMeasuresTops() {
		timeSeriesDataBaseManager.getTops(
				"vertigo-test",
				"temp:mean",
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build(),
				"temp",
				10);
	}

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder().beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.addPlugin(URLResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DatabaseFeatures()
						.withTimeSeriesDataBase()
						.addPlugin(InfluxDbTimeSeriesPlugin.class,
								Param.of("host", "http://analytica.part.klee.lan.net:8086"),
								Param.of("user", "analytica"),
								Param.of("password", "kleeklee"),
								Param.of("dbNames", "vertigo-test"))
						.build())
				.build();
	}

}
