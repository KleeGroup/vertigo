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
package io.vertigo.database.impl.sql;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.health.HealthChecked;
import io.vertigo.commons.analytics.health.HealthMeasure;
import io.vertigo.commons.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.component.Plugin;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;

/**
* Plugin du provider de connexions.
*
* @author pchretien
*/
public interface SqlConnectionProviderPlugin extends SqlConnectionProvider, Plugin {

	/**
	 * @return ConnectionProvider's name
	 */
	String getName();

	@HealthChecked(name = "testQuery", feature = "sqlDatabase")
	default HealthMeasure checkTestSelect() {

		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();

		final String testQuery = getDataBase().getSqlDialect().getTestQuery();
		try {

			final SqlDataBaseManager sqlDataBaseManager = Home.getApp().getComponentSpace().resolve(SqlDataBaseManager.class);
			final SqlConnection connection = obtainConnection();
			try {
				sqlDataBaseManager.executeQuery(
						SqlStatement.builder(testQuery).build(),
						Integer.class, 1,
						connection);
			} finally {
				connection.release();
			}
			healthMeasureBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}
}
