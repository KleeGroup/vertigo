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
package io.vertigo.commons.transaction;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import io.vertigo.commons.transaction.data.SampleDataBase;
import io.vertigo.commons.transaction.data.SampleDataBaseConnection;
import io.vertigo.commons.transaction.data.SampleTransactionResource;
import io.vertigo.core.component.Component;

public class SampleServices implements Component {
	private static int count;

	@Inject
	private VTransactionManager transactionManager;
	private final SampleDataBase dataBase = new SampleDataBase();

	private SampleDataBaseConnection obtainDataBaseConnection(final SampleDataBase sampleDataBase, final String resourceId) {
		// --- resource 1
		final VTransactionResourceId<SampleTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource transactionResourceMock = new SampleTransactionResource(sampleDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResourceMock);
		return transactionResourceMock;
	}

	@Transactional
	public String test() {
		final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

		// --- modification de la bdd
		final String value = createNewData();
		connection.setData(value);
		Assertions.assertEquals(value, connection.getData());
		return value;
	}

	public void check(final String value) {
		//On vérifie que la bdd est mise à jour.
		Assertions.assertEquals(value, dataBase.getData());
	}

	private static String createNewData() {
		count++;
		return "data - [" + count + "]" + String.valueOf(System.currentTimeMillis());
	}

}
