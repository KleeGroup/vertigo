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
package io.vertigo.account.plugins.authentication.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.account.impl.authentication.AuthenticationPlugin;
import io.vertigo.account.impl.authentication.UsernameAuthenticationToken;
import io.vertigo.account.impl.authentication.UsernamePasswordAuthenticationToken;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * A simple implementation of the Realm interface that
 * uses a set of configured user accounts and roles to support authentication and authorization.  Each account entry
 * specifies the username, password, and roles for a user.  Roles can also be mapped
 * to permissions and associated with users.
 * <p/>
 * User accounts and roles are stored in two {@code Map}s in memory, so it is expected that the total number of either
 * is not sufficiently large.
 *
 * @since 0.1
 */
public class TextAuthenticationPlugin implements AuthenticationPlugin, Activeable {
	//	accountKey  |  login  |  password
	private static final String FILE_PATTERN_STR = "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s*\\/\\/.*$";
	private static final Pattern FILE_PATTERN = Pattern.compile(FILE_PATTERN_STR);

	private final Map<String, AuthenticationAccountInfo> users; //username-to-SimpleAccount
	private final ResourceManager resourceManager;
	private final String filePath;

	/**
	 * Constructor.
	 * @param resourceManager Resource Manager
	 * @param filePath File path
	 */
	@Inject
	public TextAuthenticationPlugin(@ParamValue("filePath") final String filePath, final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		// -----
		this.resourceManager = resourceManager;
		this.filePath = filePath;
		//SimpleAccountRealms are memory-only realms
		users = new LinkedHashMap<>();

	}

	/** {@inheritDoc} */
	@Override
	public boolean supports(final AuthenticationToken token) {
		return token instanceof UsernameAuthenticationToken
				|| token instanceof UsernamePasswordAuthenticationToken;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> authenticateAccount(final AuthenticationToken token) {
		final AuthenticationAccountInfo authenticationAccountInfo = users.get(token.getPrincipal());
		if (authenticationAccountInfo != null //Username exists
				&& token.match(authenticationAccountInfo.getAuthenticationToken())) { //and tokens match
			return Optional.of(authenticationAccountInfo.getAccountKey());
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final URL realmURL = resourceManager.resolve(filePath);
		try {
			final String confTest = parseFile(realmURL);
			try (final Scanner scanner = new Scanner(confTest)) {
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					parseUserInfo(line);
				}
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur durant la lecture du Realm {0}", realmURL);
		}
	}

	private void parseUserInfo(final String line) {
		final Matcher matcher = FILE_PATTERN.matcher(line);
		final boolean matches = matcher.matches();
		Assertion.checkState(matches, "No match found for entry '{0}' and pattern '{1}'", line, FILE_PATTERN_STR);
		//---
		final String accountKey = matcher.group(1);
		final String username = matcher.group(2);
		final String password = matcher.group(3);
		final AuthenticationToken authenticationToken;
		if (password.isEmpty()) {
			authenticationToken = new UsernameAuthenticationToken(username);
		} else {
			authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		}
		users.put(username, new AuthenticationAccountInfo(accountKey, authenticationToken));
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

	/** {@inheritDoc} */
	@Override
	public void stop() {
		users.clear();
	}
}
