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
package io.vertigo.vega.plugins.webservice.handler;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.locale.MessageText;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Request;
import spark.Response;

/**
 * Security handler.
 * Ensure user is authenticated, throw VSecurityException if not.
 * @author npiedeloup
 */
public final class SecurityWebServiceHandlerPlugin implements WebServiceHandlerPlugin {
	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security Manager
	 */
	@Inject
	public SecurityWebServiceHandlerPlugin(final VSecurityManager securityManager) {
		Assertion.checkNotNull(securityManager);
		//-----
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return webServiceDefinition.isNeedAuthentification();
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		// 2. Check user is authentified
		final Optional<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		if (!userSessionOption.isPresent() || !userSessionOption.get().isAuthenticated()) {
			throw new VSecurityException(MessageText.of("User unauthentified"));
		}
		return chain.handle(request, response, routeContext);
	}
}
