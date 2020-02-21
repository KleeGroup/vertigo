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

import java.util.function.Supplier;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.lang.Assertion;

/**
 * Daemon's info.
 *
 * @author TINGARGIOLA
 */
@DefinitionPrefix("Dmn")
public final class DaemonDefinition implements Definition {

	/** Name of the daemon. */
	private final String name;
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
		DefinitionUtil.checkName(name, DaemonDefinition.class);
		Assertion.checkNotNull(daemonSupplier);
		Assertion.checkArgument(periodInSeconds > 0, "period {0} must be > 0", periodInSeconds);
		// -----
		this.name = name;
		this.daemonSupplier = daemonSupplier;
		this.periodInSeconds = periodInSeconds;
	}

	@Override
	public String getName() {
		return name;
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
