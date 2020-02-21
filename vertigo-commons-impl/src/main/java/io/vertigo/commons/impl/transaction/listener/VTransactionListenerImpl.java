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
package io.vertigo.commons.impl.transaction.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the standard implementation.
 *
 * @author pchretien
 */
public final class VTransactionListenerImpl implements VTransactionListener {

	private static final Logger LOGGER = LogManager.getLogger("transaction");

	/** {@inheritDoc} */
	@Override
	public void onStart() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Start transaction");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onFinish(final boolean commitSucceeded, final long elapsedTime) {
		if (LOGGER.isTraceEnabled()) {
			if (commitSucceeded) {
				LOGGER.trace(">>Transaction commit ( {} ms)", elapsedTime);
			} else {
				LOGGER.trace(">>Transaction rollback ( {} ms)", elapsedTime);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void logAfterCommitError(final Throwable th) {
		LOGGER.info("an error occured after commit : " + th.getMessage(), th);
	}
}
