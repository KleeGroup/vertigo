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
package io.vertigo.commons.impl.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.core.component.Activeable;
import io.vertigo.lang.Assertion;

/**
 * This class executes the daemons that have been previously registered.
 *
 * @author TINGARGIOLA
 */
final class DaemonExecutor implements Activeable {
	private boolean isActive;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final List<DaemonListener> daemonListeners = new ArrayList<>();

	/**
	* Registers a new daemon.
	* It will be executed after the delay (in milliseconds)
	* and will be periodically executed after the period (in milliseconds)
	*
	* @param daemonDefinition the daemon definition
	* @param daemon Daemon to schedule.
	*/
	void scheduleDaemon(final DaemonDefinition daemonDefinition, final Daemon daemon) {
		Assertion.checkNotNull(daemonDefinition);
		Assertion.checkState(isActive, "Manager must be active to schedule a daemon");
		// -----
		final DaemonListener daemonListener = new DaemonListener(daemonDefinition, daemon.verbose());
		final DaemonTimerTask timerTask = new DaemonTimerTask(daemonListener, daemon);
		daemonListeners.add(daemonListener);
		scheduler.scheduleWithFixedDelay(timerTask, daemonDefinition.getPeriodInSeconds(), daemonDefinition.getPeriodInSeconds(), TimeUnit.SECONDS);
	}

	/**
	 * @return Daemons stats
	 */
	List<DaemonStat> getStats() {
		return daemonListeners
				.stream()
				.map(DaemonListener::getStat)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		isActive = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		scheduler.shutdown();
		isActive = false;
	}
}
