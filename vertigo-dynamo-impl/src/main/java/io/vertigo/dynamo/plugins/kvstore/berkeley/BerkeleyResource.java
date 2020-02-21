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
package io.vertigo.dynamo.plugins.kvstore.berkeley;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Manages a connection to a berkeley database.
 *
 * @author pchretien
 */
final class BerkeleyResource implements VTransactionResource {
	private final Transaction transaction;

	/***
	 * Constructor.
	 * @param environment Berkeley Environment.
	 */
	BerkeleyResource(final Environment environment) {
		Assertion.checkNotNull(environment);
		//-----
		try {
			transaction = environment.beginTransaction(null, null);
		} catch (final DatabaseException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * @return Berkeley transaction
	 */
	Transaction getBerkeleyTransaction() {
		return transaction;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() throws Exception {
		transaction.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() throws Exception {
		transaction.abort();
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		//
	}
}
