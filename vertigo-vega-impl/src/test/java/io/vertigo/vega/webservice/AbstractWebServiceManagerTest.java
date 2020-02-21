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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.MapBuilder;

abstract class AbstractWebServiceManagerTest {
	private static final String HEADER_ACCESS_TOKEN = "x-access-token";
	private static final String UTF8_TEST_STRING = "? TM™ éè'`àöêõù Euro€ R®@©∆∏∑∞⅓۲²³œβ";

	private final SessionFilter loggedSessionFilter = new SessionFilter();
	private final SessionFilter anonymousSessionFilter = new SessionFilter();

	@BeforeEach
	public void preTestLogin() {
		RestAssured.registerParser("plain/text", Parser.TEXT);
		RestAssured.given()
				.filter(loggedSessionFilter)
				.get("/test/login");
	}

	@Test
	public void testCatalog() {
		RestAssured.given()
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(50)) //actually 62
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/catalog");
	}

	@Test
	public void testSwaggerApi() {
		RestAssured.given()
				.expect()
				.body("swagger", Matchers.equalTo("2.0"))
				.body("info", Matchers.notNullValue())
				.body("info.size()", Matchers.equalTo(3))
				.body("basePath", Matchers.anything()) //can be null
				.body("paths", Matchers.notNullValue())
				.body("paths.size()", Matchers.greaterThanOrEqualTo(50)) //actually 57
				.body("definitions", Matchers.notNullValue())
				.body("definitions.size()", Matchers.greaterThanOrEqualTo(30)) //actually 37
				.statusCode(HttpStatus.SC_OK)
				.log().ifValidationFails()
				.when()
				.get("/swaggerApi");
	}

	private static void assertStatusCode(final int expectedStatusCode, final String path) {
		RestAssured
				.given()
				.expect()
				.statusCode(expectedStatusCode)
				.when()
				.get(path);
	}

	@Test
	public void testSwaggerUi() {
		assertStatusCode(HttpStatus.SC_OK, "/swaggerUi");
		assertStatusCode(HttpStatus.SC_OK, "/swaggerUi/swagger-ui.js");
		assertStatusCode(HttpStatus.SC_OK, "/swaggerUi/swagger-ui.css");
		assertStatusCode(HttpStatus.SC_OK, "/swaggerUi/swagger-ui-bundle.js");
		assertStatusCode(HttpStatus.SC_OK, "/swaggerUi/swagger-ui-standalone-preset.js");

		assertStatusCode(HttpStatus.SC_NOT_FOUND, "/swaggerUi/test404.mp4");
	}

	@Test
	public void testLogout() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.get("/test/logout");
	}

	@Test
	public void testLogin() {
		assertStatusCode(HttpStatus.SC_NO_CONTENT, "/test/login");
	}

	@Test
	public void testAnonymousTest() {
		assertStatusCode(HttpStatus.SC_OK, "/test/anonymousTest");
	}

	@Test
	public void testAuthentifiedTest() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.when()
				.get("/test/authentifiedTest");
	}

	@Test
	public void testUnauthentifiedTest() {
		assertStatusCode(HttpStatus.SC_UNAUTHORIZED, "/test/authentifiedTest");
	}

	@Test
	public void testTwoResultConfirm() {
		loggedAndExpect(given().param("type", "Confirm"))
				.statusCode(HttpStatus.SC_OK)
				.body("message", Matchers.equalTo("Are you sure"))
				.when()
				.get("/test/twoResult");
	}

	@Test
	public void testTwoResultContact() {
		loggedAndExpect(given().param("type", "Contact"))
				.statusCode(HttpStatus.SC_OK)
				.body("contact.conId", Matchers.equalTo(1))
				.when()
				.get("/test/twoResult");
	}

	@Test
	public void docTest1() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.when()
				.get("/test/docTest/RtFM");
	}

	@Test
	public void docTest2() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/docTest/myPass");
	}

	@Test
	public void docTest3() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/docTest/");
	}

	@Test
	public void testRead1() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(2))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/2");
	}

	@Test
	public void testRead2() {
		loggedAndExpect()
				.body("globalErrors", Matchers.contains("Contact #30 unknown"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.get("/test/30");
	}

	@Test
	public void testExportContacts() {
		loggedAndExpect()
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"contacts.pdf\";filename*=UTF-8''contacts.pdf"))
				// greaterThanOrEqualTo because it depends of previously inserted elements
				.header("Content-Length", Matchers.greaterThanOrEqualTo("2572"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/export/pdf/");
	}

	@Test
	public void testExportOneContact() {
		loggedAndExpect()
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"contact2.pdf\";filename*=UTF-8''contact2.pdf"))
				.header("Content-Length", Matchers.anyOf(Matchers.equalTo("1157"), Matchers.equalTo("1703")))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/export/pdf/2");
	}

	@Test
	public void testGrantAccessToken() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.header(HEADER_ACCESS_TOKEN, Matchers.notNullValue())
				.when()
				.get("/test/grantAccess");
	}

	@Test
	public void testNoAccessToken() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testBadAccessToken() {
		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, "badToken"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testLimitedAccessToken() {
		final String headerAccessToken = given()
				.filter(loggedSessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testLimitedAccessToken2() {
		final String headerAccessToken = given()
				.filter(loggedSessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testAccessTokenConsume() {
		final String headerAccessToken = given()
				.filter(loggedSessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/oneTimeAccess/1");
	}

	@Test
	public void testAccessTokenConsume2() {
		final String headerAccessToken = given()
				.filter(loggedSessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/oneTimeAccess/1");

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/oneTimeAccess/1");
	}

	@Test
	public void testFilteredRead() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(1))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.nullValue()) //excluded field
				.body("email", Matchers.nullValue()) //excluded field
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/filtered/1");
	}

	@Test
	public void testAnonymousGrantAccessToken() {
		RestAssured.given()
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.header(HEADER_ACCESS_TOKEN, Matchers.notNullValue())
				.when()
				.get("/anonymous/test/grantAccess");
	}

	@Test
	public void testAnonymousNoAccessToken() {
		RestAssured.given()
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.statusCode(HttpStatus.SC_UNAUTHORIZED)
				.when()
				.get("/anonymous/test/limitedAccess/3");
	}

	@Test
	public void testAnonymousBadAccessToken() {
		given().header(HEADER_ACCESS_TOKEN, "badToken")
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.statusCode(HttpStatus.SC_UNAUTHORIZED)
				.when()
				.get("/anonymous/test/limitedAccess/3");
	}

	@Test
	public void testAnonymousLimitedAccessToken() {
		final String headerAccessToken = given()
				.filter(anonymousSessionFilter)
				.get("/anonymous/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/anonymous/test/limitedAccess/3");
	}

	@Test
	public void testAnonymousLimitedAccessToken2() {
		final String headerAccessToken = given()
				.filter(anonymousSessionFilter)
				.get("/anonymous/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/anonymous/test/limitedAccess/3");

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/anonymous/test/limitedAccess/3");
	}

	@Test
	public void testAnonymousAccessTokenConsume() {
		final String headerAccessToken = given()
				.filter(anonymousSessionFilter)
				.get("/anonymous/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/anonymous/test/oneTimeAccess/1");
	}

	@Test
	public void testAnonymousAccessTokenConsume2() {
		final String headerAccessToken = given()
				.filter(anonymousSessionFilter)
				.get("/anonymous/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/anonymous/test/oneTimeAccess/1");

		given().header(HEADER_ACCESS_TOKEN, headerAccessToken)
				.filter(anonymousSessionFilter)
				.expect().log().ifValidationFails()
				.statusCode(HttpStatus.SC_UNAUTHORIZED) //401 : Unauthorized means unauthenticated
				.when()
				.get("/anonymous/test/oneTimeAccess/1");
	}

	@Test
	public void testPostContact() {
		doCreateContact();
	}

	private Map<String, Object> doCreateContact() {
		final Map<String, Object> newContact = createDefaultContact(null);

		final Long conId = loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_CREATED)
				.when()
				.post("/test/contact")
				.body().path("conId");
		newContact.put("conId", conId);
		return newContact;
	}

	@Test
	public void testPostContactValidatorError() {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id must not be set"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");

		final Map<String, Object> new2Contact = createDefaultContact(null);
		new2Contact.put("birthday", "2012-10-24");

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");
	}

	@Test
	public void testPostContactUserException() {
		final Map<String, Object> newContact = createDefaultContact(null);
		newContact.put("name", null);

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");
	}

	@Test
	public void testJsonSyntaxError() {
		final String[] testOkJson = { "{ \"firstName\" : \"test\" }", "{ firstName : \"test\" }", "{ 'firstName' : \"test\" }", "{\n\t\"firstName\" : \"test\"\n}" };
		final String[] testBadInterpretedJson = { "{ \"firstName\" : test\" }" };
		final String[] testBadSyntaxJson = { " \"firstName\" : \"test\" }", "{ \"firstName : \"test\" }", "{ firstName\" : \"test\" }",
				"{ \"firstName\"  \"test\" }", "{ \"firstName\" : \"test }", "{ \"firstName\" : \"test\" " };

		for (final String testJson : testOkJson) {
			loggedAndExpect(given().body(testJson))
					.body("firstName", Matchers.equalTo("test"))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.put("/test/contactSyntax");
		}

		for (final String testJson : testBadInterpretedJson) {
			loggedAndExpect(given().body(testJson))
					.body("firstName", Matchers.equalTo("test\""))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.put("/test/contactSyntax");
		}

		for (final String testJson : testBadSyntaxJson) {
			loggedAndExpect(given().body(testJson))
					.body("globalErrors", Matchers.contains("Error parsing param :Body:[1] on service Put /test/contactSyntax"))
					.statusCode(HttpStatus.SC_BAD_REQUEST)
					.when()
					.put("/test/contactSyntax");
		}
	}

	@Test
	public void testWsUrlError() {
		final String[] testOkJson = { "{ \"firstName\" : \"test\" }", "{ firstName : \"test\" }", "{ 'firstName' : \"test\" }", "{\n\t\"firstName\" : \"test\"\n}" };
		for (final String testJson : testOkJson) {
			loggedAndExpect(given().body(testJson))
					.body("firstName", Matchers.equalTo("test"))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.put("/test/contactUrl99");
		}
	}

	@Test
	public void testPutContact() {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(100))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPostContactValidations() {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.birthday", Matchers.contains("Test birthday after birthday")) //manual validation by Vega
				.body("fieldErrors.email", Matchers.contains("Test error : email")) //manual validation by Vega
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contactValidations")
				.body().path("conId");
	}

	@Test
	public void testPutContactVAccessor() {
		final Map<String, Object> newContact = createDefaultContact(100L);
		newContact.put("adrId", 200);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(100))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.body("adrId", Matchers.equalTo(200))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactEmptyField() {
		final Map<String, Object> newContact = createDefaultContact(100L);
		newContact.put("name", "");

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.name", Matchers.contains("Le champ doit être renseigné")) //autovalidation by Vega
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");

		//----

		final Map<String, Object> newContact2 = createDefaultContact(100L);
		newContact2.put("name", null);

		loggedAndExpect(given().body(newContact2))
				.body("globalErrors", Matchers.contains("Name is mandatory")) //WS manual validation
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");

		//----

		final Map<String, Object> newContact3 = createDefaultContact(100L);
		newContact3.remove("name");

		loggedAndExpect(given().body(newContact3))
				.body("globalErrors", Matchers.contains("Name is mandatory")) //WS manual validation
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testGetContactView() {
		loggedAndExpect()
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("addresses.size()", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/contacts/contactView/1");
	}

	@Test
	public void testPutContactView() {
		final Map<String, Object> newContactView = createDefaultContact(100L);

		final List<Map<String, Object>> addresses = new ListBuilder<Map<String, Object>>()
				.add(createAddress(10L, "10, avenue Claude Vellefaux", "", "Paris", "75010", "France"))
				.add(createAddress(24L, "24, avenue General De Gaulle", "", "Paris", "75001", "France"))
				.add(createAddress(38L, "38, impasse des puits", "", "Versaille", "78000", "France"))
				.build();

		newContactView.remove("address");
		newContactView.put("addresses", addresses);

		loggedAndExpect(given().body(newContactView))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.body("addresses.size()", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/contacts/contactView");
	}

	@Disabled("Not supported yet")
	@Test
	public void testPutContactViewError() {
		final Map<String, Object> newContactView = createDefaultContact(100L);
		final List<Map<String, Object>> addresses = new ListBuilder<Map<String, Object>>()
				.add(createAddress(10L, "10, avenue Claude Vellefaux", "", "Paris", "75010", "France"))
				.add(createAddress(24L, "24, avenue General De Gaulle", "", "Paris", "75001", "France"))
				.add(createAddress(38L, "38, impasse des puits -- too long -- overrided DO_TEXT_50 length constraint -- too long -- too long", "", "Versaille", "78000", "France"))
				.build();

		newContactView.remove("address");
		newContactView.put("addresses", addresses);

		loggedAndExpect(given().body(newContactView))
				.body("fieldErrors", Matchers.notNullValue())
				//.body("fieldErrors.addresses[2]", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/contacts/contactView");
	}

	@Test
	public void testPutContactByPath() {
		final Map<String, Object> newContact = createDefaultContact(null);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(101))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testPutContactValidatorError() {
		final Map<String, Object> newContact = createDefaultContact(null);

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");

		final Map<String, Object> new2Contact = createDefaultContact(100L);
		new2Contact.put("birthday", "2012-10-24");

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactByPathValidatorError() {
		final Map<String, Object> new2Contact = createDefaultContact(null);
		new2Contact.put("birthday", "2012-10-24");

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testPutContactUserException() {
		final Map<String, Object> newContact = createDefaultContact(100L);
		newContact.remove("name");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactByPathUserException() {
		final Map<String, Object> newContact = createDefaultContact(null);
		newContact.remove("name");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testDeleteContact() {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/105");

		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.delete("/test/contact/105");
	}

	@Test
	public void testDeleteContactErrors() {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.delete("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Contact #106 unknown"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.delete("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("You don't have enought rights"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.delete("/test/contact/2");
	}

	@Test
	public void testPostInnerBodyObject() {
		final Map<String, String> contactFrom = given().filter(loggedSessionFilter)
				.when().get("/test/5")
				.body().as(Map.class);

		final Map<String, String> contactTo = given().filter(loggedSessionFilter)
				.when().get("/test/6")
				.body().as(Map.class);

		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactFrom", contactFrom)
				.put("contactTo", contactTo)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(2))
				.body("get(0).conId", Matchers.equalTo(5))
				.body("get(0).firstName", Matchers.notNullValue())
				.body("get(1).conId", Matchers.equalTo(6))
				.body("get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerbody");
	}

	@Test
	public void testPostInnerBodyObjectFieldErrors() {
		final Map<String, String> contactFrom = given().filter(loggedSessionFilter)
				.when().get("/test/5")
				.body().as(Map.class);
		contactFrom.put("email", "notAnEmail");

		final Map<String, String> contactTo = given().filter(loggedSessionFilter)
				.when().get("/test/6")
				.body().as(Map.class);
		contactTo.put("firstName", "MoreThan50CharactersIsTooLongForAFirstNameInThisTestApi");

		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactFrom", contactFrom)
				.put("contactTo", contactTo)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("objectFieldErrors.contactFrom.email", Matchers.contains("Le courriel n'est pas valide"))
				.body("objectFieldErrors.contactTo.firstName", Matchers.contains("<<fr:DYNAMO_CONSTRAINT_STRINGLENGTH_EXCEEDED[50]>>"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/innerbody");
	}

	@Test
	public void testPostInnerBodyOptionalPresentObject() {
		final Map<String, String> contactFrom = given().filter(loggedSessionFilter)
				.when().get("/test/5")
				.body().as(Map.class);

		final Map<String, String> contactTo = given().filter(loggedSessionFilter)
				.when().get("/test/6")
				.body().as(Map.class);

		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactFrom", contactFrom)
				.put("contactTo", contactTo)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(2))
				.body("get(0).conId", Matchers.equalTo(5))
				.body("get(0).firstName", Matchers.notNullValue())
				.body("get(1).conId", Matchers.equalTo(6))
				.body("get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerbodyOptional");
	}

	@Test
	public void testPostInnerBodyOptionalEmptyObject() {
		final Map<String, String> contactFrom = given().filter(loggedSessionFilter)
				.when().get("/test/5")
				.body().as(Map.class);

		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactFrom", contactFrom)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(1))
				.body("get(0).conId", Matchers.equalTo(5))
				.body("get(0).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerbodyOptional");
	}

	@Test
	public void testPostInnerBodyValidationErrors() {
		final Map<String, Object> contactFrom = createDefaultContact(140L);
		final Map<String, Object> contactTo = createDefaultContact(141L);

		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactFrom", contactFrom)
				.put("contactTo", contactTo)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("objectFieldErrors.contactFrom.firstName", Matchers.contains("Process validation error"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/innerBodyValidationErrors");
	}

	@Test
	public void testPostInnerBodyLong() {
		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactId1", 6)
				.put("contactId2", 7)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(2))
				.body("get(0).conId", Matchers.equalTo(6))
				.body("get(0).firstName", Matchers.notNullValue())
				.body("get(1).conId", Matchers.equalTo(7))
				.body("get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerLong");
	}

	@Test
	public void testInnerBodyLongToDtList() {
		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactId1", 6)
				.put("contactId2", 7)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("serverToken", Matchers.notNullValue())
				.body("value.size()", Matchers.equalTo(2))
				.body("value.get(0).conId", Matchers.equalTo(6))
				.body("value.get(0).firstName", Matchers.notNullValue())
				.body("value.get(1).conId", Matchers.equalTo(7))
				.body("value.get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerLongToDtList");
	}

	@Test
	public void testUiContext() {
		final Map<String, Object> fullBody = new MapBuilder<String, Object>()
				.put("contactId1", 6)
				.put("contactId2", 7)
				.build();

		loggedAndExpect(given().body(fullBody))
				.body("contactFrom.name", Matchers.equalTo("Leroy"))
				.body("contactFrom.serverToken", Matchers.notNullValue())
				.body("contactTo.name", Matchers.equalTo("Moreau"))
				.body("contactTo.serverToken", Matchers.notNullValue())
				.body("testLong", Matchers.equalTo(12))
				.body("testString", Matchers.equalTo("the String test"))
				//.body("testDate", Matchers.any(Date.class)) //get a string no directly a Date can't ensured
				.body("testEscapedString", Matchers.equalTo("the EscapedString \",} test"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/uiContext");
	}

	@Test
	public void testFilteredUpdateByExclude() {
		final Map<String, Object> contact = doGetServerSideObject();

		final Long oldConId = (Long) contact.get("conId");
		final String oldName = (String) contact.get("name");
		final String oldFirstName = (String) contact.get("firstName");
		final String oldEmail = (String) contact.get("email");
		final String newFirstName = oldFirstName + "FNTest";
		final String newEmail = "ETest." + oldEmail;

		contact.remove("conId"); //can't modify conId
		contact.remove("name"); //can't modify name
		//contact.put("conId", 1000L);
		//contact.put("name", newName);
		contact.put("firstName", newFirstName);
		contact.put("email", newEmail);

		loggedAndExpect(given().body(contact))
				.body("conId", Matchers.equalTo(oldConId))//not changed
				.body("honorificCode", Matchers.equalTo(contact.get("honorificCode")))
				.body("name", Matchers.equalTo(oldName)) //not changed
				.body("firstName", Matchers.equalTo(newFirstName))
				.body("birthday", Matchers.equalTo(contact.get("birthday")))
				.body("email", Matchers.equalTo(newEmail))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testFilteredUpdateByExcludeErrors() {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		contact.put("conId", 1000L);//can't modify conId
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.put("conId", 1000L);//can't modify conId
		contact.remove("name"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.remove("conId");//can't modify conId
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.remove("conId");//can't modify conId
		contact.remove("name"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testFilteredUpdateByInclude() {
		final Map<String, Object> contact = doGetServerSideObject();

		final Long oldConId = (Long) contact.get("conId");
		final String oldName = (String) contact.get("name");
		final String oldFirstName = (String) contact.get("firstName");
		final String oldEmail = (String) contact.get("email");
		final String oldHonorificCode = (String) contact.get("honorificCode");
		final String oldBirthday = (String) contact.get("birthday");
		final String newFirstName = oldFirstName + "FNTest";
		final String newEmail = "ETest." + oldEmail;

		contact.remove("conId"); //can't modify conId
		contact.remove("name"); //can't modify name
		contact.put("firstName", newFirstName);
		contact.put("email", newEmail);
		contact.remove("honorificCode"); //can't modify conId
		contact.remove("birthday"); //can't modify name

		loggedAndExpect(given().body(contact))
				.body("conId", Matchers.equalTo(oldConId))//not changed
				.body("honorificCode", Matchers.equalTo(oldHonorificCode)) //not changed
				.body("name", Matchers.equalTo(oldName)) //not changed
				.body("firstName", Matchers.equalTo(newFirstName))// changed
				.body("birthday", Matchers.equalTo(oldBirthday))//not changed
				.body("email", Matchers.equalTo(newEmail))// changed
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

	}

	@Test
	public void testFilteredUpdateByIncludeErrors() {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.put("conId", 1000L); //can't modify conId
		contact.remove("name"); //can't modify name
		contact.remove("firstName");
		contact.remove("email");
		contact.remove("honorificCode"); //can't modify conId
		contact.remove("birthday"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("conId");//can't modify conId
		contact.put("firstName", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("firstName");
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("name"); //can't modify name
		contact.put("email", "test@test.com");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.put("honorificCode", "test"); //can't modify honorificCode
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("honorificCode"); //can't modify honorificCode
		contact.put("birthday", "1985-10-24"); //can't modify birthday
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("birthday"); //can't modify honorificCode
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);
	}

	@Test
	public void testFilteredUpdateServerTokenErrors() {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		contact.remove("conId");//can't modify conId
		contact.remove("name"); //can't modify name

		contact.put("serverToken", "badToken");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.remove("serverToken");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testPutContactTooLongField() {
		final Map<String, Object> newContact = createDefaultContact(null);
		final String newNameValue = "Here i am !!";
		newContact.put("itsatoolongaliasforfieldcontactname", newNameValue);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(101))
				.body("name", Matchers.equalTo(newNameValue))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contactAliasName/101");
	}

	@Test
	public void testGetContactExtended() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(1))
				.body("vanillaUnsupportedMultipleIds", Matchers.iterableWithSize(3))
				.body("vanillaUnsupportedMultipleIds", Matchers.hasItems(1, 2, 3))
				.header("content-type", Matchers.containsString("json+entity=Contact+meta"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/contactExtended/1");
	}

	@Test
	public void testPutContactExtended() {
		final Map<String, Object> newContact = createDefaultContact(103L);
		newContact.remove("conId");
		newContact.put("vanillaUnsupportedMultipleIds", new int[] { 3, 4, 5 });

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(103))
				.body("vanillaUnsupportedMultipleIds", Matchers.iterableWithSize(3))
				.body("vanillaUnsupportedMultipleIds", Matchers.hasItems(3, 4, 5))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contactExtended/103");
	}

	@Test
	public void testPutContactExtendedValidator() {
		final Map<String, Object> newContact = createDefaultContact(104L);
		newContact.put("vanillaUnsupportedMultipleIds", new int[] { 3, 4, 5 });

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id must not be set"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contactExtended/104");

		newContact.remove("conId");
		newContact.put("birthday", "2012-10-24");
		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contactExtended/104");
	}

	@Test
	public void testPostCharset() throws UnsupportedEncodingException {
		final String testFirstName = "Gérard";
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";
		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as UTF8
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=ISO-8859-1")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as ISO-8859-1
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=ISO-8859-1")
				.body(testJson.getBytes("ISO-8859-1")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPostCharsetUtf8() throws UnsupportedEncodingException {
		final String testFirstName = UTF8_TEST_STRING;
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";
		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as UTF8
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPostCharsetDefaultUtf8() throws UnsupportedEncodingException {
		final String testFirstName = UTF8_TEST_STRING;
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset") //We precise an incomplete charset otherwise Restassured add a default charset=ISO-8859-1 to contentType
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPutContactCharsetIso8859() throws UnsupportedEncodingException {
		final String testedCharset = "ISO-8859-1";
		final String testFirstName = UTF8_TEST_STRING;
		final String testFirstNameIso = new String(UTF8_TEST_STRING.getBytes(testedCharset), testedCharset);
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=" + testedCharset)
				.body(Collections.singletonMap("firstName", testFirstName))
				.expect()
				.body("firstName", Matchers.equalTo(testFirstNameIso))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(loggedSessionFilter) //logged
				.contentType("application/json;charset=" + testedCharset)
				.body(testJson.getBytes(testedCharset))
				.expect()
				.body("firstName", Matchers.equalTo(testFirstNameIso))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testSearchQueryPagined() {
		doTestSearchPagined(false);
	}

	@Test
	public void testSearchAutoPagined() {
		doTestSearchPagined(true);
	}

	private void doTestSearchPagined(final boolean isAuto) {
		final Map<String, Object> criteriaContact = new MapBuilder<String, Object>()
				.put("birthdayMin", "1978-05-19")
				.put("birthdayMax", "1985-05-19")
				.build();

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto); //if Fournier : dateCriteria not applied
		doPaginedSearch(criteriaContact, 3, 2, "name", false, serverSideToken, 3, "Garcia", "Moreau", isAuto);
		doPaginedSearch(criteriaContact, 3, 5, "name", false, serverSideToken, 1, "Petit", "Petit", isAuto);
		doPaginedSearch(criteriaContact, 10, 10, "name", false, serverSideToken, 0, "Petit", "Petit", isAuto);
	}

	@Test
	public void testSearchQueryPaginedSortName() {
		doTestSearchPaginedSortName(false);
	}

	@Test
	public void testSearchAutoPaginedSortName() {
		doTestSearchPaginedSortName(true);
	}

	private void doTestSearchPaginedSortName(final boolean isAuto) {
		final Map<String, Object> criteriaContact = new MapBuilder<String, Object>()
				.put("birthdayMin", "1978-05-19")
				.put("birthdayMax", "1985-05-19")
				.build();
		//gets : "Dubois","Durant","Garcia","Martin","Moreau","Petit"

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 10, 0, "name", false, serverSideToken, 6, "Dubois", "Petit", isAuto);
		doPaginedSearch(criteriaContact, 4, 0, "name", false, serverSideToken, 4, "Dubois", "Martin", isAuto);
		doPaginedSearch(criteriaContact, 4, 0, "name", true, serverSideToken, 4, "Petit", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 4, 1, "name", true, serverSideToken, 4, "Moreau", "Durant", isAuto);
		doPaginedSearch(criteriaContact, 4, 1, "name", false, serverSideToken, 4, "Durant", "Moreau", isAuto);
	}

	@Test
	public void testSearchQueryPaginedSortDate() {
		doTestSearchPaginedSortDate(false);
	}

	@Test
	public void testSearchAutoPaginedSortDate() {
		doTestSearchPaginedSortDate(true);
	}

	private void doTestSearchPaginedSortDate(final boolean isAuto) {
		final Map<String, Object> criteriaContact = new MapBuilder<String, Object>()
				.put("birthdayMin", "1978-05-19")
				.put("birthdayMax", "1985-05-19")
				.build();

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 10, 0, "birthday", false, serverSideToken, 6, "Petit", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 3, 1, "birthday", false, serverSideToken, 3, "Martin", "Durant", isAuto);
		doPaginedSearch(criteriaContact, 3, 1, "birthday", true, serverSideToken, 3, "Moreau", "Dubois", isAuto);
	}

	@Test
	public void testSearchQueryPaginedMissing() {
		doTestSearchPaginedMissing(false);
	}

	@Test
	public void testSearchAutoPaginedMissing() {
		doTestSearchPaginedMissing(true);
	}

	private void doTestSearchPaginedMissing(final boolean isAuto) {
		final Map<String, Object> criteriaContact = new MapBuilder<String, Object>()
				.put("birthdayMin", "1978-05-19")
				.put("birthdayMax", "1985-05-19")
				.build();

		String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, null, null, null, null, serverSideToken, 6, "Martin", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 5, null, null, null, serverSideToken, 5, "Martin", "Moreau", isAuto);
		doPaginedSearch(criteriaContact, 5, 5, null, null, serverSideToken, 1, "Garcia", "Garcia", isAuto);
	}

	private String doPaginedSearch(
			final Map<String, Object> criteriaContact,
			final Integer top,
			final Integer skip,
			final String sortFieldName,
			final Boolean sortDesc,
			final String listServerToken,
			final int expectedSize,
			final String firstContactName,
			final String lastContactName,
			final boolean isAuto) {
		final RequestSpecification given = given().filter(loggedSessionFilter);
		final String wsUrl = isAuto ? "/test/_searchAutoPagined" : "/test/_searchQueryPagined";
		if (top != null) {
			given.queryParam("top", top);
		}
		if (skip != null) {
			given.queryParam("skip", skip);
		}
		if (sortFieldName != null) {
			given.queryParam("sortFieldName", sortFieldName);
		}
		if (sortDesc != null) {
			given.queryParam("sortDesc", sortDesc);
		}
		if (listServerToken != null) {
			given.queryParam("listServerToken", listServerToken);
		}
		ResponseSpecification responseSpecification = given.body(criteriaContact)
				.expect().log().ifValidationFails()
				.body("size()", Matchers.equalTo(expectedSize));
		if (expectedSize > 0) {
			responseSpecification = responseSpecification.body("get(0).name", Matchers.equalTo(firstContactName))
					.body("get(" + (expectedSize - 1) + ").name", Matchers.equalTo(lastContactName));
		}
		final String newListServerToken = responseSpecification.statusCode(HttpStatus.SC_OK)
				.when()
				.post(wsUrl)
				.header("listServerToken");
		return newListServerToken;
	}

	@Test
	public void testLoadListMeta() {
		loggedAndExpect()
				.header("testLong", Matchers.equalTo("12"))
				.header("testString", Matchers.equalTo("the String test"))
				.header("testDate", Matchers.notNullValue())
				//.header("testDate", Matchers.any(Date.class)) //get a string no directly a Date can't ensured
				.header("testEscapedString", Matchers.equalTo("the EscapedString \",} test"))
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/dtListMeta");
	}

	@Test
	public void testLoadListMetaAsList() {
		loggedAndExpect()
				.header("testLong", Matchers.equalTo("12"))
				.header("testString", Matchers.equalTo("the String test"))
				.header("testDate", Matchers.notNullValue())
				//.header("testDate", Matchers.any(Date.class)) //get a string no directly a Date can't ensured
				.header("testEscapedString", Matchers.equalTo("the EscapedString \",} test"))
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/dtListMetaAsList");
	}

	@Test
	public void testLoadListComplexMeta() {
		loggedAndExpect()
				.body("testLong", Matchers.equalTo(12))
				.body("testString", Matchers.equalTo("the String test"))
				.body("testDate", Matchers.notNullValue())
				//.body("testDate", Matchers.any(Date.class)) //get a string no directly a Date can't ensured
				.body("testEscapedString", Matchers.equalTo("the EscapedString \",} test"))
				.body("contact1.name", Matchers.notNullValue())
				.body("contact2.name", Matchers.notNullValue())
				.body("size()", Matchers.equalTo(1 + 6)) //value field + meta fields
				.body("value.size()", Matchers.greaterThanOrEqualTo(10)) //list value
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/listComplexMeta");
	}

	@Test
	public void testSaveListDelta() {
		final Map<String, Object> dtListDelta = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collCreates = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collUpdates = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collDeletes = new LinkedHashMap<>();
		dtListDelta.put("collCreates", collCreates);
		dtListDelta.put("collUpdates", collUpdates);
		dtListDelta.put("collDeletes", collDeletes);
		collCreates.put("c110", createDefaultContact(110L));
		collCreates.put("c111", createDefaultContact(111L));
		collUpdates.put("c100", createDefaultContact(100L));
		collUpdates.put("c101", createDefaultContact(101L));
		collUpdates.put("c102", createDefaultContact(102L));
		collDeletes.put("c90", createDefaultContact(90L));

		loggedAndExpect(given().body(dtListDelta))
				.body(Matchers.equalTo("OK : add 2 contacts, update 3 contacts, removed 1"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/saveListDelta");
	}

	@Test
	public void testSaveListDeltaValdationError() {
		final Map<String, Object> dtListDelta = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collCreates = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collUpdates = new LinkedHashMap<>();
		final Map<String, Map<String, Object>> collDeletes = new LinkedHashMap<>();

		dtListDelta.put("collCreates", collCreates);
		dtListDelta.put("collUpdates", collUpdates);
		dtListDelta.put("collDeletes", collDeletes);

		collCreates.put("c110", createDefaultContact(110L));
		collCreates.put("c111", createDefaultContact(111L));
		final Map<String, Object> newContact = createDefaultContact(100L);
		newContact.put("birthday", "2012-10-24");
		collUpdates.put("c100", newContact);
		collUpdates.put("c101", createDefaultContact(101L));
		collUpdates.put("c102", createDefaultContact(102L));
		collDeletes.put("c90", createDefaultContact(90L));

		loggedAndExpect(given().body(dtListDelta))
				.body("objectFieldErrors.c100.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveListDelta");
	}

	@Test
	public void testSaveDtListContact() {
		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(120L))
				.add(createDefaultContact(121L))
				.add(createDefaultContact(123L))
				.add(createDefaultContact(124L))
				.add(createDefaultContact(125L))
				.add(createDefaultContact(126L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body(Matchers.equalTo("OK : received 6 contacts"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/saveDtListContact");
	}

	@Test
	public void testSaveDtListContactValidationError() {
		final Map<String, Object> newContact = createDefaultContact(123L);
		newContact.remove("name");

		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(120L))
				.add(createDefaultContact(121L))
				.add(newContact)
				.add(createDefaultContact(124L))
				.add(createDefaultContact(125L))
				.add(createDefaultContact(126L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveDtListContact");

		final Map<String, Object> new2Contact = createDefaultContact(127L);
		new2Contact.put("birthday", "2012-10-24");
		dtList.add(new2Contact);
		loggedAndExpect(given().body(dtList))
				.body("objectFieldErrors.idx6.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveDtListContact");
	}

	@Test
	public void testSaveUiListContact() {
		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(130L))
				.add(createDefaultContact(131L))
				.add(createDefaultContact(133L))
				.add(createDefaultContact(134L))
				.add(createDefaultContact(135L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body(Matchers.equalTo("OK : received 5 contacts"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/saveUiListContact");
	}

	@Test
	public void testSaveUiListContactValidationError() {
		final Map<String, Object> newContact = createDefaultContact(123L);
		newContact.remove("name");

		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(120L))
				.add(createDefaultContact(121L))
				.add(newContact)
				.add(createDefaultContact(124L))
				.add(createDefaultContact(125L))
				.add(createDefaultContact(126L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveUiListContact");

		final Map<String, Object> new2Contact = createDefaultContact(127L);
		new2Contact.put("birthday", "2012-10-24");
		dtList.add(new2Contact);
		loggedAndExpect(given().body(dtList))
				.body("objectFieldErrors.idx6.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveUiListContact");
	}

	@Test
	public void testSaveListContact() {
		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(130L))
				.add(createDefaultContact(131L))
				.add(createDefaultContact(133L))
				.add(createDefaultContact(134L))
				.add(createDefaultContact(135L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body(Matchers.equalTo("OK : received 5 contacts"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/saveListContact");
	}

	@Test
	public void testSaveDtListContactValidationError2() {
		final Map<String, Object> newContact = createDefaultContact(123L);
		newContact.put("birthday", "2012-10-24");

		final List<Map<String, Object>> dtList = new ListBuilder<Map<String, Object>>()
				.add(createDefaultContact(120L))
				.add(createDefaultContact(121L))
				.add(newContact)
				.add(createDefaultContact(124L))
				.add(createDefaultContact(125L))
				.add(createDefaultContact(126L))
				.build();

		loggedAndExpect(given().body(dtList))
				.body("objectFieldErrors.idx2.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/saveDtListContact");
	}

	@Test
	public void testUploadFile() throws UnsupportedEncodingException {
		final URL imageUrl = Thread.currentThread().getContextClassLoader().getResource("npi2loup.png");
		final File imageFile = new File(URLDecoder.decode(imageUrl.getFile(), "UTF-8"));

		loggedAndExpect(given().multiPart("upfile", imageFile, "image/png")
				.formParam("id", 12)
				.formParam("note", "Some very important notes about this file."))
						//expect
						.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
						.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"npi2loup.png\";filename*=UTF-8''npi2loup.png"))
						.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
						.statusCode(HttpStatus.SC_OK)
						.when()//.log().headers()
						.post("/test/uploadFile");

		loggedAndExpect(given()
				.formParam("id", 12)
				.formParam("note", "Some very important notes about this file.")
				.multiPart("upfile", imageFile, "image/png"))
						//expect
						.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
						.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"npi2loup.png\";filename*=UTF-8''npi2loup.png"))
						.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
						.statusCode(HttpStatus.SC_OK)
						.when()//.log().headers()
						.post("/test/uploadFile");
	}

	@Test
	public void testUploadFileError() throws UnsupportedEncodingException {
		final URL imageUrl = Thread.currentThread().getContextClassLoader().getResource("npi2loup.png");
		final File imageFile = new File(URLDecoder.decode(imageUrl.getFile(), "UTF-8"));

		RestAssured.given()
				.filter(loggedSessionFilter)
				.given()
				.multiPart("upFile", imageFile, "image/png")
				.formParam("id", 12)
				.formParam("note", "Some very important notes about this file.")
				.expect()
				.body("globalErrors", Matchers.anyOf(
						Matchers.contains("File upfile not found. Parts sent : id, upFile, note"),
						Matchers.contains("File upfile not found. Parts sent : note, upFile, id")))
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.when()
				.post("/test/uploadFile");

		RestAssured.given()
				.filter(loggedSessionFilter)
				.given()
				.formParam("id", 12)
				.formParam("note", "Some very important notes about this file.")
				.formParam("upfile", imageFile)
				.expect()
				.body("globalErrors", Matchers.contains("File upfile not found. Request contentType isn't \"multipart/form-data\""))
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.when()
				.post("/test/uploadFile");
	}

	@Test
	public void testDownloadFileContentType() throws UnsupportedEncodingException {
		final String[] expectedSimpleNames = { "image0.png", "image1ÔÙæóñ.png", "image2µ°«_.png", "image3ÔÙæ%20óñµ°«_.png", "image4 __~.png",
				"image5  abcABCæøåÆØÅäöüïëêîâéíáóúýñ½§!#¤%&()=`@£$ {[]}+´¨^~'-_,_.png" };
		final String[] expectedEncodedNames = { "image0.png", "image1ÔÙæóñ.png", "image2µ°«_.png", "image3ÔÙæ óñµ°«_.png", "image4€__~.png",
				"image5你好abcABCæøåÆØÅäöüïëêîâéíáóúýñ½§!#¤%&()=`@£$€{[]}+´¨^~'-_,_.png" };

		for (int id = 0; id < expectedSimpleNames.length; id++) {
			final String expectedSimpleName = expectedSimpleNames[id];
			final String expectedEncodedName = URLEncoder.encode(expectedEncodedNames[id], "utf8").replace("+", "%20");

			loggedAndExpect(given().queryParam("id", id))
					.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
					.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"" + expectedSimpleName + "\";filename*=UTF-8''" + expectedEncodedName))
					.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.get("/test/downloadFileContentType");
		}
	}

	@Test
	public void testDownloadFile() {
		loggedAndExpect(given().queryParam("id", 10))
				.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"image10.png\";filename*=UTF-8''image10.png"))
				.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/downloadFile");
	}

	@Test
	public void testDownloadNotAttachmentFile() {
		loggedAndExpect(given().queryParam("id", 10))
				.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
				.header("Content-Disposition", Matchers.equalToIgnoringCase("filename=\"image10.png\";filename*=UTF-8''image10.png"))
				.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/downloadEmbeddedFile");
	}

	@Test
	public void testDownloadNotModifiedFile() {
		final DateTimeFormatter httpDateFormat = DateTimeFormatter
				.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
				.withLocale(Locale.US);
		//Sans préciser le if-Modified-Since, le serveur retourne le fichier
		final Response response = loggedAndExpect(given().queryParam("id", 10))
				.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"image10.png\";filename*=UTF-8''image10.png"))
				.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/downloadNotModifiedFile");

		final String lastModified = response.getHeader("Last-Modified");
		final ZonedDateTime lastModifiedDate = ZonedDateTime.from(httpDateFormat.parse(lastModified));
		final String now = httpDateFormat.format(ZonedDateTime.now());

		//On test avec le if-Modified-Since now : le server test mais ne retourne pas le fichier
		loggedAndExpect(given().queryParam("id", 10).header("if-Modified-Since", now))
				.statusCode(HttpStatus.SC_NOT_MODIFIED)
				.when()
				.get("/test/downloadNotModifiedFile");

		//On test avec le if-Modified-Since 10 min avant le lastModified : le server test et retourne le fichier
		final String beforeLastModified = httpDateFormat.format(lastModifiedDate.minusMinutes(10));
		loggedAndExpect(given().queryParam("id", 10).header("if-Modified-Since", beforeLastModified))
				.header("Content-Type", Matchers.equalToIgnoringCase("image/png"))
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=\"image10.png\";filename*=UTF-8''image10.png"))
				.header("Content-Length", Matchers.anyOf(Matchers.equalTo("27032"), Matchers.equalTo("27039")))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/downloadNotModifiedFile");
	}

	@Test
	public void testDatesUTC() {
		final String inputUtc = "2016-01-18T17:21:42.026Z";
		loggedAndExpect(given())
				.body("input", Matchers.equalTo(inputUtc))
				.body("inputAsString", Matchers.equalTo("Mon Jan 18 18:21:42 CET 2016"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/dates?date=" + inputUtc);
	}

	@Test
	public void testLocalDate() {
		loggedAndExpect(given())
				.body(Matchers.equalTo("\"2017-06-27\""))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/localDate");

		final String inputLocalDate = "2016-01-18";
		loggedAndExpect(given())
				.body("input", Matchers.equalTo(inputLocalDate))
				.body("inputAsString", Matchers.equalTo("2016-01-18"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/localDate?date=" + inputLocalDate);
	}

	@Test
	public void testZonedDateTime() {
		loggedAndExpect(given())
				.body(Matchers.equalTo("\"2016-05-26T21:30:20Z\""))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/zonedDateTime");

		final String inputZonedDateTime = "2016-01-18T17:21:42Z";
		loggedAndExpect(given())
				.body("input", Matchers.equalTo(inputZonedDateTime))
				.body("inputAsString", Matchers.equalTo("2016-01-18T17:21:42Z[UTC]"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/zonedDateTime?date=" + inputZonedDateTime);
	}

	@Test
	public void testZonedDateTimeUTC() {
		loggedAndExpect(given())
				.body(Matchers.equalTo("\"2016-07-27T22:00:00Z\""))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/zonedDateTimeUTC");

		final String inputZonedDateTime = "2016-04-25T00:00:00Z";
		loggedAndExpect(given())
				.body("input", Matchers.equalTo(inputZonedDateTime))
				.body("inputAsString", Matchers.equalTo("2016-04-25T00:00Z[UTC]"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/zonedDateTime?date=" + inputZonedDateTime);
	}

	@Test
	public void testInstant() {
		loggedAndExpect(given())
				.body(Matchers.equalTo("\"2016-05-26T21:30:20Z\""))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/instant");

		final String inputInstant = "2016-01-18T17:21:42Z";
		loggedAndExpect(given())
				.body("input", Matchers.equalTo(inputInstant))
				.body("inputAsString", Matchers.equalTo("2016-01-18T17:21:42Z"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/instant?date=" + inputInstant);
	}

	@Test
	public void testString() {
		loggedAndExpect(given().body(UTF8_TEST_STRING))
				.statusCode(HttpStatus.SC_OK).log().all()
				.when()
				.post("/test/string");
	}

	@Test
	public void testOptionalQueryParam() {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact)
				.queryParam("token", "TestedToken"))
						.statusCode(HttpStatus.SC_OK)
						.body(Matchers.equalTo("TestedToken"))
						.when()
						.post("/test/string/optionalQueryParam");

		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("empty"))
				.when()
				.post("/test/string/optionalQueryParam");

	}

	@Test
	public void testOptionalInnerBodyParam() {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("empty"))
				.when()
				.post("/test/string/optionalInnerBodyParam");

		newContact.put("token", null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("empty"))
				.when()
				.post("/test/string/optionalInnerBodyParam");

		newContact.put("token", "");
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("empty"))
				.when()
				.post("/test/string/optionalInnerBodyParam");

		newContact.put("token", "TestedToken");
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("TestedToken"))
				.when()
				.post("/test/string/optionalInnerBodyParam");

	}

	@Test
	public void testSelectedFacetValues() {
		final Map<String, Object> emptySelectedFacets = new MapBuilder<String, Object>()
				.build();
		loggedAndExpect(given().body(emptySelectedFacets))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{}"))
				.when()
				.post("/search/selectedFacetValues");

		final Map<String, Object> selectedFacetsMono = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "Mr")
				.build();
		loggedAndExpect(given().body(selectedFacetsMono))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{\"FctHonorificCode\":\"Mr\"}"))
				.when()
				.post("/search/selectedFacetValues");

		final Map<String, Object> selectedFacetsByCode = new MapBuilder<String, Object>()
				.put("FctBirthday", "r1")
				.build();
		loggedAndExpect(given().body(selectedFacetsByCode))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{\"FctBirthday\":\"r1\"}"))
				.when()
				.post("/search/selectedFacetValues");

		final Map<String, Object> selectedFacetsByLabel = new MapBuilder<String, Object>()
				.put("FctBirthday", "1980-1990")
				.build();
		loggedAndExpect(given().body(selectedFacetsByLabel))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{\"FctBirthday\":\"r2\"}"))
				.when()
				.post("/search/selectedFacetValues");

		final Map<String, Object> selectedFacetsBoth = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "Mr")
				.put("FctBirthday", "r1")
				.build();
		loggedAndExpect(given().body(selectedFacetsBoth))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{\"FctHonorificCode\":\"Mr\", \"FctBirthday\":\"r1\"}"))
				.when()
				.post("/search/selectedFacetValues");

		final Map<String, Object> selectedFacetsMultiple = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "Mr")
				.put("FctBirthday", new String[] { "r1", "r3" })
				.build();
		loggedAndExpect(given().body(selectedFacetsMultiple))
				.statusCode(HttpStatus.SC_OK)
				.body(Matchers.equalTo("{\"FctHonorificCode\":\"Mr\", \"FctBirthday\":\"r1,r3\"}"))
				.when()
				.post("/search/selectedFacetValues");

	}

	@Test
	public void testFacetedSearchResult() {
		final Map<String, Object> emptySelectedFacets = new MapBuilder<String, Object>()
				.build();
		final Response getResponse = loggedAndExpect(given().body(emptySelectedFacets))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(Matchers.greaterThanOrEqualTo(10)))
				.body("list.get(0).address", Matchers.nullValue())
				.body("highlight", Matchers.nullValue())
				.body("facets.get(1).code", Matchers.equalTo("FctBirthday"))
				.body("facets.get(1).values.get(0).code", Matchers.equalTo("r1"))
				.body("facets.get(1).values.get(0).count", Matchers.equalTo(5))
				.body("facets.get(1).values.get(1).code", Matchers.equalTo("r2"))
				.body("facets.get(1).values.get(1).count", Matchers.greaterThanOrEqualTo(6))
				.body("totalCount", Matchers.greaterThanOrEqualTo(10))
				.when()
				.post("/search/facetedResult");
		final int fctBirthDayR1 = getResponse.body().path("facets.get(1).values.get(0).count");
		final int fctBirthDayR2 = getResponse.body().path("facets.get(1).values.get(1).count");

		final Map<String, Object> selectedFacetsMono = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "MR_")
				.build();
		loggedAndExpect(given().body(selectedFacetsMono))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(2))
				.body("totalCount", Matchers.equalTo(2))
				.body("facets.get(0).values", Matchers.hasSize(1))
				.when()
				.post("/search/facetedResult");

		final Map<String, Object> selectedFacetsByCode = new MapBuilder<String, Object>()
				.put("FctBirthday", "r1")
				.build();
		loggedAndExpect(given().body(selectedFacetsByCode))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(fctBirthDayR1))
				.body("totalCount", Matchers.equalTo(fctBirthDayR1))
				.body("facets.get(0).values", Matchers.hasSize(fctBirthDayR1))
				.when()
				.post("/search/facetedResult");

		final Map<String, Object> selectedFacetsByLabel = new MapBuilder<String, Object>()
				.put("FctBirthday", "1980-1990")
				.build();
		loggedAndExpect(given().body(selectedFacetsByLabel))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(fctBirthDayR2))
				.body("totalCount", Matchers.equalTo(fctBirthDayR2))
				.body("facets.get(0).values", Matchers.hasSize(6))
				.when()
				.post("/search/facetedResult");

		final Map<String, Object> selectedFacetsBoth = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "MR_")
				.put("FctBirthday", "r2")
				.build();
		loggedAndExpect(given().body(selectedFacetsBoth))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(1))
				.body("totalCount", Matchers.equalTo(1))
				.body("facets.get(0).values", Matchers.hasSize(1))
				.when()
				.post("/search/facetedResult");

		final Map<String, Object> selectedFacetsMultiple = new MapBuilder<String, Object>()
				.put("FctHonorificCode", new String[] { "MR_", "MS_" })
				.put("FctBirthday", "r2")
				.build();
		loggedAndExpect(given().body(selectedFacetsMultiple))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.hasSize(2))
				.body("totalCount", Matchers.equalTo(2))
				.body("facets.get(0).values", Matchers.hasSize(2))
				.when()
				.post("/search/facetedResult");
	}

	@Test
	public void testFacetedClusteredSearchResult() {
		final Map<String, Object> emptySelectedFacets = new MapBuilder<String, Object>()
				.build();
		final Response getResponse = loggedAndExpect(given().body(emptySelectedFacets))
				.statusCode(HttpStatus.SC_OK)
				.body("list", Matchers.nullValue())
				.body("groups", Matchers.hasSize(Matchers.greaterThanOrEqualTo(10)))
				.body("groups.get(0).list", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1)))
				.body("groups.get(0).list.get(0).address", Matchers.nullValue())
				.body("highlight", Matchers.nullValue())
				.body("facets.get(1).code", Matchers.equalTo("FctBirthday"))
				.body("facets.get(1).values.get(0).code", Matchers.equalTo("r1"))
				.body("facets.get(1).values.get(0).count", Matchers.equalTo(5))
				.body("facets.get(1).values.get(1).code", Matchers.equalTo("r2"))
				.body("facets.get(1).values.get(1).count", Matchers.greaterThanOrEqualTo(6))
				.body("totalCount", Matchers.greaterThanOrEqualTo(10))
				.when()
				.post("/search/facetedClusteredResult");
		final int fctBirthDayR1 = getResponse.body().path("facets.get(1).values.get(0).count");
		final int fctBirthDayR2 = getResponse.body().path("facets.get(1).values.get(1).count");

		final Map<String, Object> selectedFacetsMono = new MapBuilder<String, Object>()
				.put("FctHonorificCode", "MR_")
				.build();
		loggedAndExpect(given().body(selectedFacetsMono))
				.statusCode(HttpStatus.SC_OK)
				.body("groups", Matchers.hasSize(1))
				.body("totalCount", Matchers.equalTo(2))
				.body("groups.get(0).list", Matchers.hasSize(2))
				.when()
				.post("/search/facetedClusteredResult");

		final Map<String, Object> selectedFacetsByCode = new MapBuilder<String, Object>()
				.put("FctBirthday", "r1")
				.build();
		loggedAndExpect(given().body(selectedFacetsByCode))
				.statusCode(HttpStatus.SC_OK)
				.body("groups", Matchers.hasSize(fctBirthDayR1))
				.body("totalCount", Matchers.equalTo(fctBirthDayR1))
				.body("facets.get(0).values", Matchers.hasSize(fctBirthDayR1))
				.when()
				.post("/search/facetedClusteredResult");

		final Map<String, Object> selectedFacetsByLabel = new MapBuilder<String, Object>()
				.put("FctBirthday", "1980-1990")
				.build();
		loggedAndExpect(given().body(selectedFacetsByLabel))
				.statusCode(HttpStatus.SC_OK)
				.body("groups", Matchers.hasSize(6))
				.body("totalCount", Matchers.equalTo(fctBirthDayR2))
				.body("facets.get(0).values", Matchers.hasSize(6))
				.when()
				.post("/search/facetedClusteredResult");

		final Map<String, Object> selectedFacetsMultiple = new MapBuilder<String, Object>()
				.put("FctHonorificCode", new String[] { "MR_", "MS_" })
				.put("FctBirthday", "r2")
				.build();
		loggedAndExpect(given().body(selectedFacetsMultiple))
				.statusCode(HttpStatus.SC_OK)
				.body("groups", Matchers.hasSize(2))
				.body("totalCount", Matchers.equalTo(2))
				.body("facets.get(0).values", Matchers.hasSize(2))
				.when()
				.post("/search/facetedClusteredResult");
	}

	//=========================================================================

	protected static RequestSpecification given() {
		return RestAssured.given();
	}

	private ResponseSpecification loggedAndExpect() {
		return RestAssured.given()
				.filter(loggedSessionFilter)
				.expect().log().ifValidationFails();
	}

	protected ResponseSpecification loggedAndExpect(final RequestSpecification given) {
		return given
				.filter(loggedSessionFilter)
				.expect().log().ifValidationFails();
	}

	private Map<String, Object> doGetServerSideObject() {
		final Map<String, Object> contact = doCreateContact();
		final Long oldConId = (Long) contact.get("conId");

		final Response getResponse = loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/filtered/" + oldConId);
		final String serverToken = getResponse.body().path("serverToken");
		contact.put("serverToken", serverToken);
		return contact;
	}

	protected static Map<String, Object> createDefaultContact(final Long conId) {
		final Map<String, Object> newContact = createContact2(conId, "MRS", "Fournier", "Catherine", "1985-10-24",
				createAddress(10L, "10, avenue Claude Vellefaux", "", "Paris", "75010", "France"),
				"catherine.fournier@gmail.com", "01 91 92 93 94");
		return newContact;
	}

	private static Map<String, Object> createContact2(
			final Long conId,
			final String honorific,
			final String name,
			final String firstName,
			final String birthday,
			final Map<String, Object> address,
			final String email,
			final String... tels) {
		return new MapBuilder<String, Object>()
				.putNullable("conId", conId)
				.put("honorificCode", honorific)
				.put("name", name)
				.put("firstName", firstName)
				.put("birthday", birthday)
				.put("address", address)
				.put("email", email)
				.put("tels", Arrays.asList(tels))
				.build();
	}

	private static Map<String, Object> createAddress(final Long adrId, final String street1, final String street2, final String city, final String postalCode, final String country) {
		return new MapBuilder<String, Object>()
				.put("adrId", adrId)
				.put("street1", street1)
				.put("street2", street2)
				.put("city", city)
				.put("postalCode", postalCode)
				.put("country", country)
				.build();
	}
}
