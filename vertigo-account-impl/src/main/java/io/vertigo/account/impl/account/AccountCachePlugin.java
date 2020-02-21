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
package io.vertigo.account.impl.account;

import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.model.VFile;

/**
 * @author pchretien
 */
public interface AccountCachePlugin extends Plugin {

	/**
	 * @param accountUID the account defined by its UID
	 * @return the account
	 */
	Optional<Account> getAccount(UID<Account> accountUID);

	/**
	 * @param accountUID the account defined by its UID
	 * @return Set of groups of this account
	 */
	Set<UID<AccountGroup>> getGroupUIDs(UID<Account> accountUID);

	/**
	 * Gets the group defined by an UID.
	 * @param groupUID the group UID
	 * @return the group
	 */
	Optional<AccountGroup> getGroup(UID<AccountGroup> groupUID);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupUID the group UID
	 * @return the list of acccounts.
	 */
	Set<UID<Account>> getAccountUIDs(UID<AccountGroup> groupUID);

	/**
	 * Gets the photo of an account defined by its UID.
	 *
	 * @param accountUID the account defined by its UID
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(UID<Account> accountUID);

	/**
	 * Saves an account.
	 * Caution : all the accounts must have an id.
	 * @param account account
	 */
	void putAccount(Account account);

	/**
	 * Saves a group.
	 * @param group the group
	 */
	void putGroup(AccountGroup group);

	/**
	 * Attaches an account to a group.
	 * @param accountUID the account defined by its UID
	 * @param groupUID the group
	 */
	void attach(UID<Account> accountUID, Set<UID<AccountGroup>> groupUID);

	/**
	 * Attaches an account to a group.
	 * @param accountsUID the accounts defined by their UID
	 * @param groupUID the group
	 */
	void attach(Set<UID<Account>> accountsUID, UID<AccountGroup> groupUID);

	/**
	 * Reset:
	 * - All the accounts
	 * - All the groups
	 * - All the links accounts-group
	 * - All the Photos
	 */
	void reset();

	/**
	 * Defines a photo to an account.
	 *
	 * @param accountUID the account defined by its UID
	 * @param photo the photo
	 */
	void setPhoto(UID<Account> accountUID, VFile photo);

	/**
	 * Get an newly authentify user by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);

}
