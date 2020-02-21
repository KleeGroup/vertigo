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
package io.vertigo.commons.transaction.data;

import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.lang.Assertion;

/**
 *
 * @author dchallas
 *
 */
public final class SampleTransactionResource implements VTransactionResource, SampleDataBaseConnection {
	private final SampleDataBase sampleDataBase;
	private String dataUpdated;
	private boolean isUpdated;

	private enum State {
		Started, Closed
	}

	private State state = State.Started;

	public SampleTransactionResource(final SampleDataBase sampleDataBase) {
		Assertion.checkNotNull(sampleDataBase);
		//-----
		this.sampleDataBase = sampleDataBase;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		check();
		if (isUpdated) {
			sampleDataBase.setData(dataUpdated);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		check();
		dataUpdated = null;
		isUpdated = false;
		state = State.Closed;
	}

	private void check() {
		Assertion.checkArgument(state == State.Started, "This resource is already closed.");
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		check();
		//Pas de mise à jour
	}

	@Override
	public void setData(final String newdata) {
		check();
		this.isUpdated = true;
		this.dataUpdated = newdata;
	}

	@Override
	public String getData() {
		check();
		return isUpdated ? dataUpdated : sampleDataBase.getData();
	}
}
