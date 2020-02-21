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
package io.vertigo.database.impl.sql.vendor.sqlserver;

import io.vertigo.database.impl.sql.vendor.core.SqlVendorMapping;
import io.vertigo.database.sql.vendor.SqlDataBase;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.database.sql.vendor.SqlExceptionHandler;
import io.vertigo.database.sql.vendor.SqlMapping;

/**
 * Gestion de la base de données SqlServer.
 *
 * @author pchretien
 */
public final class SqlServerDataBase implements SqlDataBase {
	private final SqlExceptionHandler sqlExceptionHandler = new SqlServerExceptionHandler();
	private final SqlMapping sqlVendorMapping = SqlVendorMapping.createWithBooleanAsBit();
	private final SqlDialect sqlDialect = new SqlServerDialect();

	/** {@inheritDoc} */
	@Override
	public SqlExceptionHandler getSqlExceptionHandler() {
		return sqlExceptionHandler;
	}

	/** {@inheritDoc} */
	@Override
	public SqlMapping getSqlMapping() {
		return sqlVendorMapping;
	}

	@Override
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}
}
