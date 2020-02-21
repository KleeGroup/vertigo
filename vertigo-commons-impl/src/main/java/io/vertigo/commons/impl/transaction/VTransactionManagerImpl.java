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

import io.vertigo.commons.impl.transaction.listener.VTransactionListener;
import io.vertigo.commons.impl.transaction.listener.VTransactionListenerImpl;
import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;

/**
 * Implémentation standard du gestionnaire de transactions.
 *
 * @author  pchretien
 */
public final class VTransactionManagerImpl implements VTransactionManager {
	private final VTransactionListener transactionListener = new VTransactionListenerImpl();

	/** {@inheritDoc} */
	@Override
	public VTransaction getCurrentTransaction() {
		return getCurrentTransactionImpl();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasCurrentTransaction() {
		return VTransactionImpl.hasLocalCurrentTransaction();
	}

	/** {@inheritDoc} */
	@Override
	public VTransactionWritable createCurrentTransaction() {
		//Il faut qu'il n'existe aucune transaction en cours.
		if (hasCurrentTransaction()) {
			throw new IllegalStateException("current transaction already created");
		}
		//On démarre la Transaction à cet endroit précis.
		return new VTransactionImpl(transactionListener);
	}

	/** {@inheritDoc} */
	@Override
	public VTransactionWritable createAutonomousTransaction() {
		final VTransactionImpl currentTransaction = getCurrentTransactionImpl();
		return new VTransactionImpl(currentTransaction);
	}

	/**
	 * Retourne la transaction courante (forcément non null).
	 * @return Transaction courante (la plus basse)
	 */
	private static VTransactionImpl getCurrentTransactionImpl() {
		final VTransactionImpl transaction = VTransactionImpl.getLocalCurrentTransaction();
		Assertion.checkNotNull(transaction, "current transaction not found");
		return transaction.getDeepestTransaction();
	}
}
