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
package io.vertigo.database.sql.vendor.postgresql;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.database.impl.sql.vendor.postgresql.PostgreSqlDataBase;
import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.mapper.MailAdapterSupplierPlugin;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

public final class PostgreSqlDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	@Override
	public SqlDialect getDialect() {
		return new PostgreSqlDataBase().getSqlDialect();
	}

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", PostgreSqlDataBase.class.getName()),
								Param.of("jdbcDriver", "org.postgresql.Driver"),
								Param.of("jdbcUrl", "jdbc:postgresql://docker-vertigo.part.klee.lan.net:5432/postgres?user=postgres&amp;password=postgres"))
						.withC3p0(
								Param.of("name", "secondary"),
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:secondaryDatabase"))
						.addPlugin(MailAdapterSupplierPlugin.class)
						.build())
				.build();
	}

	@Override
	protected String createTableMovie() {
		final String myString = "CREATE TABLE movie ( "
				+ "id 						NUMERIC(6), "
				+ "title 					VARCHAR(255), "
				+ "mail 					VARCHAR(255), "
				+ "fps 						NUMERIC(6,3), "
				+ "income 					NUMERIC(6,3), "
				+ "color 					BOOLEAN, "
				+ "release_date 			TIMESTAMP, "
				+ "release_local_date 		DATE, "
				+ "release_instant 			TIMESTAMP, "
				+ "icon 					BYTEA"
				+ ")";
		return myString;
	}

	@Override
	protected String createSequenceMovie() {
		return "CREATE SEQUENCE seq_movie";
	}

	@Override
	protected GenerationMode getExpectedGenerationMode() {
		return GenerationMode.GENERATED_KEYS;
	}

	@Override
	protected boolean commitRequiredOnSchemaModification() {
		return true;
	}

}
