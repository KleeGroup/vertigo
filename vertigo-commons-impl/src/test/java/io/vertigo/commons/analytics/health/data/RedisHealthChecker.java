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
package io.vertigo.commons.analytics.health.data;

import javax.inject.Inject;

import io.vertigo.commons.analytics.health.HealthChecked;
import io.vertigo.commons.analytics.health.HealthMeasure;
import io.vertigo.commons.analytics.health.HealthMeasureBuilder;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.core.component.Component;
import io.vertigo.lang.Assertion;
import redis.clients.jedis.Jedis;

public class RedisHealthChecker implements Component {

	private final RedisConnector redisConnector;

	@Inject
	public RedisHealthChecker(final RedisConnector redisConnector) {
		Assertion.checkNotNull(redisConnector);
		//---
		this.redisConnector = redisConnector;
	}

	@HealthChecked(name = "ping", feature = "redisChecker")
	public HealthMeasure checkRedisPing() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try (Jedis jedis = redisConnector.getResource()) {
			final String result = jedis.ping();
			healthMeasureBuilder.withGreenStatus(result);
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}

}
