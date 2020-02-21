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

import io.vertigo.core.component.Component;

@DaemonFakeAspectAnnotation
public class FakeComponent implements Component {

	static final String SIMPLE_DAEMON_NAME = "DmnSimple";
	private int executions = 0;

	public int getExecutionCount() {
		return executions;
	}

	@DaemonScheduled(name = SIMPLE_DAEMON_NAME, periodInSeconds = 2)
	public void execute() {
		executions++;
		if (executions == 1) {
			throw new IllegalStateException("ExpectedException for Test");
		}
	}

}
