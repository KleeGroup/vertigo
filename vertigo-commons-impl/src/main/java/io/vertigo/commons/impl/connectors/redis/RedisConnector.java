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
package io.vertigo.commons.impl.connectors.redis;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.analytics.health.HealthChecked;
import io.vertigo.commons.analytics.health.HealthMeasure;
import io.vertigo.commons.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.Component;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author pchretien
 */
public final class RedisConnector implements Component, Activeable {
	private static final int CONNECT_TIMEOUT = 2000;
	private final JedisPool jedisPool;

	/**
	 * Constructor.
	 * @param redisHost REDIS server host name
	 * @param redisPort REDIS server port
	 * @param redisDatabase REDIS database index
	 * @param passwordOption password (optional)
	 */
	@Inject
	public RedisConnector(
			@ParamValue("host") final String redisHost,
			@ParamValue("port") final int redisPort,
			@ParamValue("database") final int redisDatabase,
			@ParamValue("password") final Optional<String> passwordOption) {
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkNotNull(passwordOption);
		Assertion.checkArgument(redisDatabase >= 0 && redisDatabase < 16, "there 16 DBs(0 - 15); your index database '{0}' is not inside this range", redisDatabase);
		//-----
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, CONNECT_TIMEOUT, passwordOption.orElse(null), redisDatabase);
		//test
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.ping();
		}
	}

	/**
	 * @return Redis resource
	 */
	public Jedis getResource() {
		return jedisPool.getResource();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		jedisPool.destroy();
	}

	@HealthChecked(name = "ping", feature = "redis")
	public HealthMeasure checkRedisPing() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try (Jedis jedis = getResource()) {
			final String result = jedis.ping();
			healthMeasureBuilder.withGreenStatus(result);
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}

	@HealthChecked(name = "io", feature = "redis")
	public HealthMeasure checkRedisIO() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try (Jedis jedis = getResource()) {
			jedis.set("vertigoHealthCheckIoKey", "vertigoHealthCheckIoValue");
			jedis.get("vertigoHealthCheckIoKey");
			jedis.del("vertigoHealthCheckIoKey");
			healthMeasureBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}

}
