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
package io.vertigo.database.impl.sql.vendor.h2;

import java.sql.SQLException;
import java.util.Locale;

import io.vertigo.database.impl.sql.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour H2.
 *
 * @author jmainaud
 */
final class H2SqlExceptionHandler extends AbstractSqlExceptionHandler {

	/** Champ DUPLICATE_KEY_1. */
	private static final int DUPLICATE_KEY_1 = 23_001;
	/** Champ UNIQUE_INDEX_1. */
	private static final int UNIQUE_INDEX_1 = 23_505;
	/** Champ REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1. */
	private static final int REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1 = 23_003;
	/** Champ REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1. */
	private static final int REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1 = 23_002;
	/** Champ VALUE_TOO_LARGE_FOR_PRECISION_1. */
	private static final int VALUE_TOO_LARGE_FOR_PRECISION_1 = 90_039;
	/** Champ VALUE_TOO_LONG_2. */
	private static final int VALUE_TOO_LONG_2 = 90_005;

	/**
	 * Crée une nouvelle instance de H2SqlExceptionHandler.
	 */
	H2SqlExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public RuntimeException handleSQLException(final SQLException sqle, final String statementInfos) {
		final int errCode = sqle.getErrorCode();
		switch (errCode) {
			case VALUE_TOO_LONG_2:
			case VALUE_TOO_LARGE_FOR_PRECISION_1:
				//the value is too large for this column
				return handleTooLargeValueSqlException(sqle);
			case REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1:
			case REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1:
				// Violation de contrainte d'intégrité référentielle
				return handleForeignConstraintSQLException(sqle);
			case DUPLICATE_KEY_1:
			case UNIQUE_INDEX_1:
				// Violation de contrainte d'unicité
				return handleUniqueConstraintSQLException(sqle);
			default:
				// Message d'erreur par défaut
				return handleOtherSQLException(sqle, statementInfos);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		return extractConstraintName(msg, "violation", '"', ' ');
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

	/*
	 * Exemple de messages d'erreur.
	 *
	 * VALUE_TOO_LONG_2: Value too long for column "NAME VARCHAR(2)": "'Hello' (5)"; SQL statement: INSERT INTO TEST VALUES(1, 'Hello')
	 * [90005-147]
	 *
	 * VALUE_TOO_LARGE_FOR_PRECISION_1: The value is too large for the precision "2"; SQL statement: SELECT * FROM TABLE(X DECIMAL(2, 2) =
	 * (123.34)) [90039-147]
	 *
	 * REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1: Referential integrity constraint violation:
	 * "CONSTRAINT_3: PUBLIC.CHILD FOREIGN KEY(P_ID) REFERENCES PUBLIC.PARENT(ID)"; SQL statement: INSERT INTO CHILD VALUES(1) [23002-147]
	 *
	 * REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1: Referential integrity constraint violation:
	 * "CONSTRAINT_3: PUBLIC.CHILD FOREIGN KEY(P_ID) REFERENCES PUBLIC.PARENT(ID)"; SQL statement: DELETE FROM PARENT [23003-147]
	 *
	 * DUPLICATE_KEY_1: Unique index or primary key violation: "PRIMARY KEY ON PUBLIC.TEST(ID)"; SQL statement: INSERT INTO TEST VALUES(1)
	 * [23001-147]
	 *
	 * UNIQUE_INDEX_1: Unique index ou violation de clé primaire: "INS_UNIQUE_UTI_SES_INDEX_9 ON PUBLIC.INSCRIPTION (UTI_ID, SES_ID) VALUES
	 * (/ * clé: 1010 * / null, null, null, null , null, null, null, 1003, 1) ";
	 * [23505-176]
	 */

}
