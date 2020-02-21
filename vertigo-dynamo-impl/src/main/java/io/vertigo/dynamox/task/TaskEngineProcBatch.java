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
/**
 *
 */
package io.vertigo.dynamox.task;

import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.statement.SqlStatementBuilder;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;

/**
 * @author jmforhan
 */
public final class TaskEngineProcBatch extends AbstractTaskEngineSQL {

	/**
	 * Constructeur.
	 * @param scriptManager scriptManager
	 * @param transactionManager transactionManager
	 * @param storeManager storeManager
	 * @param sqlDataBaseManager sqlDataBaseManager
	 */
	@Inject
	public TaskEngineProcBatch(
			final ScriptManager scriptManager,
			final VTransactionManager transactionManager,
			final StoreManager storeManager,
			final SqlDataBaseManager sqlDataBaseManager) {
		super(scriptManager, transactionManager, storeManager, sqlDataBaseManager);
	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt doExecute(
			final SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException {
		Assertion.checkNotNull(sqlStatement);
		Assertion.checkNotNull(connection);
		//---
		return getDataBaseManager().executeBatch(sqlStatement, connection);
	}

	@Override
	protected void setNamedParameters(final SqlStatementBuilder sqlStatementBuilder) {
		final List<TaskAttribute> potentialBatchAttributes = getTaskDefinition().getInAttributes()
				.stream()
				.filter(inAttribute -> inAttribute.getDomain().isMultiple())// multiple
				.collect(Collectors.toList());

		Assertion.checkState(potentialBatchAttributes.size() == 1, "For batch a single List param is required");
		final TaskAttribute listAttribute = potentialBatchAttributes.get(0);

		final List<TaskAttribute> otherAttributes = getTaskDefinition().getInAttributes()
				.stream()
				.filter(inAttribute -> !inAttribute.getDomain().isMultiple())// not multiple
				.collect(Collectors.toList());
		//---
		final List<?> list = getValue(listAttribute.getName());
		list.forEach(object -> {
			// we bind the parameter of the batch
			sqlStatementBuilder.bind(listAttribute.getName(), listAttribute.getDomain().getJavaClass(), object);
			// we add all the "constant" parameters
			otherAttributes.forEach(
					otherAttribute -> sqlStatementBuilder.bind(otherAttribute.getName(), otherAttribute.getDomain().getJavaClass(), getValue(otherAttribute.getName())));
			sqlStatementBuilder.nextLine();
		});
	}

}
