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
package io.vertigo.dynamo.kvstore;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.kvstore.data.Flower;

/**
 * @author pchretien
 */
public abstract class AbstractKVStoreManagerTest extends AbstractTestCaseJU5 {
	protected static final String COLLECTION = "MyDB:flowers";
	@Inject
	protected KVStoreManager kvStoreManager;
	@Inject
	protected VTransactionManager transactionManager;

	//	protected static Tree buildTree(final String name, final double price) {
	//		return new Tree()
	//				.setName(name)
	//				.setPrice(price);
	//	}

	protected static Flower buildFlower(final String name, final double price) {
		return new Flower()
				.setName(name)
				.setPrice(price);
	}

	@Test
	public void testCount() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final int nbFlowers = kvStoreManager.count("flowers");
			Assertions.assertEquals(0, nbFlowers);
			final Flower tulip1 = buildFlower("tulip", 100);
			kvStoreManager.put("flowers", "1", tulip1);

			final Flower tulip2 = buildFlower("tulip", 110);
			kvStoreManager.put("flowers", "2", tulip2);

			final Flower tulip3 = buildFlower("tulip", 120);
			kvStoreManager.put("flowers", "3", tulip3);

			//count after 3 inserts
			final int nbFlowers2 = kvStoreManager.count("flowers");
			Assertions.assertEquals(3, nbFlowers2);

			//count after 1 more insert of same value
			kvStoreManager.put("flowers", "4", tulip3);
			final int nbFlowers3 = kvStoreManager.count("flowers");
			Assertions.assertEquals(4, nbFlowers3);

			//count after 1 insert of same key : update
			kvStoreManager.put("flowers", "3", tulip3);
			final int nbFlowers4 = kvStoreManager.count("flowers");
			Assertions.assertEquals(4, nbFlowers4);
		}
	}

	@Test
	public void testFind() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Optional<Flower> foundFlower = kvStoreManager.find("flowers", "1", Flower.class);
			Assertions.assertEquals(Optional.empty(), foundFlower);
			final Flower tulip = buildFlower("tulip", 100);

			kvStoreManager.put("flowers", "1", tulip);
			final Optional<Flower> foundFlower2 = kvStoreManager.find("flowers", "1", Flower.class);
			Assertions.assertTrue(foundFlower2.isPresent());
			Assertions.assertEquals("tulip", foundFlower2.get().getName());
			Assertions.assertEquals(100d, foundFlower2.get().getPrice().doubleValue()); //"Price must be excatly 100",
		}
	}

	@Test
	public void testRemove() {

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Optional<Flower> flower = kvStoreManager.find("flowers", "10", Flower.class);
			Assertions.assertFalse(flower.isPresent(), "There is already a flower id 10");

			kvStoreManager.put("flowers", "10", buildFlower("daisy", 60));
			kvStoreManager.put("flowers", "11", buildFlower("tulip", 100));

			final Optional<Flower> flower1 = kvStoreManager.find("flowers", "10", Flower.class);
			final Optional<Flower> flower2 = kvStoreManager.find("flowers", "11", Flower.class);
			Assertions.assertTrue(flower1.isPresent(), "Flower id 10 not found");
			Assertions.assertTrue(flower2.isPresent(), "Flower id 11 not found");

			transaction.commit();
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Optional<Flower> flower = kvStoreManager.find("flowers", "10", Flower.class);
			Assertions.assertTrue(flower.isPresent(), "Flower id 10 not found");

			kvStoreManager.remove("flowers", "10");

			final Optional<Flower> flower1 = kvStoreManager.find("flowers", "10", Flower.class);
			final Optional<Flower> flower2 = kvStoreManager.find("flowers", "11", Flower.class);
			Assertions.assertFalse(flower1.isPresent(), "Remove flower id 10 failed");
			Assertions.assertTrue(flower2.isPresent(), "Flower id 11 not found");
		}
	}

}
