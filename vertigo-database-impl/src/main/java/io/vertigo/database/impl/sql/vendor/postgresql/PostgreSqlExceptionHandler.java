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
package io.vertigo.database.impl.sql.vendor.postgresql;

import java.sql.SQLException;
import java.util.Locale;

import io.vertigo.database.impl.sql.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour PostgreSQL.
 *
 * @author pforhan
 */
final class PostgreSqlExceptionHandler extends AbstractSqlExceptionHandler {
	/**
	 * Constructor.
	 */
	PostgreSqlExceptionHandler() {
		super();
	}

	@Override
	public RuntimeException handleSQLException(final SQLException sqle, final String statementInfos) {
		final String errCode = sqle.getSQLState();
		//some database return null at getSQLState (@see http://stackoverflow.com/questions/26383624/postgres-exceptions-and-java)
		final String code = errCode != null ? errCode.substring(0, 2) : null;
		if ("22001".equals(errCode) || "22003".equals(errCode)) {
			//the value is too large for this column
			return handleTooLargeValueSqlException(sqle);
		} else if ("23503".equals(errCode)) {
			// Violation de contrainte d'intégrité référentielle
			return handleForeignConstraintSQLException(sqle);
		} else if ("23505".equals(errCode)) {
			// Violation de contrainte d'unicité
			return handleUniqueConstraintSQLException(sqle);
		} else if ("01".equals(code) || "02".equals(code) || "08".equals(code) || "22".equals(code) || "23".equals(code)) {
			// Erreur utilisateur
			return handleUserSQLException(sqle);
		} else {
			// Message d'erreur par défaut
			return handleOtherSQLException(sqle, statementInfos);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		String constraintName = extractConstraintName(msg, "constraint", '"', '"');
		if (constraintName == null) {
			constraintName = extractConstraintName(msg, "contrainte", '«', '»');
		}
		return constraintName;
	}

	private static String extractConstraintName(
			final String msg,
			final String constraintName,
			final char constraintNameStart,
			final char constraintNameEnd) {
		final int i1 = msg.indexOf(constraintNameStart, msg.indexOf(constraintName));
		final int i2 = msg.indexOf(constraintNameEnd, i1 + 1);
		if (i1 > -1 && i2 > -1 && i2 > i1) {
			return msg.substring(i1 + 1, i2).toUpperCase(Locale.ENGLISH).trim();
		}
		return null;
	}

}
