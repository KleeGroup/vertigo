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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.lang.VUserException;
import io.vertigo.util.DateUtil;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactValidator;
import io.vertigo.vega.webservice.data.domain.EmptyPkValidator;
import io.vertigo.vega.webservice.data.domain.MandatoryPkValidator;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.UiList;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.DELETE;
import io.vertigo.vega.webservice.stereotype.Doc;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;
import io.vertigo.vega.webservice.stereotype.ServerSideSave;
import io.vertigo.vega.webservice.stereotype.SessionInvalidate;
import io.vertigo.vega.webservice.stereotype.SessionLess;
import io.vertigo.vega.webservice.stereotype.Validate;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class SimplerTestWebServices implements WebServices {

	@Inject
	private VSecurityManager securityManager;
	@Inject
	private ContactDao contactDao;

	@AnonymousAccessAllowed
	@GET("/login")
	public void login() {
		//code 200
		securityManager.getCurrentUserSession().get().authenticate();
	}

	@SessionInvalidate
	@GET("/logout")
	public void logout() {
		//code 200
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/anonymousTest")
	public List<Contact> anonymousTest() {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@GET("/authentifiedTest")
	public List<Contact> authentifiedTest() {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@Doc("send param type='Confirm' or type = 'Contact' \n Return 'OK' or 'Contact'")
	@GET("/twoResult")
	public UiContext testTwoResult(@QueryParam("type") final String type) {
		final UiContext result = new UiContext();
		if ("Confirm".equals(type)) {
			result.put("message", "Are you sure");
		} else {
			result.put("contact", contactDao.get(1L));
		}
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/{passPhrase}")
	public List<Contact> docTest(@PathParam("passPhrase") final String passPhrase) {
		if (!"RtFM".equals(passPhrase)) {
			throw new VSecurityException(MessageText.of("Bad passPhrase, check the doc in /catalog"));
		}
		return contactDao.getList();
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/")
	public List<Contact> docTestEmpty() {
		return docTest(null);
	}

	@Doc("Not the same than /docTest/")
	@GET("/docTest")
	public void docTest() {
		//rien
	}

	@GET("/{conId}")
	public Contact testRead(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactSyntax")
	public Contact testJsonSyntax(final Contact contact) {
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactUrl99")
	public Contact testWsUrl99(final Contact contact) {
		//200
		return contact;
	}

	//@POST is non-indempotent
	@POST("/contact")
	public Contact createContact( //create POST method -> 201 instead of 200 by convention
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException("Name is mandatory");
		}
		contactDao.post(contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID mandatory
	@PUT("/contact")
	public Contact testUpdate(
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID mandatory
	@PUT("/contact/{conId}")
	public Contact testUpdateByPath(
			@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}
		contact.setConId(conId);
		contactDao.put(contact);
		//200
		return contact;
	}

	@DELETE("/contact/{conId}")
	public void delete(@PathParam("conId") final long conId) {
		if (!contactDao.containsKey(conId)) {
			//404
			throw new VUserException("Contact #" + conId + " unknown");
		}
		if (conId < 5) {
			//401
			throw new VSecurityException(MessageText.of("You don't have enought rights"));
		}
		//200
		contactDao.remove(conId);
	}

	@Doc("Test ws multipart body with objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an json of Contact.")
	@POST("/innerbody")
	public List<Contact> testInnerBodyObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Contact contactTo) {
		//offset + range ?
		//code 200
		return Arrays.asList(contactFrom, contactTo);
	}

	@Doc("Test ws multipart body with optional objects. Send a body with an object of to field : contactFrom, Optional<contactTo>. Each one should be an json of Contact.")
	@POST("/innerbodyOptional")
	public List<Contact> testInnerBodyOptionalObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Optional<Contact> contactToOpt) {
		final List<Contact> result = new ArrayList<>(2);
		result.add(contactFrom);
		contactToOpt.ifPresent(contactTo -> result.add(contactTo));
		return result;
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLong")
	public List<Contact> testInnerBodyLong(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		//offset + range ?
		//code 200
		return Arrays.asList(
				contactDao.get(contactIdFrom),
				contactDao.get(contactIdTo));
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ServerSideSave
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLongToDtList")
	public DtList<Contact> testInnerBodyLongToDtList(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		return DtList.of(
				contactDao.get(contactIdFrom),
				contactDao.get(contactIdTo));
		//offset + range ?
		//code 200
	}

	@POST("/uiMessage")
	public UiMessageStack testUiMessage(final Contact contact, final UiMessageStack uiMessageStack) {
		uiMessageStack.success("Your message have been received");
		uiMessageStack.info("We can complete messageStack : globaly or field by field");
		uiMessageStack.warning("This field must be read twice !!", contact, "birthday");
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return uiMessageStack;
	}

	@POST("/innerBodyValidationErrors")
	public List<Contact> testInnerBodyValidationErrors(//
			@InnerBodyParam("contactFrom") final Contact contactFrom, //
			@InnerBodyParam("contactTo") final Contact contactTo) {
		//offset + range ?
		//code 200
		if (contactFrom != null) {
			throw new ValidationUserException(MessageText.of("Process validation error"), contactFrom, "firstName");
		}
		return Collections.emptyList();
	}

	@POST("/saveListDelta")
	public String saveListDelta(final @Validate({ ContactValidator.class }) DtListDelta<Contact> myList) {
		return "OK : add " + myList.getCreated().size() + " contacts, update " + myList.getUpdated().size() + " contacts, removed " + myList.getDeleted().size();
	}

	@POST("/saveDtListContact")
	public String saveDtListContact(final @Validate({ ContactValidator.class }) DtList<Contact> myList) {
		for (final Contact contact : myList) {
			if (contact.getName() == null || contact.getName().isEmpty()) {
				//400
				throw new VUserException("Name is mandatory");
			}
		}
		return "OK : received " + myList.size() + " contacts";
	}

	@POST("/saveUiListContact")
	public String saveUiListContact(final UiList<Contact> myList, final UiMessageStack uiMessageStack) {
		myList.mergeAndCheckInput(Collections.singletonList(new ContactValidator()), uiMessageStack);
		for (final UiObject<Contact> contact : myList) {
			if (contact.getString("name") == null || contact.getString("name").isEmpty()) {
				//400
				throw new VUserException("Name is mandatory");
			}
		}
		return "OK : received " + myList.size() + " contacts";
	}

	@POST("/saveListContact")
	public String saveListContact(final List<Contact> myList) {
		return "OK : received " + myList.size() + " contacts";
	}

	@GET("/dtListMeta")
	public DtList<Contact> loadListMeta() {
		final DtList<Contact> result = new DtList<>(Contact.class);
		for (final Contact contact : contactDao.getList()) {
			result.add(contact);
		}
		result.setMetaData("testLong", 12);
		result.setMetaData("testString", "the String test");
		result.setMetaData("testDate", DateUtil.newDate());
		result.setMetaData("testEscapedString", "the EscapedString \",} test");
		return result;
	}

	@GET("/dtList10/{id}")
	public DtList<Contact> loadListDigitInRoute(@PathParam("id") final long conId) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		for (final Contact contact : contactDao.getList()) {
			result.add(contact);
		}
		return (DtList<Contact>) result.subList(0, 10);
	}

	@GET("/dtList10elts/{id}")
	public DtList<Contact> loadListDigitInRoute2(@PathParam("id") final long conId) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		for (final Contact contact : contactDao.getList()) {
			result.add(contact);
		}
		return (DtList<Contact>) result.subList(0, 10);
	}

	@GET("/dtListMetaAsList")
	public List<Contact> loadListMetaAsList() {
		return loadListMeta();
	}

	@GET("/listComplexMeta")
	public DtList<Contact> loadListComplexMeta() {
		final DtList<Contact> result = loadListMeta();
		result.setMetaData("testLong", 12);
		result.setMetaData("testString", "the String test");
		result.setMetaData("testDate", DateUtil.newDate());
		result.setMetaData("testEscapedString", "the EscapedString \",} test");
		result.setMetaData("contact1", result.get(1));
		result.setMetaData("contact2", result.get(2));
		return result;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactAliasName/{conId}")
	public Contact testUpdateByPath(
			@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact,
			@InnerBodyParam("itsatoolongaliasforfieldcontactname") final String aliasName) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}
		contact.setConId(conId);
		contact.setName(aliasName);
		contactDao.put(contact);
		//200
		return contact;
	}

	@POST("/charset")
	public Contact testCharset(
			final Contact text) {
		//200
		return text;
	}

	@GET("/dates")
	public UiContext testDate(@QueryParam("date") final Date date) {
		final UiContext result = new UiContext();
		result.put("input", date);
		result.put("inputAsString", date.toString());
		return result;
	}

	@GET("/localDate")
	public LocalDate getLocalDate() {
		return LocalDate.of(2017, 6, 27);
	}

	@PUT("/localDate")
	public UiContext putLocalDate(@QueryParam("date") final LocalDate localDate) {
		final UiContext result = new UiContext();
		result.put("input", localDate);
		result.put("inputAsString", localDate.toString());
		return result;
	}

	@GET("/zonedDateTime")
	public ZonedDateTime getZonedDateTime() {
		return ZonedDateTime.of(2016, 5, 26, 23, 30, 20, 0, ZoneId.of("CET"));
	}

	@GET("/zonedDateTimeUTC")
	public ZonedDateTime getZonedDateTimeUTC() {
		return ZonedDateTime.of(2016, 7, 28, 0, 0, 0, 0, ZoneId.of("CET"));
	}

	@PUT("/zonedDateTime")
	public UiContext putZonedDateTime(@QueryParam("date") final ZonedDateTime zonedDateTime) {
		final UiContext result = new UiContext();
		result.put("input", zonedDateTime);
		result.put("inputAsString", zonedDateTime.toString());
		return result;
	}

	@GET("/instant")
	public Instant getInstant() {
		return LocalDateTime.of(2016, 5, 26, 21, 30, 20, 0).toInstant(ZoneOffset.UTC);
	}

	@PUT("/instant")
	public UiContext putInstant(@QueryParam("date") final Instant instant) {
		final UiContext result = new UiContext();
		result.put("input", instant);
		result.put("inputAsString", instant.toString());
		return result;
	}

	@POST("/string")
	public String testString(final String bodyString) {
		return bodyString;
	}

	@POST("/string/optionalInnerBodyParam")
	public String testOptionalInnerBodyParam(final Contact contact, @InnerBodyParam("token") final Optional<String> token) {
		return token.orElse("empty");
	}

	@POST("/string/optionalQueryParam")
	public String testOptionalQueryParam(final Contact contact, @QueryParam("token") final Optional<String> token) {
		return token.orElse("empty");
	}

	/*@GET("/searchFacet")
	public FacetedQueryResult<DtObject, ContactCriteria> testSearchServiceFaceted(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());
		//offset + range ?
		//code 200
		return result;
	}*/

}
