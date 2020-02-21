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

import javax.inject.Inject;

import io.vertigo.lang.VUserException;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.stereotype.AccessTokenConsume;
import io.vertigo.vega.webservice.stereotype.AccessTokenMandatory;
import io.vertigo.vega.webservice.stereotype.AccessTokenPublish;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/anonymous/test")
public final class AnonymousTestWebServices implements WebServices {

	@Inject
	private ContactDao contactDao;

	@GET("/grantAccess")
	@AccessTokenPublish
	@AnonymousAccessAllowed
	public void testAccessToken() {
		//access token publish
	}

	@GET("/limitedAccess/{conId}")
	@AccessTokenMandatory
	@AnonymousAccessAllowed
	public Contact testAccessToken(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/oneTimeAccess/{conId}")
	@AccessTokenConsume
	@AnonymousAccessAllowed
	public Contact testAccessTokenConsume(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	private Contact testRead(final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//200
		return contact;
	}

}
