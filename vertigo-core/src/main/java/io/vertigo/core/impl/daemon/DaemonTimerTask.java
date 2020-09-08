/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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

import java.lang.reflect.Field;

import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.lang.Assertion;

/**
 * @author mlaroche, pchretien, npiedeloup
 */
final class DaemonTimerTask implements Runnable {
	private final Daemon daemon;
	private final DaemonListener daemonListener;

	DaemonTimerTask(final DaemonListener daemonListener, final Daemon daemon) {
		Assertion.check()
				.isNotNull(daemonListener)
				.isNotNull(daemon);
		//---
		this.daemon = daemon;
		this.daemonListener = daemonListener;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try {//try catch needed to ensure execution aren't suppressed
			daemonListener.onStart();
			//---
			daemon.run();
			//---
			daemonListener.onSuccess();
		} catch (final Exception e) {
			daemonListener.onFailure(e);
		} finally {
			clearAllThreadLocals();
		}
	}

	private static void clearAllThreadLocals() {
		try {
			final Field threadLocals = Thread.class.getDeclaredField("threadLocals");
			threadLocals.setAccessible(true);
			threadLocals.set(Thread.currentThread(), null);
		} catch (final Exception e) {
			throw new AssertionError(e);
		}
	}
}
