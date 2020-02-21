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
package io.vertigo.account.plugins.account.store.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * A simple implementation of the Realm interface that
 * uses a set of configured account accounts and roles to support authentication and authorization.  Each account entry
 * specifies the accountname, password, and roles for a account.  Roles can also be mapped
 * to permissions and associated with accounts.
 * <p/>
 * Account accounts and roles are stored in two {@code Map}s in memory, so it is expected that the total number of either
 * is not sufficiently large.
 *
 * @since 0.1
 */
public class TextAccountStorePlugin implements AccountStorePlugin, Activeable {
	private final Pattern accountFilePattern;
	private final Pattern groupFilePattern;

	private final Map<String, AccountInfo> accounts; //id-to-Account
	private final Map<String, List<Account>> accountsPerGroup; //groupId-to-Accounts
	private final Map<String, AccountGroup> groups; //id-to-Groups
	private final Map<String, List<AccountGroup>> groupsPerAccount; //accountId-to-Groups
	private final ResourceManager resourceManager;
	private final String accountFilePath;
	private final String groupFilePath;

	private enum AccountProperty {
		id, displayName, email, authToken, photoUrl
	}

	private enum GroupProperty {
		id, displayName, accountIds
	}

	/**
	 * Constructor.
	 * @param resourceManager Resource Manager
	 * @param accountFilePath File path
	 * @param accountFilePatternStr File Pattern (id, displayName, email, authToken, photoUrl)
	 */
	@Inject
	public TextAccountStorePlugin(
			@ParamValue("accountFilePath") final String accountFilePath,
			@ParamValue("accountFilePattern") final String accountFilePatternStr,
			@ParamValue("groupFilePath") final String groupFilePath,
			@ParamValue("groupFilePattern") final String groupFilePatternStr,
			final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(accountFilePatternStr);
		Assertion.checkArgument(accountFilePatternStr.contains("(?<"),
				"accountFilePattern should be a regexp of named group for each Account's fields (like : '(?<id>[^\\s;]+);(?<displayName>[^\\s;]+);(?<email>)(?<authToken>[^\\s;]+);(?<photoUrl>[^\\s;]+)' )");
		Assertion.checkArgument(groupFilePatternStr.contains("(?<"),
				"groupFilePattern should be a regexp of named group for each group's fields (like : '(?<id>[^\\s;]+);(?<displayName>[^\\s;]+);(?<accountIds>([^\\s;]+(;[^\\s;]+)*)' )");
		for (final AccountProperty accountProperty : AccountProperty.values()) {
			Assertion.checkArgument(accountFilePatternStr.contains("(?<" + accountProperty.name() + ">"),
					"filePattern should be a regexp of named group for each Account fields (missing {0} field) (like : '(?<id>\\S+);(?<displayName>\\S+);(?<email>)(?<authToken>\\S+);(?<photoUrl>\\S+)' )", accountProperty.name());
		}
		for (final GroupProperty groupProperty : GroupProperty.values()) {
			Assertion.checkArgument(groupFilePatternStr.contains("(?<" + groupProperty.name() + ">"),
					"filePattern should be a regexp of named group for each Group fields (missing {0} field) (like : '(?<id>[^\\s;]+);(?<displayName>[^\\s;]+);(?<accountIds>([^\\s;]+(;[^\\s;]+)*)' )", groupProperty.name());
		}
		// -----
		this.resourceManager = resourceManager;
		this.accountFilePath = accountFilePath.replace(File.separatorChar, '/');
		this.groupFilePath = groupFilePath.replace(File.separatorChar, '/');
		//SimpleAccountRealms are memory-only realms
		accounts = new LinkedHashMap<>();
		groups = new LinkedHashMap<>();
		groupsPerAccount = new LinkedHashMap<>();
		accountsPerGroup = new HashMap<>();
		accountFilePattern = Pattern.compile(accountFilePatternStr);
		groupFilePattern = Pattern.compile(groupFilePatternStr);
	}

	@Override
	public Account getAccount(final UID<Account> accountURI) {
		return accounts.get(accountURI.getId()).getAccount();
	}

	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		return groupsPerAccount.get(accountUID.getId()).stream()
				.map(AccountGroup::getUID)
				.collect(Collectors.toSet());
	}

	@Override
	public AccountGroup getGroup(final UID<AccountGroup> groupURI) {
		return groups.get(groupURI.getId());
	}

	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupURI) {
		return accountsPerGroup.get(groupURI.getId()).stream()
				.map(Account::getUID)
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String accountAuthToken) {
		final Optional<AccountInfo> accountInfoOpt = accounts.values().stream()
				.filter(accountInfo -> accountAuthToken.equals(accountInfo.getAccount().getAuthToken()))
				.findFirst();
		return accountInfoOpt.map(AccountInfo::getAccount);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountURI) {
		final AccountInfo accountInfo = accounts.get(accountURI.getId());
		Assertion.checkNotNull(accountInfo, "No account found for {0}", accountURI);
		if (accountInfo.getPhotoUrl() == null || accountInfo.getPhotoUrl().isEmpty()) {
			return Optional.empty();
		}
		final URL fileURL;
		if (accountInfo.getPhotoUrl().startsWith(".")) {//si on est en relatif, on repart du prefix du fichier des accounts
			final String accountFilePrefix = accountFilePath.substring(0, accountFilePath.lastIndexOf('/')) + "/";
			fileURL = resourceManager.resolve(accountFilePrefix + accountInfo.getPhotoUrl());
		} else {
			fileURL = resourceManager.resolve(accountInfo.getPhotoUrl());
		}

		return createVFile(accountURI, fileURL, accountInfo.getPhotoUrl());
	}

	private static Optional<VFile> createVFile(final UID<Account> accountURI, final URL fileURL, final String photoUrl) {
		Path photoFile;
		try {
			photoFile = Paths.get(fileURL.toURI());
		} catch (final URISyntaxException e) {
			return Optional.empty();
		}
		Assertion.checkArgument(photoFile.toFile().exists(), "Account {0} photo {1} not found", accountURI, photoUrl);
		Assertion.checkArgument(photoFile.toFile().isFile(), "Account {0} photo {1} must be a file", accountURI, photoUrl);
		try {
			final String contentType = Files.probeContentType(photoFile);
			return Optional.of(new FSFile(photoFile.getFileName().toString(), contentType, photoFile));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		readData(this::parseAccounts, accountFilePath);
		readData(this::parseGroups, groupFilePath);
	}

	private void readData(final Consumer<String> parser, final String filePath) {
		final URL fileURL = resourceManager.resolve(filePath);
		try {
			final String confTest = parseFile(fileURL);
			try (final Scanner scanner = new Scanner(confTest)) {
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					parser.accept(line);
				}
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur durant la lecture des données {0}", fileURL);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		accounts.clear();
	}

	private void parseAccounts(final String line) {
		final Matcher matcher = accountFilePattern.matcher(line);
		final boolean matches = matcher.matches();
		Assertion.checkState(matches, "AccountFile ({2}) can't be parse by this regexp :\nline:{0}\nregexp:{1}", line, matches, accountFilePath);
		final String id = matcher.group(AccountProperty.id.name());
		final String displayName = matcher.group(AccountProperty.displayName.name());
		final String email = matcher.group(AccountProperty.email.name());
		final String authToken = matcher.group(AccountProperty.authToken.name());
		final String photoUrl = matcher.group(AccountProperty.photoUrl.name());
		final Account account = Account.builder(id)
				.withDisplayName(displayName)
				.withEmail(email)
				.withAuthToken(authToken)
				.build();
		accounts.put(id, new AccountInfo(account, photoUrl));
	}

	private void parseGroups(final String line) {
		final Matcher matcher = groupFilePattern.matcher(line);
		final boolean matches = matcher.matches();
		Assertion.checkState(matches, "GroupFile ({2}) can't be parse by this regexp :\nline:{0}\nregexp:{1}", line, matches, groupFilePath);
		final String groupId = matcher.group(GroupProperty.id.name());
		final String displayName = matcher.group(GroupProperty.displayName.name());
		final String accountIds = matcher.group(GroupProperty.accountIds.name());

		final AccountGroup accountGroup = new AccountGroup(groupId, displayName);
		groups.put(groupId, accountGroup);
		final List<Account> groupAccounts = new ArrayList<>();
		for (final String accountId : accountIds.split(";")) {
			groupsPerAccount.computeIfAbsent(accountId, k -> new ArrayList<>()).add(accountGroup);
			final Account account = accounts.get(accountId).getAccount();
			Assertion.checkNotNull(account, "Group {0} reference an undeclared account {1}", groupId, accountId);
			groupAccounts.add(account);
		}
		accountsPerGroup.put(groupId, groupAccounts);

	}

	private static String parseFile(final URL url) throws IOException {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		}
	}

}
