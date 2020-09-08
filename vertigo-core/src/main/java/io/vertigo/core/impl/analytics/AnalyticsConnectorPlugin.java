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
package io.vertigo.core.impl.analytics;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.node.component.Plugin;

/**
 * Connecteur des process.
 * Les messages sont composes des Processus et envoyes ; un consommateur les traitera.
 *
 * @author pchretien, npiedeloup
 * @version $Id: NetPlugin.java,v 1.1 2012/03/22 18:20:57 pchretien Exp $
 */
public interface AnalyticsConnectorPlugin extends Plugin {
	/**
	 * Adds a process to a connector which acts as a consumer.
	 * @param process the process
	 */
	void add(AProcess process);

	/**
	 * Adds a metric to a connector which acts as a consumer.
	 * @param metric the metric
	 */
	void add(Metric metric);

	/**
	 * Adds a healthCheck to a connector which acts as a consumer.
	 * @param healthCheck the healthCheck
	 */
	void add(HealthCheck healthCheck);
}
