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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.account.AccountManager;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien, npiedeloup
 */
public final class AccountManagerImpl implements AccountManager {
	private final AccountStorePlugin accountStorePlugin; //accès local aux users
	private final Optional<AccountCachePlugin> accountCachePlugin;
	private final VFile defaultPhoto;

	/**
	 * Constructor.
	 * @param accountStorePlugin the account store plugin
	 * @param accountCachePlugin the account cache plugin
	 * @param fileManager the file manager
	 */
	@Inject
	public AccountManagerImpl(
			final AccountStorePlugin accountStorePlugin,
			final Optional<AccountCachePlugin> accountCachePlugin,
			final FileManager fileManager) {
		Assertion.checkNotNull(accountStorePlugin);
		Assertion.checkNotNull(accountCachePlugin);
		Assertion.checkNotNull(fileManager);
		//-----
		this.accountStorePlugin = accountStorePlugin;
		this.accountCachePlugin = accountCachePlugin;
		defaultPhoto = fileManager.createFile(
				"defaultPhoto.png",
				"image/png",
				AccountManagerImpl.class.getResource("defaultPhoto.png"));
	}

	/** {@inheritDoc} */
	@Override
	public VFile getDefaultPhoto() {
		return defaultPhoto;
	}

	//------------------//
	//-- AccountStore --//
	//------------------//
	/** {@inheritDoc} */
	@Override
	public Account getAccount(final UID<Account> accountUID) {
		return loadWithCache(accountUID,
				() -> accountCachePlugin.get()::getAccount,
				accountStorePlugin::getAccount,
				() -> accountCachePlugin.get()::putAccount);
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final UID<AccountGroup> groupUID) {
		return loadWithCache(groupUID,
				() -> accountCachePlugin.get()::getGroup,
				accountStorePlugin::getGroup,
				() -> accountCachePlugin.get()::putGroup);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountUID) {
		return loadWithCacheOptionalValue(accountUID,
				() -> accountCachePlugin.get()::getPhoto,
				accountStorePlugin::getPhoto,
				() -> photo -> accountCachePlugin.get().setPhoto(accountUID, photo));
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		return loadWithCacheSetValue(groupUID,
				() -> accountCachePlugin.get()::getAccountUIDs,
				accountStorePlugin::getAccountUIDs,
				() -> accountsUID -> accountCachePlugin.get().attach(accountsUID, groupUID));
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		return loadWithCacheSetValue(accountUID,
				() -> accountCachePlugin.get()::getGroupUIDs,
				accountStorePlugin::getGroupUIDs,
				() -> groupsUID -> accountCachePlugin.get().attach(accountUID, groupsUID));
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return loadWithCacheOptionalValue(userAuthToken,
				() -> accountCachePlugin.get()::getAccountByAuthToken,
				accountStorePlugin::getAccountByAuthToken,
				() -> accountCachePlugin.get()::putAccount);
	}

	private <O extends Object, U extends Object> Optional<O> loadWithCacheOptionalValue(
			final U uid,
			final Supplier<Function<U, Optional<O>>> cacheSupplier,
			final Function<U, Optional<O>> storeSupplier,
			final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uid);
			if (!resultOpt.isPresent()) {
				final Optional<O> result = storeSupplier.apply(uid);
				if (result.isPresent()) {
					cacheRegister.get().accept(result.get());
				}
				return result;
			}
		}
		return storeSupplier.apply(uid);

	}

	private <O extends Object, U extends Object> Set<O> loadWithCacheSetValue(
			final U uid,
			final Supplier<Function<U, Set<O>>> cacheSupplier,
			final Function<U, Set<O>> storeSupplier,
			final Supplier<Consumer<Set<O>>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Set<O> resultOpt = cacheSupplier.get().apply(uid);
			if (resultOpt.isEmpty()) {
				final Set<O> result = storeSupplier.apply(uid);
				if (!result.isEmpty()) {
					cacheRegister.get().accept(result);
				}
				return result;
			}
		}
		return storeSupplier.apply(uid);

	}

	private <O extends Object, U extends Object> O loadWithCache(
			final U uid,
			final Supplier<Function<U, Optional<O>>> cacheSupplier,
			final Function<U, O> storeSupplier,
			final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uid);
			if (!resultOpt.isPresent()) {
				final O result = storeSupplier.apply(uid);
				cacheRegister.get().accept(result);
				return result;
			}
		}
		return storeSupplier.apply(uid);
	}
}
