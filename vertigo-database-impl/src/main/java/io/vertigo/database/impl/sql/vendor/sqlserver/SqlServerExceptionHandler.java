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

import java.sql.SQLException;
import java.util.Arrays;

import io.vertigo.database.impl.sql.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour SQL Server.
 *
 * @author jmainaud, npiedeloup
 */
final class SqlServerExceptionHandler extends AbstractSqlExceptionHandler {

	private enum SQLServerVersion {
		PostSQLServer2008("«\u00A0", "\u00A0»."), // \u00A0 : no-break-space (ne part pas avec un trim)
		PostSQLServer2008b("\"", "\"."), // autre facon de SQL serveur pour lever les contraintes (au moins sur les FK)
		PreSQLServer2008("'", "'.");

		private final String startQuote;
		private final String endQuote;

		SQLServerVersion(final String startQuote, final String endQuote) {
			this.startQuote = startQuote;
			this.endQuote = endQuote;
		}

		private String getStartQuote() {
			return startQuote;
		}

		private String getEndQuote() {
			return endQuote;
		}

		String extractConstraintName(final String errorMessage) {
			final int indexFin = errorMessage.indexOf(getEndQuote());
			if (indexFin != -1) {
				final int indexDebut = errorMessage.lastIndexOf(getStartQuote(), indexFin - 1);
				if (indexDebut != -1) {
					return errorMessage.substring(indexDebut + getStartQuote().length(), indexFin);
				}
			}
			return null;
		}
	}

	/**
	 * Constructor.
	 */
	SqlServerExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public RuntimeException handleSQLException(final SQLException sqle, final String statementInfos) {
		final int errorCode = sqle.getErrorCode();

		if (errorCode >= 20_000 && errorCode < 30_000) {
			// Erreur utilisateur
			return handleUserSQLException(sqle);
		}
		switch (errorCode) {
			case 8152:
				//the value is too large for this column
				return handleTooLargeValueSqlException(sqle);
			case 547:
				// Violation de contrainte d'intégrité référentielle (#547)
				return handleForeignConstraintSQLException(sqle);
			case 2601:
			case 2627:
				// Violation de contrainte d'unicité (#2627)
				// Violation de contrainte d'unicité sur index (#2601) (attention message différent)
				return handleUniqueConstraintSQLException(sqle);
			default:
				// Message d'erreur par défaut
				return handleOtherSQLException(sqle, statementInfos);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String errorMessage) {
		return Arrays.stream(SQLServerVersion.values())
				.map(sqlServerVersion -> sqlServerVersion.extractConstraintName(errorMessage))
				.filter(constraintName -> constraintName != null)
				.findFirst()
				.orElse(null);
	}

}
