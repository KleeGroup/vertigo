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
package io.vertigo.commons.plugins.cache.ehcache;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.ehcache.Cache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;

import io.vertigo.app.Home;
import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.component.Activeable;
import io.vertigo.lang.Assertion;

/**
 * Implémentation EHCache du CacheManager.
 *
 * @author pchretien, npiedeloup
 */
public final class EhCachePlugin implements Activeable, CachePlugin {
	private final org.ehcache.CacheManager manager;
	private final CodecManager codecManager;

	/**
	 * Constructor.
	 * @param codecManager CodecManager
	 */
	@Inject
	public EhCachePlugin(final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		manager = CacheManagerBuilder.newCacheManagerBuilder().build();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		manager.init();
		registerCaches();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		manager.close();
	}

	private void registerCaches() {
		Home.getApp().getDefinitionSpace()
				.getAll(CacheDefinition.class).stream()
				.forEach(this::registerCache);
	}

	private void registerCache(final CacheDefinition cacheDefinition) {
		final boolean overflowToDisk = cacheDefinition.shouldSerializeElements(); //don't overflow
		final ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
				.heap(cacheDefinition.getMaxElementsInMemory(), EntryUnit.ENTRIES);
		if (overflowToDisk) {
			resourcePoolsBuilder.disk(300, MemoryUnit.MB, true);
		}

		final CacheConfiguration<Serializable, Object> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Serializable.class, Object.class,
				resourcePoolsBuilder.build())
				.withExpiry(new ExpiryPolicy<Serializable, Object>() {

					@Override
					public java.time.Duration getExpiryForCreation(final Serializable key, final Object value) {
						return Duration.of(cacheDefinition.getTimeToLiveSeconds(), ChronoUnit.SECONDS); //time out after creation
					}

					@Override
					public java.time.Duration getExpiryForAccess(final Serializable key, final Supplier<? extends Object> value) {
						return Duration.of(cacheDefinition.getTimeToIdleSeconds(), ChronoUnit.SECONDS); //time out after an access
					}

					@Override
					public java.time.Duration getExpiryForUpdate(final Serializable key, final Supplier<? extends Object> oldValue, final Object newValue) {
						return null; // Keeping the existing expiry
					}
				})
				.build();
		manager.createCache(cacheDefinition.getName(), cacheConfiguration);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String cacheName, final Serializable key, final Object value) {
		Assertion.checkNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", cacheName, key);
		Assertion.checkState(!(value instanceof byte[]), "CachePlugin can't cache byte[] values");
		//-----
		//On regarde la conf du cache pour vérifier s'il on serialize/clone les éléments ou non.
		if (getCacheDefinition(cacheName).shouldSerializeElements()) {
			Assertion.checkArgument(value instanceof Serializable,
					"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
					cacheName, key, value.getClass().getSimpleName());
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			//La sérialisation est équivalente à un deep Clone.
			putEH(cacheName, key, serializedObject);
		} else {
			putEH(cacheName, key, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final Object cachedObject = getEH(context, key);
		//on ne connait pas l'état Modifiable ou non de l'objet, on se base sur son type.
		if (cachedObject instanceof byte[]) {
			final byte[] serializedObject = (byte[]) cachedObject;
			return codecManager.getCompressedSerializationCodec().decode(serializedObject);
		}
		return cachedObject;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		getEHCache(context).remove(key);
		return getEHCache(context).get(key) == null;
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		Home.getApp().getDefinitionSpace()
				.getAll(CacheDefinition.class).stream()
				.forEach(cacheDefinition -> {
					final Cache<?, ?> cache = manager.getCache(cacheDefinition.getName(), Serializable.class, Object.class);
					if (cache != null) { //we maximized clear command, cache must exists
						cache.clear();
					}
				});
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		getEHCache(context).clear();
	}

	private void putEH(final String context, final Serializable key, final Object value) {
		getEHCache(context).put(key, value);
	}

	private Object getEH(final String context, final Serializable key) {
		return getEHCache(context).get(key);
	}

	private Cache<Serializable, Object> getEHCache(final String context) {
		final Cache<Serializable, Object> ehCache = manager.getCache(context, Serializable.class, Object.class);
		Assertion.checkNotNull(ehCache, "Cache {0} are not yet registered. Add it into a file ehcache.xml and put it into the WEB-INF directory of your webapp.", context);
		return ehCache;
	}

	private static CacheDefinition getCacheDefinition(final String cacheName) {
		return Home.getApp().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}
}
