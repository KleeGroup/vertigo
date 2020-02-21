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

import java.util.Optional;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.core.component.Plugin;

/**
 * Plugin use for authentification.
 * Inspired by org.apache.shiro.realm.AuthenticatingRealm.
 * @author npiedeloup
 */
public interface AuthenticationPlugin extends Plugin {

	/**
	 * Convenience implementation that returns getAuthenticationTokenClass().isAssignableFrom( token.getClass() );.
	 * Can be overridden by subclasses for more complex token checking.
	 * Most configurations will only need to set a different class via
	 * setAuthenticationTokenClass(java.lang.Class<? extends org.apache.shiro.authc.AuthenticationToken>), as opposed to overriding this method.
	 *
	 * @param token the token being submitted for authentication.
	 * @return true if this authentication realm can process the submitted token instance of the class, false otherwise.
	 */
	boolean supports(AuthenticationToken token);

	/**
	 * @param token the token being submitted for authentication.
	 * @return Validated Principal corresponding to the given token, or Option.empty if authentication fail.
	 */
	Optional<String> authenticateAccount(AuthenticationToken token);

}
