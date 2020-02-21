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
package io.vertigo.vega.webservice.data.ws;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.SessionInvalidate;
import io.vertigo.vega.webservice.stereotype.SessionLess;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/secured/contacts")
public class LoginSecuredWebServices implements WebServices {

	@Inject
	private ContactDao contactDao;

	@Inject
	private VSecurityManager securityManager;
	@Inject
	private AuthorizationManager authorizationManager;

	@AnonymousAccessAllowed
	@GET("/login")
	public void login() {
		//code 200
		final UserSession userSession = securityManager.getCurrentUserSession().get();
		userSession.authenticate();

		final Authorization securedUser = getAuthorization("AtzSecuredUser");
		final Authorization contactWrite = getAuthorization("AtzContact$write");
		final Authorization contactDelete = getAuthorization("AtzContact$delete");
		authorizationManager.obtainUserAuthorizations()
				.withSecurityKeys("name", "Fournier")
				.withSecurityKeys("honorificCode", "MR_")
				.withSecurityKeys("honorificCode", "MRS")
				.addAuthorization(securedUser)
				.addAuthorization(contactWrite)
				.addAuthorization(contactDelete);

	}

	private Authorization getAuthorization(final String authorizationName) {
		final DefinitionSpace definitionSpace = Home.getApp().getDefinitionSpace();
		return definitionSpace.resolve(authorizationName, Authorization.class);
	}

	@SessionInvalidate
	@GET("/logout")
	public void logout() {
		//code 200
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("")
	public List<Contact> readAllList() {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}
}
