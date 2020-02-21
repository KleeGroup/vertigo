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
package io.vertigo.commons.daemon;

/**
 * Some execution stats about registered daemons.
 *
 * @author pchretien
 */
public interface DaemonStat {

	/**
	 * Daemon execution status.
	 */
	enum Status {
		/** Waiting for next execution. */
		pending,
		/** Running. */
		running;
	}

	/**
	 * @return the daemon name
	 */
	String getDaemonName();

	/**
	 * @return the demon period
	 */
	int getDaemonPeriodInSecond();

	/**
	 * @return the number of executions since the daemon started
	 */
	long getCount();

	/**
	 * @return the number of successes since the daemon started
	 */
	long getSuccesses();

	/**
	 * @return the number of failures since the daemon started
	 */
	long getFailures();

	/**
	 * @return the current status
	 */
	Status getStatus();

	/**
	 * @return if last exec was a success
	 */
	boolean isLastExecSuccess();
}
