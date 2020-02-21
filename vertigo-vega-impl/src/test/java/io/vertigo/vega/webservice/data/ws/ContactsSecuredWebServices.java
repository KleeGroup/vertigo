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

import io.vertigo.account.authorization.annotations.Secured;
import io.vertigo.account.authorization.annotations.SecuredOperation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.lang.VUserException;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Address;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactValidator;
import io.vertigo.vega.webservice.data.domain.ContactView;
import io.vertigo.vega.webservice.data.domain.MandatoryPkValidator;
import io.vertigo.vega.webservice.stereotype.DELETE;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.Validate;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@Secured("SecuredUser")
@PathPrefix("/secured/contacts")
public class ContactsSecuredWebServices implements WebServices {

	@Inject
	private ContactDao contactDao;

	@Secured("Contact$read")
	@POST("/search()")
	public List<Contact> readList(final ContactCriteria listCriteria) {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@Secured("Contact$read")
	@GET("/{conId}")
	public Contact read(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//200
		return contact;
	}

	@Secured("Contact$read")
	@GET("/contactView/{conId}")
	public ContactView readContactView(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//we sheet and use 3 times the same address.
		final DtList<Address> addresses = DtList.of(contact.getAddressAccessor().get(), contact.getAddressAccessor().get(), contact.getAddressAccessor().get());

		final ContactView contactView = new ContactView();
		contactView.setName(contact.getName());
		contactView.setFirstName(contact.getFirstName());
		contactView.setHonorificCode(contact.getHonorificCode());
		contactView.setEmail(contact.getEmail());
		contactView.setBirthday(contact.getBirthday());
		contactView.setAddresses(addresses);
		//200
		return contactView;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactView")
	public ContactView updateContactView(@SecuredOperation("write") final ContactView contactView) {
		//200
		return contactView;
	}

	//@POST is non-indempotent
	@POST("")
	public Contact create(@SecuredOperation("write") final Contact contact) {
		if (contact.getConId() != null) {
			throw new VUserException("Contact #" + contact.getConId() + " already exist");
		}
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException("Name is mandatory");
		}
		contactDao.post(contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/*")
	public Contact update(//
			@Validate({ ContactValidator.class, MandatoryPkValidator.class }) @SecuredOperation("write") final Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}
		if (contact.getConId() == null) {
			throw new VUserException("Id is mandatory");
		}
		contactDao.put(contact);
		//200
		return contact;
	}

	@Secured("Contact$delete")
	@DELETE("/{conId}")
	public void delete(@PathParam("conId") final long conId) {
		if (!contactDao.containsKey(conId)) {
			//404
			throw new VUserException("Contact #" + conId + " unknown");
		}
		if (conId < 5) {
			//401
			throw new VUserException("You don't have enought rights");
		}
		//200
		contactDao.remove(conId);
	}
}
