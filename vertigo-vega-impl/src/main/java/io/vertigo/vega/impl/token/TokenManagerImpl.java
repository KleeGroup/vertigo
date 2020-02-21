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
package io.vertigo.vega.impl.token;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.kvstore.KVStoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.token.TokenManager;

/**
 * Ui data security token.
 * Keep an security token by user and DtObject.
 *
 * @author npiedeloup
 */
public final class TokenManagerImpl implements TokenManager {
	private static final int UUID_LENGTH = 36;
	private final String collection;
	private final VSecurityManager securityManager;
	/** Object token, by */
	private final KVStoreManager kvStoreManager;

	/**
	 * Constructor.
	 * @param collection Collection's name
	 * @param securityManager Security manager
	 * @param kvStoreManager KV store manager
	 */
	@Inject
	public TokenManagerImpl(
			@ParamValue("collection") final String collection,
			final VSecurityManager securityManager,
			final KVStoreManager kvStoreManager) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(kvStoreManager);
		//-----
		this.collection = collection;
		this.securityManager = securityManager;
		this.kvStoreManager = kvStoreManager;
	}

	//===========================================================================
	//==================Gestion du rendu et des interactions=====================
	//===========================================================================

	/** {@inheritDoc} */
	@Override
	public String put(final Serializable data) {
		Assertion.checkNotNull(data, "Data is mandatory");
		//-----
		final String objectUUID = UUID.randomUUID().toString();
		final String tokenKey = makeTokenKey(objectUUID);
		kvStoreManager.put(collection, tokenKey, data);
		return objectUUID; //We only return the object part.
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Serializable> get(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//-----
		final String tokenKey = makeTokenKey(objectUUID);
		return kvStoreManager.find(collection, tokenKey, Serializable.class);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Serializable> getAndRemove(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//-----
		final String tokenKey = makeTokenKey(objectUUID);
		final Optional<Serializable> result = kvStoreManager.find(collection, tokenKey, Serializable.class);
		if (result.isPresent()) {
			kvStoreManager.remove(collection, tokenKey);
		}
		return result;
	}

	private String makeTokenKey(final String objectUUID) {
		final Optional<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOption.isPresent(), "UserSession is mandatory for security token");
		//-----
		return new StringBuilder(UUID_LENGTH + 1 + UUID_LENGTH)
				.append(getUserTokenPart()).append(":").append(objectUUID)
				.toString();
	}

	private String getUserTokenPart() {
		final Optional<UserSession> userSessionOptional = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOptional.isPresent(), "UserSession is mandatory for security token");
		//-----
		return userSessionOptional.get().getSessionUUID().toString();
	}

}
