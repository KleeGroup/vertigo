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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.lang.Assertion;

final class DaemonListener {
	private static final Logger LOG = LogManager.getLogger(DaemonListener.class);

	private long successes;
	private boolean lastExecSucceed;
	private long failures;
	private DaemonStat.Status status = DaemonStat.Status.pending;
	private final DaemonDefinition daemonDefinition;
	private final boolean verbose;

	public DaemonListener(final DaemonDefinition daemonDefinition, final boolean verbose) {
		Assertion.checkNotNull(daemonDefinition);
		//---
		this.daemonDefinition = daemonDefinition;
		this.verbose = verbose;
	}

	//Les synchronized sont placés de façon à garantir l'intégrité des données
	//On effectue une copie/snapshot des stats de façon à ne pas perturber la suite du fonctionnement.

	synchronized DaemonStat getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonDefinition, successes, failures, status, lastExecSucceed);
	}

	synchronized void onStart() {
		status = DaemonStat.Status.running;
		if (verbose) {
			LOG.info("Start daemon: {}", daemonDefinition.getName());
		}
	}

	synchronized void onFailure(final Exception e) {
		status = DaemonStat.Status.pending;
		failures++;
		lastExecSucceed = false;
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonDefinition.getName(), e);
	}

	synchronized void onSuccess() {
		status = DaemonStat.Status.pending;
		successes++;
		lastExecSucceed = true;
		if (verbose) {
			LOG.info("Execution succeeded on daemon: {}", daemonDefinition.getName());
		}
	}
}
