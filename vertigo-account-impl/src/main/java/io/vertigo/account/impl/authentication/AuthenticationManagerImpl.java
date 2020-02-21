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
package io.vertigo.account.impl.authentication;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountManager;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.lang.Assertion;

/**
 * Main authentication manager implementation.
 * @author npiedeloup
 */
public final class AuthenticationManagerImpl implements AuthenticationManager {
	private static final String USER_SESSION_ACCOUNT_KEY = "vertigo.account.authentication";

	private final List<AuthenticationPlugin> authenticationPlugins;
	private final AccountManager accountManager;
	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param accountManager the account Manager
	 * @param securityManager the security Manager
	 * @param authenticationPlugins List of authenticationPlugins
	 */
	@Inject
	public AuthenticationManagerImpl(
			final AccountManager accountManager,
			final VSecurityManager securityManager,
			final List<AuthenticationPlugin> authenticationPlugins) {
		Assertion.checkNotNull(accountManager);
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(authenticationPlugins);
		//----
		this.accountManager = accountManager;
		this.securityManager = securityManager;
		this.authenticationPlugins = authenticationPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> login(final AuthenticationToken token) {
		final Optional<Account> accountOpt = tryLoginAccount(token);
		if (accountOpt.isPresent()) {
			securityManager.getCurrentUserSession().ifPresent(userSession -> {
				userSession.authenticate();
				userSession.putAttribute(USER_SESSION_ACCOUNT_KEY, accountOpt.get());
			});
		}
		return accountOpt;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getLoggedAccount() {
		return securityManager.getCurrentUserSession()
				.map(userSession -> userSession.getAttribute(USER_SESSION_ACCOUNT_KEY));
	}

	/** {@inheritDoc} */
	@Override
	public void logout() {
		securityManager.getCurrentUserSession()
				.ifPresent(userSession -> userSession.logout());
	}

	private Optional<Account> tryLoginAccount(final AuthenticationToken token) {
		boolean tokenSupported = false;
		for (final AuthenticationPlugin authenticatingRealmPlugin : authenticationPlugins) {
			if (authenticatingRealmPlugin.supports(token)) {
				tokenSupported = true;
				final Optional<String> accountAuthToken = authenticatingRealmPlugin.authenticateAccount(token);
				if (accountAuthToken.isPresent()) {
					return accountManager.getAccountByAuthToken(accountAuthToken.get());
				}
			}
		}
		Assertion.checkState(tokenSupported, "No authenticationPlugin found to support this token ({0}), in plugins ({1})", token.getClass().getSimpleName(), authenticationPlugins);
		return Optional.empty();
	}
}
