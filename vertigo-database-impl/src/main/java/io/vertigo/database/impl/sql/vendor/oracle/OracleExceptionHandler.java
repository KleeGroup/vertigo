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
package io.vertigo.database.impl.sql.vendor.oracle;

import java.sql.SQLException;

import io.vertigo.database.impl.sql.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour Oracle.
 *
 * @author npiedeloup
 */
final class OracleExceptionHandler extends AbstractSqlExceptionHandler {
	/**
	 * Constructor.
	 */
	OracleExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public RuntimeException handleSQLException(final SQLException sqle, final String statementInfos) {
		final int errCode = sqle.getErrorCode();
		if (errCode >= 20_000 && errCode < 30_000) {
			// Erreur utilisateur
			return handleUserSQLException(sqle);
		}
		switch (errCode) {
			case 1438:
			case 12_899:
				//the value is too large for this column
				return handleTooLargeValueSqlException(sqle);
			case 2292:
				// Violation de contrainte d'intégrité référentielle
				return handleForeignConstraintSQLException(sqle);
			case 1:
				// Violation de contrainte d'unicité
				return handleUniqueConstraintSQLException(sqle);
			default:
				// default message
				return handleOtherSQLException(sqle, statementInfos);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		final int i1 = msg.indexOf('.', msg.indexOf('('));
		final int i2 = msg.indexOf(')', i1);
		if (i1 > -1 && i2 > -1) {
			return msg.substring(i1 + 1, i2);
		}
		return null;
	}
}
