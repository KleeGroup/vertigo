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

import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.dynamo.domain.model.UID;

/**
 * @author npiedeloup
 */
public interface GroupLoader {

	/**
	 * @return the number of groups.
	 */
	long getGroupsCount();

	/**
	 * Gets the group defined by an URI.
	 * @param groupURI the group URI
	 * @return the group
	 */
	AccountGroup getGroup(UID<AccountGroup> groupURI);

	/**
	 * @param accountURI the account defined by its URI
	 * @return Set of groups of this account
	 */
	Set<UID<AccountGroup>> getGroupURIs(UID<Account> accountURI);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupURI the group URI
	 * @return the list of acccounts.
	 */
	Set<UID<Account>> getAccountURIs(UID<AccountGroup> groupURI);

}
