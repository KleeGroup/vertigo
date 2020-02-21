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
package io.vertigo.account.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.plugins.account.store.loader.AccountLoader;
import io.vertigo.account.plugins.account.store.loader.GroupLoader;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.util.ListBuilder;

public final class TestIdentities implements AccountLoader, GroupLoader {

	private final Map<String, Account> accountsMap = new HashMap<>();
	private final Map<String, Account> accountsMapByAuth = new HashMap<>();
	private final Map<String, AccountGroup> groupsMap = new HashMap<>();
	private final Map<UID<Account>, Set<UID<AccountGroup>>> groupsPerAccount = new HashMap<>();
	private final Map<UID<AccountGroup>, Set<UID<Account>>> accountsPerGroup = new HashMap<>();

	private TestIdentities() {
		//rien
	}

	public static UID<Account> createAccountURI(final String id) {
		return UID.of(Account.class, id);
	}

	public static UID<AccountGroup> createGroupURI(final String id) {
		return UID.of(AccountGroup.class, id);
	}

	public void initData() {
		final Account testAccount0 = Account.builder("0").withAuthToken("john.doe").withDisplayName("John doe").withEmail("john.doe@yopmail.com").build();
		final Account testAccount1 = Account.builder("1").withAuthToken("palmer.luckey").withDisplayName("Palmer Luckey").withEmail("palmer.luckey@yopmail.com").build();
		final Account testAccount2 = Account.builder("2").withAuthToken("bill.clinton").withDisplayName("Bill Clinton").withEmail("bill.clinton@yopmail.com").build();
		final Account testAccount3 = Account.builder("3").withAuthToken("admin").withDisplayName("Phil Mormon").withEmail("phil.mormon@yopmail.com").build();
		saveAccounts(Arrays.asList(testAccount0, testAccount1, testAccount2, testAccount3));

		final UID<Account> accountURI0 = createAccountURI(testAccount0.getId());
		final UID<Account> accountURI1 = createAccountURI(testAccount1.getId());
		final UID<Account> accountURI2 = createAccountURI(testAccount2.getId());

		final AccountGroup testAccountGroup1 = new AccountGroup("100", "TIME's cover");
		final UID<AccountGroup> group1Uri = UID.of(AccountGroup.class, testAccountGroup1.getId());
		saveGroup(testAccountGroup1);

		attach(accountURI1, group1Uri);
		attach(accountURI2, group1Uri);

		final AccountGroup groupAll = new AccountGroup("ALL", "Everyone");
		final UID<AccountGroup> groupAllUri = UID.of(AccountGroup.class, groupAll.getId());
		saveGroup(groupAll);
		attach(accountURI0, groupAllUri);
		attach(accountURI1, groupAllUri);
		attach(accountURI2, groupAllUri);

		//---create 10 noisy data
		final List<Account> accounts = createAccounts();
		saveAccounts(accounts);
		for (final Account account : accounts) {
			final UID<Account> accountUri = createAccountURI(account.getId());
			attach(accountUri, groupAllUri);
		}

	}

	private static int SEQ_ID = 10;

	private static List<Account> createAccounts() {
		return new ListBuilder<Account>()
				.add(createAccount("Jean Meunier", "jmeunier@yopmail.com"))
				.add(createAccount("Emeline Granger", "egranger@yopmail.com"))
				.add(createAccount("Silvia Robert", "sylv.robert@yopmail.com"))
				.add(createAccount("Manuel Long", "manu@yopmail.com"))
				.add(createAccount("David Martin", "david.martin@yopmail.com"))
				.add(createAccount("Véronique LeBourgeois", "vero89@yopmail.com"))
				.add(createAccount("Bernard Dufour", "bdufour@yopmail.com"))
				.add(createAccount("Nicolas Legendre", "nicolas.legendre@yopmail.com"))
				.add(createAccount("Marie Garnier", "marie.garnier@yopmail.com"))
				.add(createAccount("Hugo Bertrand", "hb@yopmail.com"))
				.build();
	}

	private static Account createAccount(final String displayName, final String email) {
		return Account.builder(Integer.toString(SEQ_ID++))
				.withAuthToken(email.substring(0, email.indexOf('@')))
				.withDisplayName(displayName)
				.withEmail(email)
				.build();
	}

	private void attach(final UID<Account> accountURI, final UID<AccountGroup> groupURI) {
		groupsPerAccount.computeIfAbsent(accountURI, key -> new HashSet<>()).add(groupURI);
		accountsPerGroup.computeIfAbsent(groupURI, key -> new HashSet<>()).add(accountURI);
	}

	private void saveGroup(final AccountGroup accountGroup) {
		groupsMap.put(accountGroup.getId(), accountGroup);
	}

	private void saveAccounts(final List<Account> accounts) {
		accounts.stream().forEach(account -> {
			accountsMap.put(account.getId(), account);
			accountsMapByAuth.put(account.getAuthToken(), account);
		});
	}

	@Override
	public long getAccountsCount() {
		return accountsMap.size();
	}

	@Override
	public Account getAccount(final UID<Account> accountURI) {
		return accountsMap.get(accountURI);
	}

	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountURI) {
		return Optional.empty();
	}

	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return Optional.ofNullable(accountsMapByAuth.get(userAuthToken));
	}

	@Override
	public long getGroupsCount() {
		return groupsMap.size();
	}

	@Override
	public AccountGroup getGroup(final UID<AccountGroup> groupURI) {
		return groupsMap.get(groupURI);
	}

	@Override
	public Set<UID<AccountGroup>> getGroupURIs(final UID<Account> accountURI) {
		return groupsPerAccount.computeIfAbsent(accountURI, key -> Collections.emptySet());
	}

	@Override
	public Set<UID<Account>> getAccountURIs(final UID<AccountGroup> groupURI) {
		// TODO Auto-generated method stub
		return null;
	}
}
