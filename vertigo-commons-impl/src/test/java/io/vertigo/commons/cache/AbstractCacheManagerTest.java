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
package io.vertigo.commons.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.lang.Assertion;

/**
 *
 * @author dchallas
 */
public abstract class AbstractCacheManagerTest extends AbstractTestCaseJU5 {
	private static final String KEY = "ma clé";
	private static final String CONTEXT_EDITABLE = TestCacheDefinitionProvider.CONTEXT_EDITABLE;
	private static final String CONTEXT_READONLY = TestCacheDefinitionProvider.CONTEXT_READONLY;

	private final int maxNbRow;

	@Inject
	private CacheManager cacheManager;

	public AbstractCacheManagerTest() {
		maxNbRow = 10000;
	}

	public AbstractCacheManagerTest(final int maxNbRow) {
		this.maxNbRow = maxNbRow;
	}

	/**
	 *
	 * @throws Exception manager null
	 */
	@Test
	public void testNotNull() throws Exception {
		Assertion.checkNotNull(cacheManager);
	}

	/**
	 * Test sur un element.
	 */
	@Test
	public void testPut1() {
		final Serializable value = new Element();

		cacheManager.put(CONTEXT_EDITABLE, KEY, value);
		final Object retrieve = cacheManager.get(CONTEXT_EDITABLE, KEY);
		//On vérifie qu'il s'agit du même objet.
		assertEquals(value, retrieve);
	}

	/**
	 * Supprime un element.
	 */
	@Test
	public void testRemove1() {
		testPut1();
		// vérification de suppression d'un element
		cacheManager.remove(CONTEXT_EDITABLE, KEY);
		assertNull(cacheManager.get(CONTEXT_EDITABLE, KEY));
	}

	/**
	 * vide complètement le cache.
	 */
	@Test
	public void testClear() {
		testPut1();
		cacheManager.clear(CONTEXT_EDITABLE);
		assertNull(cacheManager.get(CONTEXT_EDITABLE, KEY));
	}

	/**
	 * vide complètement tous les caches.
	 */
	@Test
	public void testClearAll() {
		testPut1();
		cacheManager.clearAll();
		assertNull(cacheManager.get(CONTEXT_EDITABLE, KEY));
	}

	/**
	 * Test sur maxNbRows elements.
	 */
	@Test
	public void testPutMass() {
		final int nbRow = maxNbRow;
		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			final Serializable value = new Element();
			cacheManager.put(CONTEXT_EDITABLE, key, value);
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = maxNbRow / 2; i < maxNbRow * 0.55d; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_EDITABLE, key), "key [" + i + "] not found");
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_EDITABLE, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_EDITABLE, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());
		cacheManager.clear(CONTEXT_EDITABLE);
	}

	/**
	 * Test sur maxNbRow elements.
	 */
	@Test
	public void testPutMassUnmodifiable() {
		final int nbRow = maxNbRow;
		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			final Serializable value = new Element();
			cacheManager.put(CONTEXT_READONLY, key, value);
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = maxNbRow / 2; i < maxNbRow * 0.55d; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_READONLY, key));
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_READONLY, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma clé[" + i + "]";
			assertNotNull(cacheManager.get(CONTEXT_READONLY, key));
		}
		//assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());
		cacheManager.clear(CONTEXT_EDITABLE);
	}

	/**
	 * Test l'accès concurrent au cache.
	 */
	@Test
	public void testConcurrentAccess() {
		final Set<Thread> threadSet = new HashSet<>();
		final long baseTime = System.currentTimeMillis();
		final long deathTime = baseTime + 5 * 1000;
		final int nbRow = maxNbRow / 10;
		final int nbReader = 100;
		final int nbWriter = 10;

		for (int i = 0; i < nbReader; i++) {
			final CacheReader ope = new CacheReader(cacheManager, deathTime, nbRow);
			threadSet.add(new Thread(ope, "Reader_" + i));
		}
		for (int i = 0; i < nbWriter; i++) {
			final CacheWriter ope = new CacheWriter(cacheManager, deathTime, nbRow);
			threadSet.add(new Thread(ope, "Writer_" + i));
		}

		for (final Thread th : threadSet) {
			th.start();
		}
		System.out.println("Start : " + nbReader + " readers et " + nbWriter + " writers, utilisation de " + nbRow + " clés, pendant " + (deathTime - baseTime) / 1000 + "s ");

		//on attend la fin
		for (final Thread th : threadSet) {
			try {
				th.join();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt(); //si interrupt on relance
			}
		}
		cacheManager.clear(CONTEXT_EDITABLE);
	}

	private class CacheReader implements Runnable {
		private final CacheManager lCacheManager;
		private final long deathTime;
		private final long nbRow;

		public CacheReader(final CacheManager cacheManager, final long deathTime, final long nbRow) {
			lCacheManager = cacheManager;
			this.deathTime = deathTime;
			this.nbRow = nbRow;
		}

		@Override
		public void run() {
			while (!Thread.interrupted() && System.currentTimeMillis() < deathTime) {
				final String key = "ma clé[" + Math.round(Math.random() * nbRow) + "]";
				lCacheManager.get(CONTEXT_EDITABLE, key); //on test juste le get
				try {
					Thread.sleep(10); //on rend juste la main
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt(); //si interrupt on relance
				}
			}
		}
	}

	private class CacheWriter implements Runnable {
		private final CacheManager lCacheManager;
		private final long deathTime;
		private final long nbRow;

		public CacheWriter(final CacheManager cacheManager, final long deathTime, final long nbRow) {
			lCacheManager = cacheManager;
			this.deathTime = deathTime;
			this.nbRow = nbRow;
		}

		@Override
		public void run() {
			while (System.currentTimeMillis() < deathTime) {
				final String key = "ma clé[" + Math.round(Math.random() * nbRow) + "]";
				final Serializable value = new Element();
				lCacheManager.put(CONTEXT_READONLY, key, value);
				try {
					Thread.sleep(10); //on rend juste la main
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt(); //si interrupt on relance
				}
			}
		}
	}

	//	/**
	//	 * Test que quand on ferme
	//	  * @throws KSystemException   si erreur récupération cache
	//	 */
	//	public void testClose() throws KSystemException {
	//		manager.put(CONTEXT, String.valueOf(1), String.valueOf(1));
	//		assertNotNull(manager.get(CONTEXT, String.valueOf(1)));
	//
	//		try {
	//			manager.close();
	//			assertNotNull(manager.get(CONTEXT, String.valueOf(1)));
	//			fail("Cache toujours actif");
	//		} catch (final IllegalStateException e) {
	//			assertEquals("The CacheManager is not alive.", e.getMessage());
	//		}
	//	}

	/**
	 * Test element.
	 */
	static class Element implements Serializable {
		private static final long serialVersionUID = 1L;
		private static long count;
		private final long i;

		/**
		 * Constructor.
		 */
		Element() {
			i = count++;
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof Element) {
				return ((Element) obj).i == i;
			}
			return false;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return String.valueOf(i).hashCode();
		}

	}
}
