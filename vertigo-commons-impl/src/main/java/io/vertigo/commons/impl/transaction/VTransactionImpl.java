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
package io.vertigo.commons.impl.transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.commons.impl.transaction.listener.VTransactionListener;
import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionAfterCompletionFunction;
import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.commons.transaction.VTransactionResourceId;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation standard d'une transaction Dynamo.
 *
 * @author  pchretien
 */
final class VTransactionImpl implements VTransactionWritable {

	private enum State {
		ALIVE,
		CLOSED;

		/**
		 * A transaction is alive or closed.
		 * @return if the transaction is closed.
		 */
		boolean isClosed() {
			return this == State.CLOSED;
		}

		/**
		 * Checks if the transaction is alive
		 */
		void assertIsAlive() {
			if (this != State.ALIVE) {
				throw new IllegalStateException("The transaction must be alive.");
			}
		}

		/**
		 * Checks if the transaction is closed
		 */
		void assertIsClosed() {
			if (this != State.CLOSED) {
				throw new IllegalStateException("The transaction must be closed");
			}
		}
	}

	/**
	 * The current transaction is bound to the current thread.
	 */
	private static final ThreadLocal<VTransactionImpl> CURRENT_THREAD_LOCAL_TRANSACTION = new ThreadLocal<>();

	/**
	 * At the start the current transaction is alive (not closed).
	 */
	private State state = State.ALIVE;
	private final VTransactionListener transactionListener;
	/**
	 * Map des autres ressources de la transaction.
	 */
	private final Map<VTransactionResourceId<?>, VTransactionResource> resources = new LinkedHashMap<>();

	private final List<Runnable> beforeCommitFunctions = new ArrayList<>();
	private final List<VTransactionAfterCompletionFunction> afterCompletionFunctions = new ArrayList<>();

	/**
	 * Transaction parente dans le cadre d'une transaction imbriquée.
	 * Nullable.
	 */
	private final VTransactionImpl parentTransaction;

	/**
	 * Inner transaction.
	 * Nullable.
	 */
	private VTransactionImpl innerTransaction;
	/**
	 * Start of the transaction
	 */
	private final long start = System.currentTimeMillis();

	//==========================================================================
	//=========================== CONSTUCTEUR ==================================
	//==========================================================================
	/**
	 * Constructor.
	 *
	 * @param transactionListener the listener of the event fired during the execution of the tranasction
	 */
	VTransactionImpl(final VTransactionListener transactionListener) {
		Assertion.checkNotNull(transactionListener);
		//-----
		parentTransaction = null;
		this.transactionListener = transactionListener;
		//We notify the start of the transaction
		transactionListener.onStart();
		CURRENT_THREAD_LOCAL_TRANSACTION.set(this);
	}

	/**
	 * Constructor of an inner transaction.
	 * @param parentTransaction the parent transaction
	 */
	VTransactionImpl(final VTransactionImpl parentTransaction) {
		Assertion.checkNotNull(parentTransaction);
		//-----
		this.parentTransaction = parentTransaction;
		parentTransaction.addInnerTransaction(this);
		transactionListener = parentTransaction.transactionListener;
		//We notify the start of the transaction
		transactionListener.onStart();
	}

	//==========================================================================
	//=========================== API ==========================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public <R extends VTransactionResource> R getResource(final VTransactionResourceId<R> transactionResourceId) {
		state.assertIsAlive();
		Assertion.checkNotNull(transactionResourceId);
		//-----
		return (R) resources.get(transactionResourceId);
	}

	/**
	 * Retourne la transaction imbriquée de plus bas étage ou elle même si aucune transaction imbriquée.
	 * @return Transaction de plus bas niveau (elle même si il n'y a pas de transaction imbriquée)
	 */
	VTransactionImpl getDeepestTransaction() {
		return innerTransaction == null ? this : innerTransaction.getDeepestTransaction();
	}

	/**
	 * Adds an inner transaction .
	 * Checks the status of the inner transaction.
	 *
	 * the inner transaction must be alive.
	 *
	 * @param newInnerTransaction the inner transaction to add
	 */
	private void addInnerTransaction(final VTransactionImpl newInnerTransaction) {
		Assertion.checkState(innerTransaction == null, "the current transaction has already an inner transaction");
		Assertion.checkNotNull(newInnerTransaction);
		newInnerTransaction.state.assertIsAlive();
		//-----
		innerTransaction = newInnerTransaction;
	}

	/**
	 * Removes the inner transaction.
	 * Checks the state of the inner transaction.
	 *
	 * The inner transaction must be closed.
	 */
	private void removeInnerTransaction() {
		Assertion.checkNotNull(innerTransaction, "The current transaction doesn't have any inner transaction");
		innerTransaction.state.assertIsClosed();
		//-----
		innerTransaction = null;
	}

	/** {@inheritDoc} */
	@Override
	public <R extends VTransactionResource> void addResource(final VTransactionResourceId<R> id, final R resource) {
		state.assertIsAlive();
		Assertion.checkNotNull(resource);
		Assertion.checkNotNull(id);
		//-----
		final Object o = resources.put(id, resource);
		Assertion.checkState(o == null, "Ressource déjà enregistrée");
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		state.assertIsAlive();
		// There must no more inner transaction.
		if (innerTransaction != null) {
			throw new IllegalStateException("The inner transaction must be closed(Commit or rollback) before the parent transaction");
		}

		// In case of exception, we rethrow it. The transaction sould be rollback.
		doBeforeCommit();

		//-----
		final Throwable throwable = this.doEnd(false);
		if (throwable != null) {
			throw WrappedException.wrap(throwable, "Transaction");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		final Throwable throwable = doRollback();
		if (throwable != null) {
			throw WrappedException.wrap(throwable, "Transaction");
		}
	}

	private void doBeforeCommit() {
		for (final Runnable function : beforeCommitFunctions) {
			function.run();
		}
	}

	/**
	 * Rollback et retourne l'erreur sans la lancer (throw)
	 * @return Erreur de rollback (null la plupart du temps)
	 */
	private Throwable doRollback() {
		if (state.isClosed()) {
			//If the transaction is already closed then we do nothing
			return null;
		}
		//If the transaction is not closed the the transaction is ended.
		//-----
		Throwable throwable = this.doEnd(true);

		if (innerTransaction != null) {
			//Si il existe une transaction imbriquée non terminée
			//alors on note qu'il existe une erreur et on la rollback
			innerTransaction.doRollback();
			throwable = new IllegalStateException("La transaction imbriquée doit être terminée(Commit ou Rollback) avant la transaction parente");
		}
		return throwable;
	}

	/**
	 * End the transaction.
	 * If an error occures, then the best exception is thrown. (the first exception caught  during the finalization of the resources)
	 *
	 * @param rollback if rollback (commit else).
	 * @return the exception to throw.
	 */
	private Throwable doEnd(final boolean rollback) {
		//We change the current status of the transaction and force it to closed.
		state = State.CLOSED;

		Throwable firstThrowable = null;
		if (!resources.isEmpty()) {
			//If there is some resources
			firstThrowable = doEndResources(rollback);
		}

		if (parentTransaction != null) {
			//Lors de la clôture d'une transaction imbriquée,
			//on la supprime de la transaction parente.
			parentTransaction.removeInnerTransaction();
		}

		final boolean commitSucceeded = !rollback && firstThrowable == null;
		//afterCommit must not throws exceptions
		doAfterCompletion(commitSucceeded);

		//Fin de la transaction, si firstThrowable!=null alors on a rollbacké tout ou partie des resources
		transactionListener.onFinish(!commitSucceeded, System.currentTimeMillis() - start);
		return firstThrowable;
	}

	private void doAfterCompletion(final boolean commitSucceeded) {
		for (final VTransactionAfterCompletionFunction function : afterCompletionFunctions) {
			try {
				function.afterCompletion(commitSucceeded);
			} catch (final Throwable th) {
				transactionListener.logAfterCommitError(th);
				//we don't rethrow this exception, main resource was finished, we should continue to proceed afterCompletion functions
			}
		}
	}

	private Throwable doEndResources(final boolean rollback) {
		Throwable firstThrowable = null;
		boolean shouldRollback = rollback;
		//On traite les ressources par ordre de priorité
		for (final VTransactionResourceId<?> id : getOrderedListByPriority()) {
			final VTransactionResource ktr = resources.remove(id);
			//On termine toutes les resources utilisées en les otant de la map.
			Assertion.checkNotNull(ktr);
			final Throwable throwable = doEnd(ktr, shouldRollback);
			if (throwable != null) {
				shouldRollback = true;
				if (firstThrowable == null) {
					firstThrowable = throwable;
				} else {
					firstThrowable.addSuppressed(throwable);
				}
			}
		}
		return firstThrowable;
	}

	//=========================================================================
	//=============TRI de la liste des id de ressouces par priorité============
	//=========================================================================
	private List<VTransactionResourceId<?>> getOrderedListByPriority() {
		//On termine les ressources dans l'ordre DEFAULT, A, B...F
		final List<VTransactionResourceId<?>> list = new ArrayList<>(resources.size());

		populate(list, VTransactionResourceId.Priority.TOP);
		populate(list, VTransactionResourceId.Priority.NORMAL);
		return list;
	}

	private void populate(final List<VTransactionResourceId<?>> list, final VTransactionResourceId.Priority priority) {
		// Ajout des ressources ayant une ceraine priorité à la liste.
		for (final VTransactionResourceId<?> id : resources.keySet()) {
			if (id.getPriority().equals(priority)) {
				list.add(id);
			}
		}
	}

	//=========================================================================

	/**
	 * Termine la transaction pour une ressource.
	 * La première exception (considérée comme la plus grave est retournée).
	 * @param resource Ressource transactionnelle.
	 * @param rollback Si vrai annule, sinon valide.
	 * @return Exception à lancer.
	 */
	private static Throwable doEnd(final VTransactionResource resource, final boolean rollback) {
		Assertion.checkNotNull(resource);
		//-----
		Throwable throwable = null;
		//autoCloseableResource is use to call release() in a finally/suppressedException block
		try (AutoCloseableResource autoCloseableResource = new AutoCloseableResource(resource)) {
			if (rollback) {
				autoCloseableResource.rollback();
			} else {
				if (resource instanceof VTransaction) {
					//Si la ressource est elle même une transaction, elle ne doit pas etre commitée de cette facon implicite
					autoCloseableResource.rollback();
					throw new IllegalStateException("La transaction incluse dans la transaction courante n'a pas été commité correctement");
				}
				autoCloseableResource.commit();
			}
		} catch (final Throwable t) {
			//we catch Throwable in order to handle all ressources even if one of them throw an error
			throwable = t;
		}
		return throwable;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		try {
			rollback();
		} finally {
			final boolean isAutonomous = parentTransaction != null;
			if (!isAutonomous) {
				//At the end of the root transaction then the current transaction is unbound. (removed from the thread)
				CURRENT_THREAD_LOCAL_TRANSACTION.remove();
			}
		}
	}

	//==========================================================================
	//=========================PRIVATE==========================================
	//==========================================================================

	private static class AutoCloseableResource implements AutoCloseable {
		private final VTransactionResource innerResource;

		AutoCloseableResource(final VTransactionResource innerResource) {
			this.innerResource = innerResource;
		}

		void commit() throws Exception {
			innerResource.commit();
		}

		void rollback() throws Exception {
			innerResource.rollback();
		}

		@Override
		public void close() throws Exception {
			innerResource.release();
		}
	}

	static boolean hasLocalCurrentTransaction() {
		return getLocalCurrentTransaction() != null;
	}

	/**
	 * Retourne la transaction courante de plus haut niveau.
	 * - jamais closed
	 * - peut être null
	 * @return VTransaction
	 */
	static VTransactionImpl getLocalCurrentTransaction() {
		VTransactionImpl transaction = CURRENT_THREAD_LOCAL_TRANSACTION.get();
		//Si la transaction courante est finie on ne la retourne pas.
		if (transaction != null && transaction.state.isClosed()) {
			transaction = null;
		}
		return transaction;
	}

	/** {@inheritDoc} */
	@Override
	public void addBeforeCommit(final Runnable function) {
		Assertion.checkNotNull(function);
		//-----
		beforeCommitFunctions.add(function);
	}

	/** {@inheritDoc} */
	@Override
	public void addAfterCompletion(final VTransactionAfterCompletionFunction function) {
		Assertion.checkNotNull(function);
		//-----
		afterCompletionFunctions.add(function);
	}
}
