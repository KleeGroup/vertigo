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
package io.vertigo.core.daemon.definitions;

import java.util.function.Supplier;

import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * Daemon's info.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
@DefinitionPrefix(DaemonDefinition.PREFIX)
public final class DaemonDefinition extends AbstractDefinition {
	public static final String PREFIX = "Dmn";

	private final int periodInSeconds;
	private final Supplier<Daemon> daemonSupplier;

	/**
	 * Constructor.
	 *
	 * @param name the daemon Name
	 * @param daemonSupplier the daemon supplier.
	 * @param periodInSeconds daemon execution period.
	 */
	public DaemonDefinition(final String name, final Supplier<Daemon> daemonSupplier, final int periodInSeconds) {
		super(name);
		//---
		Assertion.check()
				.isNotNull(daemonSupplier)
				.isTrue(periodInSeconds > 0, "period {0} must be > 0", periodInSeconds);
		// -----
		this.daemonSupplier = daemonSupplier;
		this.periodInSeconds = periodInSeconds;
	}

	/**
	 * Give the value of periodInSeconds.
	 *
	 * @return PeriodInSeconds.
	 */
	public int getPeriodInSeconds() {
		return periodInSeconds;
	}

	public Supplier<Daemon> getDaemonSupplier() {
		return daemonSupplier;
	}
}
