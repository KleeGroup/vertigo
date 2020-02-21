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
package io.vertigo.studio.data.webservices;

import java.util.Date;
import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.studio.data.domain.Contact;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.WebServices;
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
import io.vertigo.vega.webservice.validation.UiMessageStack;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class SimplerTestWebServices implements WebServices {

	@AnonymousAccessAllowed
	@GET("/login")
	public void login() {
		//code 200
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
		return null;
	}

	@GET("/authentifiedTest")
	public List<Contact> authentifiedTest() {
		return null;
	}

	@Doc("send param type='Confirm' or type = 'Contact' \n Return 'OK' or 'Contact'")
	@GET("/twoResult")
	public UiContext testTwoResult(@QueryParam("type") final String type) {
		return null;
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/{passPhrase}")
	public List<Contact> docTest(@PathParam("passPhrase") final String passPhrase) {
		return null;
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/")
	public List<Contact> docTestEmpty() {
		return null;
	}

	@Doc("Not the same than /docTest/")
	@GET("/docTest")
	public void docTest() {
		//rien
	}

	@GET("/{conId}")
	public Contact testRead(@PathParam("conId") final long conId) {
		return null;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactSyntax")
	public Contact testJsonSyntax(final Contact contact) {
		//200
		return contact;
	}

	//	//@POST is non-indempotent
	//	@POST("/contact")
	//	public Contact createContact( //create POST method -> 201 instead of 200 by convention
	//			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
	//		return null;
	//	}
	//
	//	//PUT is indempotent : ID mandatory
	//	@PUT("/contact")
	//	public Contact testUpdate(
	//			final @Validate({ Contact.class, MandatoryPkValidator.class }) Contact contact) {
	//		return null;
	//	}
	//
	//	//PUT is indempotent : ID mandatory
	//	@PUT("/contact/{conId}")
	//	public Contact testUpdateByPath(@PathParam("conId") final long conId,
	//			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
	//		return null;
	//	}

	@DELETE("/contact/{conId}")
	public void delete(@PathParam("conId") final long conId) {
		//
	}

	@Doc("Test ws multipart body with objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an json of Contact.")
	@POST("/innerbody")
	public List<Contact> testInnerBodyObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Contact contactTo) {
		return null;
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLong")
	public List<Contact> testInnerBodyLong(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		return null;
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ServerSideSave
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLongToDtList")
	public DtList<Contact> testInnerBodyLongToDtList(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		return null;
	}

	@POST("/uiMessage")
	public UiMessageStack testUiMessage(final Contact contact, final UiMessageStack uiMessageStack) {
		return null;
	}

	@POST("/innerBodyValidationErrors")
	public List<Contact> testInnerBodyValidationErrors() {
		return null;
	}

	//	@POST("/saveListDelta")
	//	public String saveListDelta(final @Validate({ ContactValidator.class }) DtListDelta<Contact> myList) {
	//		return null;
	//	}
	//
	//	@POST("/saveDtListContact")
	//	public String saveDtListContact(final @Validate({ ContactValidator.class }) DtList<Contact> myList) {
	//		return null;
	//	}

	@POST("/saveListContact")
	public String saveListContact(final List<Contact> myList) {
		return null;
	}

	@GET("/dtListMeta")
	public DtList<Contact> loadListMeta() {
		return null;
	}

	@GET("/dtListMetaAsList")
	public List<Contact> loadListMetaAsList() {
		return null;
	}

	@GET("/listComplexMeta")
	public DtList<Contact> loadListComplexMeta() {
		return null;
	}

	//	//PUT is indempotent : ID obligatoire
	//	@PUT("/contactAliasName/{conId}")
	//	public Contact testUpdateByPath(@PathParam("conId") final long conId,
	//			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact,
	//			@InnerBodyParam("itsatoolongaliasforfieldcontactname") final String aliasName) {
	//		return null;
	//	}

	@POST("/charset")
	public Contact testCharset(
			final Contact text) {
		return null;
	}

	@GET("/dates")
	public UiContext testDate(@QueryParam("date") final Date date) {
		return null;
	}

	@POST("/string")
	public String testString(final String bodyString) {
		return null;
	}

}
