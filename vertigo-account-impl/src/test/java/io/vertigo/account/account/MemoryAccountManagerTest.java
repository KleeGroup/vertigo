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
package io.vertigo.account.account;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.transaction.VTransactionWritable;

public final class MemoryAccountManagerTest extends AbstractAccountManagerTest {

	@Inject
	private AccountManager accountManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return MyNodeConfig.config(false, false);
	}

	@Test
	public void testPhotoMemory() {
		//Before the photo is the default photo
		Assertions.assertFalse(accountManager.getPhoto(accountUID1).isPresent());
		Assertions.assertEquals("defaultPhoto.png", accountManager.getDefaultPhoto().getFileName());
		//-----
		//	final VFile photo = fileManager.createFile(new File(this.getClass().getResource("../data/marianne.png").toURI()));
		//	identityManager.setPhoto(accountUID0, photo);
		//-----
		Assertions.assertTrue(accountManager.getPhoto(accountUID0).isPresent());
		Assertions.assertEquals("photo-jdoe.jpg", accountManager.getPhoto(accountUID0).get().getFileName());
	}

	@Test
	public void testGroupsMemory() {
		try (VTransactionWritable tx = obtainTx()) {
			//Assertions.assertEquals(2, identityManager.getGroupsCount());
			//----
			Assertions.assertEquals(1, accountManager.getGroupUIDs(accountUID0).size());
			Assertions.assertEquals(2, accountManager.getGroupUIDs(accountUID1).size());
			Assertions.assertEquals(2, accountManager.getGroupUIDs(accountUID2).size());
			Assertions.assertEquals(2, accountManager.getAccountUIDs(groupURI).size());
			Assertions.assertEquals(10 + 4, accountManager.getAccountUIDs(groupAllURI).size());
			//---
			/*identityManager.attach(accountUID0, groupURI);
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountUID0).size());
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountUID1).size());
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountUID2).size());
			Assertions.assertEquals(3, identityManager.getAccountURIs(groupURI).size());
			Assertions.assertEquals(10 + 3, identityManager.getAccountURIs(groupAllURI).size());*/
		}
	}

}
