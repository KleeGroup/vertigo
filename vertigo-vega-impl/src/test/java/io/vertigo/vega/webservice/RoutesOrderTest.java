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
package io.vertigo.vega.webservice;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.vega.webservice.data.MyNodeConfig;

public final class RoutesOrderTest {

	private static AutoCloseableApp app;

	static {
		//RestAsssured init
		RestAssured.port = MyNodeConfig.WS_PORT;
	}

	@BeforeAll
	public static void setUp() {
		app = new AutoCloseableApp(MyNodeConfig.config(true));
	}

	@BeforeEach
	public void preTestLogin() {
		RestAssured.registerParser("plain/text", Parser.TEXT);
	}

	@AfterAll
	public static void tearDown() {
		if (app != null) {
			app.close();
		}
	}

	@Test
	public void testRoutesOrder() {
		assertOrder("$", "-", "/", "0", "9", "A", "a", "Z", "z", "_");
		assertOrder("a", "aaa");
		assertOrder("aaa1", "AAA2");
		assertOrder("aa", "aa_");
		assertOrder("aa_", "aa__");
		assertOrder("aaa_", "_aa_");
		assertOrder("aa()", "aa__");
		assertOrder("Post /contacts/view", "Post /contacts/{conId}");
		assertOrder("Post /contacts/view", "Post /contacts/*");
	}

	private void assertOrder(final String... routes) {
		final List<String> myRoutesCode = Arrays.asList(routes);
		final SortedMap<String, String> myRoutesCodeSorted = new TreeMap<>(
				myRoutesCode.stream()
						.collect(Collectors.toMap(p -> normalizePath(p), p -> p)));
		Assertions.assertIterableEquals(myRoutesCode, myRoutesCodeSorted.values());
	}

	private String normalizePath(final String servicePath) {
		//On calcule la taille du path sans le nom des paramètres, c'est util pour trier les routes dans l'ordre d'interception.
		final String argsRemovedPath = servicePath.replaceAll("\\{.*?\\}|\\*", "_");//.*? : reluctant quantifier;

		//On rend le path plus lisible et compatible DefinitionName
		final String hashcodeAsHex = "$" + Integer.toHexString(argsRemovedPath.hashCode());
		//On limite sa taille pour avec un nom de définition acceptable
		return argsRemovedPath.toUpperCase(Locale.ROOT) + hashcodeAsHex;
	}

	@Test
	public void testCatalogOrder() {
		int i = 0;
		RestAssured.given()
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(50)) //actually 62
				.body("get(" + i++ + ")", Matchers.equalTo("Delete /contacts/{conId} (long :Path:conId)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Delete /secured/contacts/{conId} (long :Path:conId)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Delete /test/contact/{conId} (long :Path:conId)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /anonymous/test/grantAccess ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /anonymous/test/limitedAccess/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /anonymous/test/oneTimeAccess/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /catalog () -> interface java.util.List<String>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /contacts () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /contacts/contactView/{conId} (long :Path:conId) -> ContactView"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /contacts/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /login ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /secured/contacts () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /secured/contacts/contactView/{conId} (long :Path:conId) -> ContactView"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /secured/contacts/login ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /secured/contacts/logout ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /secured/contacts/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /swaggerApi (interface javax.servlet.http.HttpServletRequest :Implicit:Request) -> interface java.util.Map<String,Object>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /swaggerUi (interface javax.servlet.http.HttpServletResponse :Implicit:Response)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /swaggerUi/ (interface javax.servlet.http.HttpServletResponse :Implicit:Response)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /swaggerUi/{resourceUrl} (class java.lang.String :Path:resourceUrl, interface javax.servlet.http.HttpServletResponse :Implicit:Response)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /swaggerUi/{resourcePathUrl}/{resourceUrl} (class java.lang.String :Path:resourcePathUrl, class java.lang.String :Path:resourceUrl, interface javax.servlet.http.HttpServletResponse :Implicit:Response)"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/anonymousTest () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/authentifiedTest () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/contactExtended/{conId} (long :Path:conId) -> class io.vertigo.vega.webservice.model.ExtendedObject<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/dates (class java.util.Date :Query:date) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Not the same than /docTest/*/\nGet /test/docTest ()"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Use passPhrase : RtFM*/\nGet /test/docTest/ () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Use passPhrase : RtFM*/\nGet /test/docTest/{passPhrase} (class java.lang.String :Path:passPhrase) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/downloadEmbeddedFile (class java.lang.Integer :Query:id) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/downloadFile (class java.lang.Integer :Query:id) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/downloadFileContentType (class java.lang.Integer :Query:id) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/downloadNotModifiedFile (class java.lang.Integer :Query:id, class java.util.Date :Header:If-Modified-Since, interface javax.servlet.http.HttpServletResponse :Implicit:Response) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/dtList10/{id} (long :Path:id) -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/dtList10elts/{id} (long :Path:id) -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/dtListMeta () -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/dtListMetaAsList () -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/export/pdf/ () -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/export/pdf/{conId} (long :Path:conId) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/filtered/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/grantAccess ()"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Just send x-test-param:\"i'ts fine\"*/\nGet /test/headerParams (class java.lang.String :Header:x-test-param) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/instant () -> Instant"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/limitedAccess/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/listComplexMeta () -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/localDate () -> LocalDate"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/login ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/logout ()"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/oneTimeAccess/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*send param type='Confirm' or type = 'Contact' \n Return 'OK' or 'Contact'*/\nGet /test/twoResult (class java.lang.String :Query:type) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/zonedDateTime () -> ZonedDateTime"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/zonedDateTimeUTC () -> ZonedDateTime"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /test/{conId} (long :Path:conId) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/components () -> NodeConfig"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/components/modules () -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/components/modules/{moduleName} (class java.lang.String :Path:moduleName) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/components/{componentId} (class java.lang.String :Path:componentId) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/definitions () -> DefinitionSpace"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/definitions/types/{definitionType} (class java.lang.String :Path:definitionType) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/definitions/{definitionName} (class java.lang.String :Path:definitionName) -> Definition"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/healthcheck () -> interface java.util.List<HealthCheck>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Get /vertigo/types () -> interface java.util.Collection<class java.lang.Class<? extends io.vertigo.core.definition.Definition>>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /contacts (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /contacts/search() (class io.vertigo.vega.webservice.data.domain.ContactCriteria :Body:[1]) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /search/facetedClusteredResult (class io.vertigo.dynamo.collections.model.SelectedFacetValues :Body:[1]) -> class io.vertigo.dynamo.collections.model.FacetedQueryResult<Contact,class io.vertigo.dynamo.domain.model.DtList<Contact>>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /search/facetedResult (class io.vertigo.dynamo.collections.model.SelectedFacetValues :Body:[1]) -> class io.vertigo.dynamo.collections.model.FacetedQueryResult<Contact,class io.vertigo.dynamo.domain.model.DtList<Contact>>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /search/selectedFacetValues (class io.vertigo.dynamo.collections.model.SelectedFacetValues :Body:[1]) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /secured/contacts (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /secured/contacts/search() (class io.vertigo.vega.webservice.data.domain.ContactCriteria :Body:[1]) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/charset (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/contact (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/contactValidations (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws multipart body with objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an json of Contact.*/\nPost /test/innerbody (class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactFrom, class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactTo) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws multipart body with optional objects. Send a body with an object of to field : contactFrom, Optional<contactTo>. Each one should be an json of Contact.*/\nPost /test/innerbodyOptional (class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactFrom, class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactTo) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws multipart body with serverSide objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an partial json of Contact with clientId.*/\nPost /test/innerBodyServerClient (class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactFrom, class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactTo) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/innerBodyValidationErrors (class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactFrom, class io.vertigo.vega.webservice.data.domain.Contact :InnerBody:contactTo) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.*/\nPost /test/innerLong (long :InnerBody:contactId1, long :InnerBody:contactId2) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.*/\nPost /test/innerLongToDtList (long :InnerBody:contactId1, long :InnerBody:contactId2) -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws with multiple path params.*/\nPost /test/multiPath/from/{conIdFrom}/to/{conIdTo} (long :Path:conIdFrom, long :Path:conIdTo) -> class io.vertigo.dynamo.domain.model.DtList<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/saveDtListContact (io.vertigo.dynamo.domain.model.DtList<io.vertigo.vega.webservice.data.domain.Contact> :Body:[1]) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/saveListContact (java.util.List<io.vertigo.vega.webservice.data.domain.Contact> :Body:[1]) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/saveListDelta (io.vertigo.vega.webservice.model.DtListDelta<io.vertigo.vega.webservice.data.domain.Contact> :Body:[1]) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/saveUiListContact (io.vertigo.vega.webservice.model.UiList<io.vertigo.vega.webservice.data.domain.Contact> :Body:[1], interface io.vertigo.vega.webservice.validation.UiMessageStack :Implicit:UiMessageStack) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/search() (class io.vertigo.vega.webservice.data.domain.ContactCriteria :Body:[1]) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/_searchAutoPagined (class java.lang.String :Query:listServerToken, class io.vertigo.dynamo.domain.model.DtListState :Query:, class io.vertigo.vega.webservice.data.domain.ContactCriteria :Body:[1]) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/searchPagined() (class io.vertigo.vega.webservice.data.domain.ContactCriteria :InnerBody:criteria, class io.vertigo.dynamo.domain.model.DtListState :InnerBody:listState) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/_searchQueryPagined (class io.vertigo.vega.webservice.data.domain.ContactCriteria :Body:[1], class io.vertigo.dynamo.domain.model.DtListState :Query:) -> interface java.util.List<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/string (class java.lang.String :Body:[1]) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/string/optionalInnerBodyParam (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1], class java.lang.String :InnerBody:token) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/string/optionalQueryParam (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1], class java.lang.String :Query:token) -> String"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Test ws returning UiContext. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long. You get partial Contacts with clientId in each one*/\nPost /test/uiContext (long :InnerBody:contactId1, long :InnerBody:contactId2) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/uiMessage (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1], interface io.vertigo.vega.webservice.validation.UiMessageStack :Implicit:UiMessageStack) -> UiMessageStack"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/uploadFile (interface io.vertigo.dynamo.file.model.VFile :Query:upfile, class java.lang.Integer :Query:id, class java.lang.String :Query:note) -> VFile"))
				.body("get(" + i++ + ")", Matchers.equalTo("Post /test/uploadFileFocus (interface io.vertigo.dynamo.file.model.VFile :Query:upfile) -> Integer"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /contacts/contactView (class io.vertigo.vega.webservice.data.domain.ContactView :Body:[1]) -> ContactView"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /contacts/* (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /secured/contacts/contactView (class io.vertigo.vega.webservice.data.domain.ContactView :Body:[1]) -> ContactView"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /secured/contacts/* (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contact (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contact/{conId} (long :Path:conId, class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contactAliasName/{conId} (long :Path:conId, class io.vertigo.vega.webservice.data.domain.Contact :Body:[1], class java.lang.String :InnerBody:itsatoolongaliasforfieldcontactname) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contactExtended/{conId} (long :Path:conId, class io.vertigo.vega.webservice.data.domain.Contact :Body:[1], class [I :InnerBody:vanillaUnsupportedMultipleIds) -> class io.vertigo.vega.webservice.model.ExtendedObject<Contact>"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contactSyntax (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/contactUrl99 (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Exclude conId and name.*/\nPut /test/filtered/* (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo(" /*Only accept firstName and email. Will blocked if other fields are send.*/\nPut /test/filteredInclude/* (class io.vertigo.vega.webservice.data.domain.Contact :Body:[1]) -> Contact"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/instant (class java.time.Instant :Query:date) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/localDate (class java.time.LocalDate :Query:date) -> UiContext"))
				.body("get(" + i++ + ")", Matchers.equalTo("Put /test/zonedDateTime (class java.time.ZonedDateTime :Query:date) -> UiContext"))
				.statusCode(HttpStatus.SC_OK)
				.log().ifValidationFails()
				.when()
				.get("/catalog");
	}

}
