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
package io.vertigo.core.impl.daemon;

import io.vertigo.core.daemon.DaemonDefinition;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.lang.Assertion;

/**
 * Snapshot of execution's stats.
 * A snaphot is prefereed to a real time view to avoid complexity generated by "synchronized"
 * @author pchretien
 */
final class DaemonStatImpl implements DaemonStat {
	private final DaemonDefinition daemonDefinition;
	private final DaemonStat.Status status;
	private final long sucesses;
	private final long failures;
	private final boolean lastExecSuccess;

	/**
	 * Constructor.
	 * @param daemonDefinition the daemon definition
	 * @param successes Nb success
	 * @param failures Nb failure
	 * @param status Current status
	 * @param lastExecSuccess if last exec was a success
	 */
	DaemonStatImpl(
			final DaemonDefinition daemonDefinition,
			final long successes,
			final long failures,
			final DaemonStat.Status status,
			final boolean lastExecSuccess) {
		Assertion.check()
				.isNotNull(daemonDefinition)
				.isNotNull(status);
		//----
		this.daemonDefinition = daemonDefinition;
		this.failures = failures;
		sucesses = successes;
		this.status = status;
		this.lastExecSuccess = lastExecSuccess;
	}

	/** {@inheritDoc} */
	@Override
	public String getDaemonName() {
		return daemonDefinition.getName();
	}

	/** {@inheritDoc} */
	@Override
	public int getDaemonPeriodInSecond() {
		return daemonDefinition.getPeriodInSeconds();
	}

	/** {@inheritDoc} */
	@Override
	public long getCount() {
		return sucesses + failures;
	}

	/** {@inheritDoc} */
	@Override
	public long getSuccesses() {
		return sucesses;
	}

	/** {@inheritDoc} */
	@Override
	public long getFailures() {
		return failures;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLastExecSuccess() {
		return lastExecSuccess;
	}

	/** {@inheritDoc} */
	@Override
	public Status getStatus() {
		return status;
	}

}
