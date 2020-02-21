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
package io.vertigo.dynamo.kvstore.berkeley;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.kvstore.AbstractKVStoreManagerTest;
import io.vertigo.dynamo.kvstore.data.Flower;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
public final class BerkeleyKVStoreManagerTest extends AbstractKVStoreManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withMemoryCache()
						.build())
				.addModule(new DynamoFeatures()
						.withKVStore()
						.withBerkleyKV(
								Param.of("collections", "flowers;TTL=10, trees;inMemory"),
								Param.of("dbFilePath", "d:/dynamo-tmp"))
						.build())
				.build();
	}

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvStoreManager.clear("flowers");
		}
	}

	@Test
	public void testInsertMass() {

		for (int j = 0; j < 10; j++) {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				for (int i = 0; i < 10; i++) {
					kvStoreManager.put("flowers", String.valueOf(j * 1000 + i), buildFlower("Test", 60));
				}
				transaction.commit();
			}
		}
	}

	@Test
	public void testFindAll() {
		final List<Flower> flowers = new ListBuilder<Flower>()
				.add(buildFlower("daisy", 60))
				.add(buildFlower("tulip", 100))
				.add(buildFlower("rose", 110))
				.add(buildFlower("lily", 120))
				.add(buildFlower("orchid", 200))
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final List<Flower> foundFlowers = kvStoreManager.findAll("flowers", 0, null, Flower.class);
			Assertions.assertTrue(foundFlowers.isEmpty());

			int i = 0;
			for (final Flower flower : flowers) {
				final String id = "" + i++;
				kvStoreManager.put("flowers", id, flower);
			}

			final List<Flower> foundFlowers2 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class);
			Assertions.assertEquals(flowers.size(), foundFlowers2.size());
			transaction.commit();
		}
	}

	@Test
	public void testRemoveFail() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				kvStoreManager.remove("flowers", "1");
			}
		});
	}

	@Test
	public void testRollback() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final Flower tulip = buildFlower("tulip", 100);
				kvStoreManager.put("flowers", "1", tulip);
				transaction.commit();
			}
			final Optional<Flower> flower1 = kvStoreManager.find("flowers", "1", Flower.class);
			Assertions.assertTrue(flower1.isPresent(), "Flower id 1 not found");

			final Optional<Flower> flower2 = kvStoreManager.find("flowers", "2", Flower.class);
			Assertions.assertFalse(flower2.isPresent(), "There is already a flower id 2");
			try {
				try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
					final Flower tulip = buildFlower("rose", 100);
					kvStoreManager.put("flowers", "2", tulip);
					throw new VSystemException("Error");
				}
			} catch (final RuntimeException e) {
				//on doit passer par là
			}

			final Optional<Flower> flower2bis = kvStoreManager.find("flowers", "2", Flower.class);
			Assertions.assertFalse(flower2bis.isPresent(), "Rollback flower id 2 failed");
		});

	}

	@Test
	@Disabled
	public void testPurgeTooOldElements() {
		int flowerId = 1;
		for (int i = 0; i < 40; i++) {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final int countFlowers = kvStoreManager.count("flowers");
				System.out.println("flowers count " + countFlowers);
				//Assertions.assertEquals(0, nbFlowers);
				//put a flower a t+0s (expire a T+10s)
				final Flower tulip1 = buildFlower("tulip", 100);
				kvStoreManager.put("flowers", String.valueOf(flowerId++), tulip1);

				//put a flower a t+2s (expire a T+12s)
				final Flower tulip2 = buildFlower("tulip", 110);
				kvStoreManager.put("flowers", String.valueOf(flowerId++), tulip2);

				//put a flower a t+4s (expire a T+14s)
				final Flower tulip3 = buildFlower("tulip", 120);
				kvStoreManager.put("flowers", String.valueOf(flowerId++), tulip3);

				final long searchFlowers = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size(); //can't use count as it doesnt detect too old element (needs daemon)
				System.out.println("flowers search " + searchFlowers);
				//
				transaction.commit();
			}
			sleep(2);
		}
		sleep(10);
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final int finalSearchFlowers = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size();
			//search always filter too old elements
			Assertions.assertEquals(0, finalSearchFlowers);

			final int finalCountFlowers = kvStoreManager.count("flowers");
			//count needs daemon
			Assertions.assertNotEquals(3 * 50 - 3, finalCountFlowers);
		}

	}

	@Test
	public void testTimeToLive() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final int nbFlowers = kvStoreManager.count("flowers");
			Assertions.assertEquals(0, nbFlowers);
			//put a flower a t+0s (expire a T+10s)
			final Flower tulip1 = buildFlower("tulip", 100);
			kvStoreManager.put("flowers", "1", tulip1);
			sleep(2);

			//put a flower a t+2s (expire a T+12s)
			final Flower tulip2 = buildFlower("tulip", 110);
			kvStoreManager.put("flowers", "2", tulip2);
			sleep(2);

			//put a flower a t+4s (expire a T+14s)
			final Flower tulip3 = buildFlower("tulip", 120);
			kvStoreManager.put("flowers", "3", tulip3);
			sleep(2);

			//count after 3 inserts and T+6s
			final long nbFlowers2 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size(); //can't use count as it doesnt detect too old element (needs daemon)
			Assertions.assertEquals(3, nbFlowers2);

			sleep(3);

			//find unexpired element
			final Optional<Flower> tulip1Load = kvStoreManager.find("flowers", "1", Flower.class);
			Assertions.assertTrue(tulip1Load.isPresent());

			//count after 3 inserts and T+9s
			final long nbFlowers3 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size();
			Assertions.assertEquals(3, nbFlowers3);

			sleep(2);

			//count after 3 inserts and T+11s
			final long nbFlowers4 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size();
			Assertions.assertEquals(2, nbFlowers4);
			sleep(2);

			//count after 3 inserts and T+13s
			final long nbFlowers5 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size();
			Assertions.assertEquals(1, nbFlowers5);
			sleep(2);

			//count after 3 inserts and 15s
			final long nbFlowers6 = kvStoreManager.findAll("flowers", 0, 1000, Flower.class).size();
			Assertions.assertEquals(0, nbFlowers6);

			//find expired element
			final Optional<Flower> tulip1Reload = kvStoreManager.find("flowers", "1", Flower.class);
			Assertions.assertFalse(tulip1Reload.isPresent());
		}
	}

	private void sleep(final int timeSecond) {
		try {
			Thread.sleep(timeSecond * 1000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}

}
