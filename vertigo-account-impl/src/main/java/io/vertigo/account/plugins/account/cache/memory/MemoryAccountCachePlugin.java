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
package io.vertigo.account.plugins.account.cache.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountCachePlugin;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class MemoryAccountCachePlugin implements AccountCachePlugin {
	private final Map<UID<Account>, Account> accountByUID = new HashMap<>();
	private final Map<String, UID<Account>> accountUIDByAuthToken = new HashMap<>();
	private final Map<UID<AccountGroup>, AccountGroup> groupByUID = new HashMap<>();
	//---
	private final Map<UID<Account>, Set<UID<AccountGroup>>> groupByAccountUID = new HashMap<>();
	private final Map<UID<AccountGroup>, Set<UID<Account>>> accountByGroupUID = new HashMap<>();
	//---
	private final Map<UID<Account>, VFile> photoByAccountUID = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public synchronized Optional<Account> getAccount(final UID<Account> accountUID) {
		Assertion.checkNotNull(accountUID);
		//-----
		return Optional.ofNullable(accountByUID.get(accountUID));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void putAccount(final Account account) {
		Assertion.checkNotNull(account);
		//-----
		final UID<Account> uid = account.getUID();
		//----
		final Object old = accountByUID.put(uid, account);
		if (old == null) {
			groupByAccountUID.put(uid, new HashSet<UID<AccountGroup>>());
			accountUIDByAuthToken.put(account.getAuthToken(), uid);
		}
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized Optional<AccountGroup> getGroup(final UID<AccountGroup> groupUID) {
		Assertion.checkNotNull(groupUID);
		//-----
		return Optional.ofNullable(groupByUID.get(groupUID));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void putGroup(final AccountGroup group) {
		Assertion.checkNotNull(group);
		//-----
		final UID<AccountGroup> uid = group.getUID();
		//----
		Assertion.checkArgument(!groupByUID.containsKey(uid), "this group is already registered, you can't create it");
		//-----
		accountByGroupUID.put(uid, new HashSet<UID<Account>>());
		groupByUID.put(uid, group);
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized void attach(final Set<UID<Account>> accountsUID, final UID<AccountGroup> groupUID) {
		Assertion.checkNotNull(accountsUID);
		Assertion.checkNotNull(groupUID);
		//-----
		accountsUID.forEach(accountURI -> this.attach(accountURI, groupUID));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void attach(final UID<Account> accountUID, final Set<UID<AccountGroup>> groupUIDs) {
		//-----
		groupUIDs.forEach(groupURI -> this.attach(accountUID, groupURI));
	}

	private synchronized void attach(final UID<Account> accountUID, final UID<AccountGroup> groupUID) {
		Assertion.checkNotNull(accountUID);
		Assertion.checkNotNull(groupUID);
		//-----
		final Set<UID<AccountGroup>> groupUIDs = groupByAccountUID.computeIfAbsent(accountUID, key -> new HashSet<>());
		groupUIDs.add(groupUID);
		//-----
		final Set<UID<Account>> accountUIDs = accountByGroupUID.computeIfAbsent(groupUID, key -> new HashSet<>());
		accountUIDs.add(accountUID);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		Assertion.checkNotNull(accountUID);
		//-----
		final Set<UID<AccountGroup>> groupUIDs = groupByAccountUID.get(accountUID);
		if (groupUIDs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(groupUIDs);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		Assertion.checkNotNull(groupUID);
		//-----
		final Set<UID<Account>> accountURIs = accountByGroupUID.get(groupUID);
		if (accountURIs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(accountURIs);
	}

	/** {@inheritDoc} */
	@Override
	public void setPhoto(final UID<Account> accountUID, final VFile photo) {
		Assertion.checkNotNull(accountUID);
		Assertion.checkNotNull(photo);
		//-----
		photoByAccountUID.put(accountUID, photo);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountUID) {
		Assertion.checkNotNull(accountUID);
		//-----
		return Optional.ofNullable(photoByAccountUID.get(accountUID));
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		photoByAccountUID.clear();
		accountByGroupUID.clear();
		accountByUID.clear();
		groupByAccountUID.clear();
		groupByUID.clear();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		final UID<Account> accountUID = accountUIDByAuthToken.get(userAuthToken);
		if (accountUID != null) {
			return getAccount(accountUID);
		}
		return Optional.empty();
	}

}
