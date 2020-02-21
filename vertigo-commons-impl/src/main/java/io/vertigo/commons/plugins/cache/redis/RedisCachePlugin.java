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
package io.vertigo.commons.plugins.cache.redis;

import java.io.Serializable;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.lang.Assertion;
import redis.clients.jedis.Jedis;

/**
 * RedisCache plugin
 *
 * @author pchretien, dszniten
 */
public class RedisCachePlugin implements CachePlugin {

	private final CodecManager codecManager;
	private final RedisConnector redisConnector;

	private static final String VERTIGO_CACHE = "vertigo:cache";
	private static final String DELETE_KEYS_ON_PATTERN_SCRIPT = "local keys = redis.call('keys', '%s') for i,k in ipairs(keys) do local res = redis.call('del', k) end";

	/**
	 * Constructor.
	 * @param codecManager  the codecManager
	 * @param redisConnector the redis connector
	 */
	@Inject
	public RedisCachePlugin(
			final CodecManager codecManager,
			final RedisConnector redisConnector) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkNotNull(redisConnector);
		//-----
		this.codecManager = codecManager;
		this.redisConnector = redisConnector;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.checkNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key);
		Assertion.checkState(!(value instanceof byte[]), "CachePlugin can't cache byte[] values");
		Assertion.checkArgument(value instanceof Serializable,
				"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
				context, key, value.getClass().getSimpleName());
		//---
		final String redisKey = buildRedisKey(context, key);

		// redisValue = Base64 encoding of the serialized value
		final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
		final String redisValue = codecManager.getBase64Codec().encode(serializedObject);

		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.setex(redisKey, getCacheDefinition(context).getTimeToLiveSeconds(), redisValue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		final String redisValue;

		try (final Jedis jedis = redisConnector.getResource()) {
			redisValue = jedis.get(redisKey);
		}

		if (redisValue == null) {
			return null;
		}
		final byte[] serializedObject = codecManager.getBase64Codec().decode(redisValue);
		return codecManager.getCompressedSerializationCodec().decode(serializedObject);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.del(redisKey) > 0;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		final String pattern = buildPatternFromContext(context);
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, pattern));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, VERTIGO_CACHE + ":*"));
		}

	}

	private static CacheDefinition getCacheDefinition(final String cacheName) {
		return Home.getApp().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildRedisKey(final String context, final Serializable key) {
		Assertion.checkArgNotEmpty(context);
		Assertion.checkNotNull(key);
		//---
		return VERTIGO_CACHE + ":" + context + ":" + keyToString(key);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildPatternFromContext(final String context) {
		Assertion.checkArgNotEmpty(context);
		//---
		return VERTIGO_CACHE + ":" + context + ":*";
	}

	/*
	 * Converts a key into a String.
	 * An empty key is considered as null.
	 */
	private static String keyToString(final Serializable key) {
		Assertion.checkNotNull(key);
		//---
		if (key instanceof String) {
			Assertion.checkArgNotEmpty((String) key, "a key cannot be an empty string");
			//--
			return ((String) key).trim();
		} else if (key instanceof Integer) {
			return key.toString();
		} else if (key instanceof Long) {
			return key.toString();
		}
		throw new IllegalArgumentException(key.toString() + " is not supported as a key type");
	}
}
