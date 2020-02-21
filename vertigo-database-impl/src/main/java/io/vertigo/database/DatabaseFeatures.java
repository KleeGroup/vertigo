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
package io.vertigo.database;

import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.database.impl.sql.SqlDataBaseManagerImpl;
import io.vertigo.database.impl.timeseries.TimeSeriesDataBaseManagerImpl;
import io.vertigo.database.plugins.sql.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.database.plugins.sql.connection.datasource.DataSourceConnectionProviderPlugin;
import io.vertigo.database.plugins.timeseries.fake.FakeTimeSeriesPlugin;
import io.vertigo.database.plugins.timeseries.influxdb.InfluxDbTimeSeriesPlugin;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.timeseries.TimeSeriesDataBaseManager;

/**
 * Defines database features.
 *
 * @author mlaroche
 */
public final class DatabaseFeatures extends Features<DatabaseFeatures> {

	/**
	 * Constructor.
	 */
	public DatabaseFeatures() {
		super("vertigo-database");
	}

	/**
	 * Add sqlDataBase management to dynamo.
	 * @return  the feature
	 */
	@Feature("sql")
	public DatabaseFeatures withSqlDataBase() {
		getModuleConfigBuilder()
				.addComponent(SqlDataBaseManager.class, SqlDataBaseManagerImpl.class);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries")
	public DatabaseFeatures withTimeSeriesDataBase() {
		getModuleConfigBuilder()
				.addComponent(TimeSeriesDataBaseManager.class, TimeSeriesDataBaseManagerImpl.class);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries.influxdb")
	public DatabaseFeatures withInfluxDb(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(InfluxDbTimeSeriesPlugin.class, params);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries.fake")
	public DatabaseFeatures withFakeTimeseries() {
		getModuleConfigBuilder()
				.addPlugin(FakeTimeSeriesPlugin.class);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("sql.datasource")
	public DatabaseFeatures withDatasource(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DataSourceConnectionProviderPlugin.class, params);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("sql.c3p0")
	public DatabaseFeatures withC3p0(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(C3p0ConnectionProviderPlugin.class, params);
		return this;
	}

	/**
	
	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//
	}
}
