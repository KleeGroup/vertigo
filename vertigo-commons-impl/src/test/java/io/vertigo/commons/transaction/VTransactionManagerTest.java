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
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.data.SampleDataBase;
import io.vertigo.commons.transaction.data.SampleDataBaseConnection;
import io.vertigo.commons.transaction.data.SampleTransactionResource;

/**
 *
 * @author dchallas
 *
 */
public final class VTransactionManagerTest extends AbstractTestCaseJU5 {
	private static int count;

	@Inject
	private SampleServices sampleServices;

	@Inject
	private VTransactionManager transactionManager;
	private SampleDataBase dataBase;

	private static String createData() {
		count++;
		return "data - [" + count + "]" + String.valueOf(System.currentTimeMillis());
	}

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(SampleServices.class)
						.build())
				.build();
	}

	@Override
	protected void doSetUp() {
		dataBase = new SampleDataBase();
	}

	@Override
	protected void doTearDown() {
		Assertions.assertFalse(transactionManager.hasCurrentTransaction(), "transaction must be closed");
	}

	/**
	 * Il n'est pas possible de créer une transaction courante si celle-ci existe déjà.
	 */
	@Test
	public void testFailCreateCurrentTransaction() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			transactionManager.createCurrentTransaction();
		} catch (final IllegalStateException e) {
			Assertions.assertEquals("current transaction already created", e.getMessage());
		}
	}

	/**
	 * Test récupération de la transaction courante.
	 */
	@Test
	public void testGetCurrentTransaction() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(currentTransaction, transactionManager.getCurrentTransaction());
			currentTransaction.rollback();
		}
	}

	/**
	 * Verifier la gestion du commit.
	 */
	@Test
	public void testCommit() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

			// --- modification de la bdd
			final String value = createData();
			connection.setData(value);
			Assertions.assertEquals(value, connection.getData());
			currentTransaction.commit();
			//On vérifie que la bdd est mise à jour.
			Assertions.assertEquals(value, dataBase.getData());
		}
	}

	/**
	 * Verifier la gestion du rollback.
	 */
	@Test
	public void testRollback() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

			// --- modification de la bdd
			final String oldValue = dataBase.getData();
			final String value = createData();
			connection.setData(value);
			Assertions.assertEquals(value, connection.getData());
			currentTransaction.rollback();
			Assertions.assertEquals(oldValue, dataBase.getData());
		}
	}

	/**
	 * Verifier la gestion du commit.
	 * Impossibilité de commiter deux fois.
	 */
	@Test
	public void testCommitCommit() {
		Assertions.assertThrows(Exception.class, () -> {
			try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
				currentTransaction.commit();

				//L'appel au second commit doit déclencher une exception
				currentTransaction.commit();
			}
		});
	}

	/**
	 * Verifier la gestion du commit.
	 * Impossibilité de commiter après un rollback.
	 */
	@Test
	public void testRollbackCommit() {
		Assertions.assertThrows(Exception.class, () -> {
			try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
				currentTransaction.rollback();

				//L'appel au commit après un rollback doit déclencher une exception
				currentTransaction.commit();
			}
		});
	}

	/**
	 * Verifier la gestion du rollback.
	 * Autorisation de rollbacker après un commit.
	 */
	@Test
	public void testCommitRollback() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.commit();
			currentTransaction.rollback();
		}
	}

	/**
	 * Verifier la gestion du rollback.
	 * Autorisation de rollbacker après un rollback.
	 */
	@Test
	public void testRollbackRollback() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.rollback();
			currentTransaction.rollback();
		}
	}

	/**
	 * Création d'une transaction automome à l'intérieur d'une transaction.
	 */
	@Test
	public void testCreateAutonomousTransaction() {
		Assertions.assertFalse(transactionManager.hasCurrentTransaction());
		try (final VTransactionWritable rootTransaction = transactionManager.createCurrentTransaction()) {
			final SampleDataBaseConnection rootConnection = obtainDataBaseConnection(dataBase, "test-memory-1");

			Assertions.assertEquals(rootTransaction, transactionManager.getCurrentTransaction());
			// --- modification de la bdd sur la transaction principale.
			final String rootValue = createData();
			rootConnection.setData(rootValue);
			Assertions.assertEquals(rootValue, rootConnection.getData());

			final String value = createData();
			try (final VTransactionWritable autonomousTransaction = transactionManager.createAutonomousTransaction()) {
				Assertions.assertEquals(autonomousTransaction, transactionManager.getCurrentTransaction());
				final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-2");
				// --- modification de la bdd sur la transaction autonome.
				connection.setData(value);
				Assertions.assertEquals(value, connection.getData());
				autonomousTransaction.commit();

				Assertions.assertNotSame(rootTransaction, autonomousTransaction);
			}
			Assertions.assertEquals(rootTransaction, transactionManager.getCurrentTransaction());

			//On vérifie que la bdd est mise à jour.
			Assertions.assertEquals(value, dataBase.getData());
			Assertions.assertEquals(rootValue, rootConnection.getData());
			rootTransaction.commit();
			//On vérifie que la bdd est mise à jour.
			Assertions.assertEquals(rootValue, dataBase.getData());
		}
		Assertions.assertFalse(transactionManager.hasCurrentTransaction());
	}

	/**
	 * Création d'une transaction automome à l'intérieur d'une transaction.
	 */
	@Test
	public void testCreateAutonomousTransactionForgetToClose() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			try (final VTransactionWritable rootTransaction = transactionManager.createCurrentTransaction()) {
				final SampleDataBaseConnection rootConnection = obtainDataBaseConnection(dataBase, "test-memory-1");
				// --- modification de la bdd sur la transaction principale.
				final String rootValue = createData();
				rootConnection.setData(rootValue);
				Assertions.assertEquals(rootValue, rootConnection.getData());

				try (final VTransactionWritable autonomousTransaction = transactionManager.createAutonomousTransaction()) {
					final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-2");
					// --- modification de la bdd sur la transaction autonome.
					final String value = createData();
					connection.setData(value);
					Assertions.assertEquals(value, connection.getData());
					rootTransaction.commit();
					//commit sur la transaction parent avant d'avoir fermer l'inner transaction lance une exception
				}
			}
		});
	}

	/**
	 * Création d'une transaction automome à l'intérieur d'une transaction.
	 * Quand la transaction n'existe pas.
	 */
	@Test
	public void testFailCreateAutonomousTransaction() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			try (final VTransactionWritable autonomousTransaction = transactionManager.createAutonomousTransaction()) {
				nop(autonomousTransaction);
			}
		});
	}

	//Utilitaire
	private SampleDataBaseConnection obtainDataBaseConnection(final SampleDataBase myDataBase, final String resourceId) {
		// --- resource 1
		final VTransactionResourceId<SampleTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource sampleTransactionResource = new SampleTransactionResource(myDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, sampleTransactionResource);
		return sampleTransactionResource;
	}

	/**
	 * Vérifier la gestion du commit sur une ressource avant la fin de la transaction.
	 */
	@Test
	public void testGetResourcesCommit() {

		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			final VTransactionResourceId<SampleTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "test-memory-1");
			final SampleTransactionResource sampleDataBaseTransactionResource = currentTransaction.getResource(transactionResourceId);

			// --- modification des deux bdd
			final String value1 = createData();
			sampleDataBaseConnection1.setData(value1);
			Assertions.assertEquals(value1, sampleDataBaseConnection1.getData());

			sampleDataBaseTransactionResource.commit();

			// --- test du commit
			currentTransaction.commit();
			Assertions.assertEquals(value1, dataBase.getData());
		}
	}

	/**
	 * Vérifier la gestion du commit sur deux ressources différentes.
	 */
	@Test
	public void testTwoResourcesCommit() {
		//On crée une autre BDD.
		final SampleDataBase secondDataBase = new SampleDataBase();

		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			final SampleDataBaseConnection sampleDataBaseConnection2 = obtainDataBaseConnection(secondDataBase, "test-memory-2");

			// --- modification des deux bdd
			final String value1 = createData();
			sampleDataBaseConnection1.setData(value1);
			Assertions.assertEquals(value1, sampleDataBaseConnection1.getData());

			final String value2 = createData();
			sampleDataBaseConnection2.setData(value2);
			Assertions.assertEquals(value2, sampleDataBaseConnection2.getData());

			// --- test du commit
			currentTransaction.commit();
			Assertions.assertEquals(value1, dataBase.getData());
			Assertions.assertEquals(value2, secondDataBase.getData());
		}
	}

	/**
	 * Vérifier la gestion du rollback sur deux ressources différentes.
	 */
	@Test
	public void testTwoResourcesRollback() {
		//On crée une autre BDD.
		final SampleDataBase secondDataBase = new SampleDataBase();

		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			final SampleDataBaseConnection sampleDataBaseConnection2 = obtainDataBaseConnection(secondDataBase, "test-memory-2");

			final String oldValue1 = dataBase.getData();
			final String oldValue2 = secondDataBase.getData();

			// --- modification des deux bdd
			final String value1 = createData();
			sampleDataBaseConnection1.setData(value1);
			Assertions.assertEquals(value1, sampleDataBaseConnection1.getData());

			final String value2 = createData();
			sampleDataBaseConnection2.setData(value2);
			Assertions.assertEquals(value2, sampleDataBaseConnection2.getData());

			currentTransaction.rollback();
			// --- test du rollback
			Assertions.assertEquals(oldValue1, dataBase.getData());
			Assertions.assertEquals(oldValue2, secondDataBase.getData());
		}
	}

	/**
	 * Vérifier la gestion des erreurs lors d'un commit.
	 * @throws Throwable Exception
	 */
	@Test
	public void testResourcesExceptionInCommit() throws Throwable {
		Assertions.assertThrows(Error.class, () -> {
			final ErronousTransactionResource sampleTransactionResource = new ErronousTransactionResource(new Error("SpecificException on commit"), null, null);
			try {
				try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
					// --- resource 1
					final VTransactionResourceId<VTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Ressource-commit-exception");
					transactionManager.getCurrentTransaction().addResource(transactionResourceId, sampleTransactionResource);
					currentTransaction.commit();
				}
			} catch (final RuntimeException e) {
				throw e.getCause(); //we unwrapp RuntimeException
			} finally {
				Assertions.assertTrue(sampleTransactionResource.commitCalled, "Commit resource must be called");
				Assertions.assertFalse(sampleTransactionResource.rollbackCalled, "Rollback resource should not be called");
				Assertions.assertTrue(sampleTransactionResource.releaseCalled, "Release resource must be called");
			}
		}, "SpecificException on commit");
	}

	/**
	 *  Vérifier la gestion des erreurs lors d'un commit et release.
	 * @throws Throwable Exception
	 */
	@Test
	public void testResourcesErrorInCommitAndRelease() throws Throwable {
		Assertions.assertThrows(Error.class, () -> {
			final ErronousTransactionResource sampleTransactionResource = new ErronousTransactionResource(new Error("SpecificException on commit"), null, new Error("SpecificException on release"));
			try {
				try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
					// --- resource 1
					final VTransactionResourceId<VTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Ressource-commit-exception");
					transactionManager.getCurrentTransaction().addResource(transactionResourceId, sampleTransactionResource);

					currentTransaction.commit();
				}
			} catch (final RuntimeException e) {
				throw e.getCause(); //we unwrapp RuntimeException
			} finally {
				Assertions.assertTrue(sampleTransactionResource.commitCalled, "Commit resource must be called");
				Assertions.assertFalse(sampleTransactionResource.rollbackCalled, "Rollback resource should not be called");
				Assertions.assertTrue(sampleTransactionResource.releaseCalled, "Release resource must be called");
			}
		}, "SpecificException on commit");
	}

	/**
	 *  Vérifier la gestion des erreurs lors d'un commit et release sur deux resources.
	 * @throws Throwable Exception
	 */
	@Test
	public void testTwoResourcesErrorInCommitAndRelease() {
		Assertions.assertThrows(Error.class, () -> {
			final ErronousTransactionResource sampleTransactionResource1 = new ErronousTransactionResource(new Error("SpecificException on commit 1"), null, new Error("SpecificException on release 1"));
			final ErronousTransactionResource sampleTransactionResource2 = new ErronousTransactionResource(new Error("SpecificException on commit 2"), null, new Error("SpecificException on release 2"));
			try {
				try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
					// --- resource 1
					final VTransactionResourceId<VTransactionResource> transactionResource1Id = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Ressource1-commit-exception");
					transactionManager.getCurrentTransaction().addResource(transactionResource1Id, sampleTransactionResource1);

					// --- resource 2
					final VTransactionResourceId<VTransactionResource> transactionResource2Id = new VTransactionResourceId<>(VTransactionResourceId.Priority.NORMAL, "Ressource2-commit-exception");
					transactionManager.getCurrentTransaction().addResource(transactionResource2Id, sampleTransactionResource2);

					currentTransaction.commit();
				}
			} catch (final RuntimeException e) {
				throw e.getCause(); //we unwrapp RuntimeException
			} finally {
				Assertions.assertTrue(sampleTransactionResource1.commitCalled, "Commit resource1 must be called");
				Assertions.assertFalse(sampleTransactionResource1.rollbackCalled, "Rollback resource1 should not be called");
				Assertions.assertTrue(sampleTransactionResource1.releaseCalled, "Release resource1 must be called");

				Assertions.assertFalse(sampleTransactionResource2.commitCalled, "Commit resource2 should not be called");
				Assertions.assertTrue(sampleTransactionResource2.rollbackCalled, "Rollback resource2 must be called");
				Assertions.assertTrue(sampleTransactionResource2.releaseCalled, "Release resource2 must be called");
			}
		}, "SpecificException on commit 1");
	}

	/**
	 * Test @Transactional aspect placed upon all methods of BusinessServices component.
	 */
	@Test
	public void testTransactional() {
		final String value = sampleServices.test();
		sampleServices.check(value);
	}

	private static class ErronousTransactionResource implements VTransactionResource {
		boolean commitCalled = false;
		boolean rollbackCalled = false;
		boolean releaseCalled = false;

		Throwable throwOnCommit = null;
		Throwable throwOnRollback = null;
		Throwable throwOnRelease = null;

		ErronousTransactionResource(final Throwable throwOnCommit, final Throwable throwOnRollback, final Throwable throwOnRelease) {
			this.throwOnCommit = throwOnCommit;
			this.throwOnRollback = throwOnRollback;
			this.throwOnRelease = throwOnRelease;
		}

		@Override
		public void commit() throws Exception {
			commitCalled = true;
			if (throwOnCommit != null) {
				doThrow(throwOnCommit);
			}
		}

		@Override
		public void rollback() throws Exception {
			rollbackCalled = true;
			if (throwOnRollback != null) {
				doThrow(throwOnRollback);
			}
		}

		@Override
		public void release() throws Exception {
			releaseCalled = true;
			if (throwOnRelease != null) {
				doThrow(throwOnRelease);
			}
		}

		private static void doThrow(final Throwable t) throws Exception {
			if (t instanceof Exception) {
				throw (Exception) t;
			}
			throw (Error) t;
		}
	}
}
