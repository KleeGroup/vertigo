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
package io.vertigo.account;

import io.vertigo.account.account.AccountManager;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.identityprovider.IdentityProviderManager;
import io.vertigo.account.impl.account.AccountDefinitionProvider;
import io.vertigo.account.impl.account.AccountManagerImpl;
import io.vertigo.account.impl.authentication.AuthenticationManagerImpl;
import io.vertigo.account.impl.authorization.AuthorizationAspect;
import io.vertigo.account.impl.authorization.AuthorizationManagerImpl;
import io.vertigo.account.impl.identityprovider.IdentityProviderManagerImpl;
import io.vertigo.account.impl.security.VSecurityManagerImpl;
import io.vertigo.account.plugins.account.cache.memory.MemoryAccountCachePlugin;
import io.vertigo.account.plugins.account.cache.redis.RedisAccountCachePlugin;
import io.vertigo.account.plugins.account.store.datastore.StoreAccountStorePlugin;
import io.vertigo.account.plugins.account.store.loader.LoaderAccountStorePlugin;
import io.vertigo.account.plugins.account.store.text.TextAccountStorePlugin;
import io.vertigo.account.plugins.authentication.ldap.LdapAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.mock.MockAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.store.StoreAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.text.TextAuthenticationPlugin;
import io.vertigo.account.plugins.identityprovider.ldap.LdapIdentityProviderPlugin;
import io.vertigo.account.plugins.identityprovider.store.StoreIdentityProviderPlugin;
import io.vertigo.account.plugins.identityprovider.text.TextIdentityProviderPlugin;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;

/**
 * Defines the 'account' extension
 * @author pchretien
 */
public final class AccountFeatures extends Features<AccountFeatures> {

	/**
	 * Constructor.
	 */
	public AccountFeatures() {
		super("account");
	}

	/**
	 * Activates user session.
	 * @param params the user session class name
	 * @return these features
	 */
	@Feature("security")
	public AccountFeatures withSecurity(final Param... params) {
		getModuleConfigBuilder()
				.addComponent(VSecurityManager.class, VSecurityManagerImpl.class, params);
		return this;
	}

	/**
	 * Activates authentication.
	 * @return these features
	 */
	@Feature("authentication")
	public AccountFeatures withAuthentication() {
		getModuleConfigBuilder()
				.addComponent(AuthenticationManager.class, AuthenticationManagerImpl.class);
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("authentication.text")
	public AccountFeatures withTextAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextAuthenticationPlugin.class, params);
		return this;
	}

	/**
	 * Activates store authentication.
	 * @return these features
	 */
	@Feature("authentication.store")
	public AccountFeatures withStoreAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreAuthenticationPlugin.class, params);
		return this;
	}

	/**
	 * Activates ldap authentication.
	 * @return these features
	 */
	@Feature("authentication.ldap")
	public AccountFeatures withLdapAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LdapAuthenticationPlugin.class, params);
		return this;
	}

	/**
	 * Activates mock authentication.
	 * @return these features
	 */
	@Feature("authentication.mock")
	public AccountFeatures withMockAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(MockAuthenticationPlugin.class, params);
		return this;
	}

	@Feature("account")
	public AccountFeatures withAccount() {
		getModuleConfigBuilder()
				.addComponent(AccountManager.class, AccountManagerImpl.class)
				.addDefinitionProvider(AccountDefinitionProvider.class);
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("account.store.store")
	public AccountFeatures withStoreAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreAccountStorePlugin.class, params);
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("account.store.text")
	public AccountFeatures withTextAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextAccountStorePlugin.class, params);
		return this;
	}

	@Feature("account.store.loader")
	public AccountFeatures withLoaderAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LoaderAccountStorePlugin.class, params);
		return this;
	}

	@Feature("account.cache.memory")
	public AccountFeatures withMemoryAccountCache(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(MemoryAccountCachePlugin.class, params);
		return this;
	}

	@Feature("account.cache.redis")
	public AccountFeatures withRedisAccountCache() {
		getModuleConfigBuilder()
				.addPlugin(RedisAccountCachePlugin.class);
		return this;
	}

	@Feature("identityProvider")
	public AccountFeatures withIdentityProvider() {
		getModuleConfigBuilder()
				.addComponent(IdentityProviderManager.class, IdentityProviderManagerImpl.class);
		return this;
	}

	@Feature("identityProvider.store")
	public AccountFeatures withStoreIdentityProvider(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreIdentityProviderPlugin.class, params);
		return this;
	}

	@Feature("identityProvider.ldap")
	public AccountFeatures withLdapIdentityProvider(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LdapIdentityProviderPlugin.class, params);
		return this;
	}

	@Feature("identityProvider.text")
	public AccountFeatures withTextIdentityProvider(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextIdentityProviderPlugin.class, params);
		return this;
	}

	/**
	 * Activates authorization.
	 * @return these features
	 */
	@Feature("authorization")
	public AccountFeatures withAuthorization() {
		getModuleConfigBuilder()
				.addComponent(AuthorizationManager.class, AuthorizationManagerImpl.class)
				.addAspect(AuthorizationAspect.class);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//
	}

}
