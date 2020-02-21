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

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 */
public final class UsernameAuthenticationToken implements AuthenticationToken {

	/**
	 * The username
	 */
	private final String username;

	/**
	 * @param username the username submitted for authentication
	 */
	public UsernameAuthenticationToken(final String username) {
		Assertion.checkArgNotEmpty(username);
		//----
		this.username = username;
	}

	/** {@inheritDoc} */
	@Override
	public String getPrincipal() {
		return username;
	}

	/** {@inheritDoc} */
	@Override
	public boolean match(final AuthenticationToken trustedAuthenticationToken) {
		if (trustedAuthenticationToken instanceof UsernameAuthenticationToken) {
			return trustedAuthenticationToken.getPrincipal().equals(username);
		}
		return false;
	}

}
