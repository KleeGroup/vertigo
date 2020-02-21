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
package io.vertigo.database.impl.sql.vendor.core;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;
import io.vertigo.database.impl.sql.Resources;
import io.vertigo.database.sql.vendor.SqlExceptionHandler;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;

/**
 * Handler abstrait des exceptions SQL qui peuvent survenir dans un service.
 * Cette classe est abstraite et doit être concretisée par une sous-classe.
 * Dans le cas d'une contrainte d'intégrité référentiel ou d'unicité, le message de
 * l'erreur utilisateur doit être specialisé en l'ajoutant comme ressource dans
 * le RessourceManager en utilisant le nom de la contrainte comme clef.
 *
 * @author npiedeloup, evernat
 */
public abstract class AbstractSqlExceptionHandler implements SqlExceptionHandler {
	private static final int ERROR_CODE_LENGTH = 6;
	private static final Logger LOGGER = LogManager.getLogger(AbstractSqlExceptionHandler.class);

	/**
	 * Crée une nouvelle instance de AbstractSqlExceptionHandler.
	 */
	public AbstractSqlExceptionHandler() {
		LOGGER.info("Use {} as SqlExceptionHandler", this.getClass().getName());
	}

	/**
	 * @param msg Message base de données
	 * @return Code de la contrainte
	 */
	protected abstract String extractConstraintName(final String msg);

	/**
	 * @param sqle Exception base de données
	 */
	protected final VUserException handleTooLargeValueSqlException(final SQLException sqle) {
		final MessageKey key = Resources.DYNAMO_SQL_CONSTRAINT_TOO_BIG_VALUE;
		LOGGER.warn(MessageText.of(key).getDisplay(), sqle);
		//On se contente de logger l'exception cause mais on ne la lie pas à l'erreur utilisateur.
		return new VUserException(key);
	}

	/**
	 * @param sqle SQLException launch by SQL (often PLSQL application specific exception with &lt;text&gt; tag)
	 */
	protected final VUserException handleUserSQLException(final SQLException sqle) {
		String msg = sqle.getMessage();
		final int i1 = msg.indexOf("<text>");
		final int i2 = msg.indexOf("</text>", i1);

		if (i1 > -1 && i2 > -1) {
			msg = msg.substring(i1 + ERROR_CODE_LENGTH, i2);
		}
		//On se contente de logger l'exception cause mais on ne la lie pas à l'erreur utilisateur.
		return new VUserException(msg);
	}

	/**
	 * Traite l'exception lié à la contrainte d'intégrité.
	 * Et lance une KUserException avec le message par défaut passé en paramètre et une MessageKey basé sur le nom de la contrainte.
	 * @param sqle Exception SQL
	 * @param defaultMsg Message par defaut
	 */
	protected final VUserException handleConstraintSQLException(final SQLException sqle, final MessageKey defaultMsg) {
		final String msg = sqle.getMessage();
		// recherche le nom de la contrainte d'intégrité
		final String constraintName = extractConstraintName(msg);
		Assertion.checkNotNull(constraintName, "Impossible d''extraire le nom de la contrainte : {0}", msg);

		// recherche le message pour l'utilisateur
		//On crée une clé de MessageText dynamiquement sur le nom de la contrainte d'intégrité
		//Ex: CK_PERSON_FULL_NAME_UNIQUE
		final MessageKey constraintKey = new SQLConstraintMessageKey(constraintName);

		//On récupère ici le message externalisé par défaut : Resources.DYNAMO_SQL_CONSTRAINT_IMPOSSIBLE_TO_DELETE ou Resources.DYNAMO_SQL_CONSTRAINT_ALREADY_REGISTRED)
		final String defaultConstraintMsg = MessageText.of(defaultMsg).getDisplay();
		final MessageText userContraintMessageText = MessageText.ofDefaultMsg(defaultConstraintMsg, constraintKey);
		final VUserException constraintException = new VUserException(userContraintMessageText);
		constraintException.initCause(sqle);
		return constraintException;
	}

	/**
	 * @param sqle ForeignConstraintSQLException
	 */
	protected final VUserException handleForeignConstraintSQLException(final SQLException sqle) {
		return handleConstraintSQLException(sqle, Resources.DYNAMO_SQL_CONSTRAINT_IMPOSSIBLE_TO_DELETE);
	}

	/**
	 * @param sqle UniqueConstraintSQLException
	 */
	protected final VUserException handleUniqueConstraintSQLException(final SQLException sqle) {
		return handleConstraintSQLException(sqle, Resources.DYNAMO_SQL_CONSTRAINT_ALREADY_REGISTRED);
	}

	/**
	 * @param sqle OtherSQLException
	 * @param statementInfos the statement
	 */
	protected final RuntimeException handleOtherSQLException(final SQLException sqle, final String statementInfos) {
		final int errCode = sqle.getErrorCode();
		return WrappedException.wrap(sqle, StringUtil.format("[SQL error] {0} : {1}", errCode, statementInfos));
	}

	private static final class SQLConstraintMessageKey implements MessageKey {
		private final String constraintName;
		private static final long serialVersionUID = -3457399434625437700L;

		SQLConstraintMessageKey(final String constraintName) {
			this.constraintName = constraintName;
		}

		/** {@inheritDoc} */
		@Override
		public String name() {
			return constraintName;
		}
	}
}
