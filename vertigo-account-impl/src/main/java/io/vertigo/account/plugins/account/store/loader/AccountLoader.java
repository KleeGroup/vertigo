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
package io.vertigo.account.plugins.account.store.loader;

import java.util.Optional;

import io.vertigo.account.account.Account;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.model.VFile;

/**
 * @author npiedeloup
 */
public interface AccountLoader {

	/**
	 * @return the number of accounts
	 */
	long getAccountsCount();

	/**
	 * @param accountURI the account defined by its URI
	 * @return the account
	 */
	Account getAccount(UID<Account> accountURI);

	/**
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(UID<Account> accountURI);

	/**
	 * Get an newly authentify user by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);
}
