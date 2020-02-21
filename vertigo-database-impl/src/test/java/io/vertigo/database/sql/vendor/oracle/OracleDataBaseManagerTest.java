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
package io.vertigo.database.sql.vendor.oracle;

import org.junit.jupiter.api.Disabled;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.database.impl.sql.vendor.oracle.Oracle11DataBase;
import io.vertigo.database.impl.sql.vendor.oracle.OracleDataBase;
import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.mapper.MailAdapterSupplierPlugin;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

@Disabled
public final class OracleDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {

	private static final boolean ORACLE_11 = false;

	@Override
	public SqlDialect getDialect() {
		return ORACLE_11 ? new Oracle11DataBase().getSqlDialect() : new OracleDataBase().getSqlDialect();
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
								Param.of("dataBaseClass", ORACLE_11 ? Oracle11DataBase.class.getName() : OracleDataBase.class.getName()),
								Param.of("jdbcDriver", "oracle.jdbc.OracleDriver"),
								ORACLE_11 ? Param.of("jdbcUrl", "jdbc:oracle:thin:DT_VERTIGO/DT_VERTIGO@selma.dev.klee.lan.net:1521/O11UTF8")
										: Param.of("jdbcUrl", "jdbc:oracle:thin:DT_VERTIGO/DT_VERTIGO@luna.dev.klee.lan.net:1521/O12UTF8"))
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
		return "CREATE TABLE movie ( "
				+ "id 						NUMBER(6), "
				+ "title 					VARCHAR2(255), "
				+ "mail 					VARCHAR2(255), "
				+ "fps 						NUMBER(6,3), "
				+ "income 					NUMBER(6,3), "
				+ "color 					NUMBER(1), "
				+ "release_date 			DATE, "
				+ "release_local_date 		DATE, "
				+ "release_instant 			DATE	, "
				+ "icon 					BLOB"
				+ ")";
	}

	@Override
	protected String createSequenceMovie() {
		return "CREATE SEQUENCE seq_movie";
	}

	@Override
	protected GenerationMode getExpectedGenerationMode() {
		return GenerationMode.GENERATED_COLUMNS;
	}

	@Override
	protected boolean commitRequiredOnSchemaModification() {
		return false;
	}
}
