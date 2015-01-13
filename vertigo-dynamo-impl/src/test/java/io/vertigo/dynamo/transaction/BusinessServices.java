package io.vertigo.dynamo.transaction;

import io.vertigo.dynamo.transaction.data.SampleDataBase;
import io.vertigo.dynamo.transaction.data.SampleDataBaseConection;
import io.vertigo.dynamo.transaction.data.SampleTransactionResource;
import io.vertigo.lang.Component;

import javax.inject.Inject;

import org.junit.Assert;

public class BusinessServices implements Component {
	private static int count;

	@Inject
	private KTransactionManager transactionManager;
	private final SampleDataBase dataBase = new SampleDataBase();

	private SampleDataBaseConection obtainDataBaseConnection(final SampleDataBase sampleDataBase, final String resourceId) {
		// --- resource 1
		final KTransactionResourceId<SampleTransactionResource> transactionResourceId = new KTransactionResourceId<>(KTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource transactionResourceMock = new SampleTransactionResource(sampleDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResourceMock);
		return transactionResourceMock;
	}

	@Transactional
	public String test() {
		final SampleDataBaseConection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

		// --- modification de la bdd
		final String value = createNewData();
		connection.setData(value);
		Assert.assertEquals(value, connection.getData());
		return value;
	}

	public void check(final String value) {
		//On vérifie que la bdd est mise à jour.
		Assert.assertEquals(value, dataBase.getData());
	}

	private static String createNewData() {
		count++;
		return "data - [" + count + "]" + String.valueOf(System.currentTimeMillis());
	}

}
