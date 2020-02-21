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
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.MapBuilder;
import io.vertigo.vega.webservice.data.MyNodeConfig;

public final class WebServiceManagerSecuredTest {
	private final SessionFilter loggedSessionFilter = new SessionFilter();
	private final SessionFilter loggedSecuredSessionFilter = new SessionFilter();
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
		RestAssured.given()
				.filter(loggedSessionFilter)
				.get("/test/login");

		RestAssured.given()
				.filter(loggedSecuredSessionFilter)
				.get("/secured/contacts/login");
	}

	@AfterAll
	public static void tearDown() {
		if (app != null) {
			app.close();
			app = null;
		}
	}

	@Test
	public void testGetContactView() {
		loggedAndExpect()
				.body("globalErrors", Matchers.contains("Not enought authorizations"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/secured/contacts/contactView/1");
	}

	@Test
	public void testGetContactViewSecured() {
		loggedSecuredAndExpect()
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("addresses.size()", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/secured/contacts/contactView/1");
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
				.body("globalErrors", Matchers.contains("Not enought authorizations"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/secured/contacts/contactView");
	}

	@Test
	public void testPutContactViewSecured() {
		final Map<String, Object> newContactView = createDefaultContact(100L);

		final List<Map<String, Object>> addresses = new ListBuilder<Map<String, Object>>()
				.add(createAddress(10L, "10, avenue Claude Vellefaux", "", "Paris", "75010", "France"))
				.add(createAddress(24L, "24, avenue General De Gaulle", "", "Paris", "75001", "France"))
				.add(createAddress(38L, "38, impasse des puits", "", "Versaille", "78000", "France"))
				.build();

		newContactView.remove("address");
		newContactView.put("addresses", addresses);

		loggedSecuredAndExpect(given().body(newContactView))
				.body("globalErrors", Matchers.contains(Matchers.containsString("Can't check authorization on arg0")))
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.when()
				.put("/secured/contacts/contactView");
	}

	@Test
	public void testPutContact() {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Not enought authorizations"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/secured/contacts/100");
	}

	@Test
	public void testPutContactSecured() {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedSecuredAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(100))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/secured/contacts/100");
	}

	@Test
	public void testPostContact() {
		final Map<String, Object> newContact = createDefaultContact(null);

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Not enought authorizations"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.post("/secured/contacts");
	}

	@Test
	public void testPostContactSecured() {
		final Map<String, Object> newContact = createDefaultContact(null);

		final Long conId = loggedSecuredAndExpect(given().body(newContact))
				.body("conId", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_CREATED)
				.when()
				.post("/secured/contacts")
				.body().path("conId");
		newContact.put("conId", conId);
	}

	@Test
	public void testDeleteContact() {
		final Map<String, Object> newContact = createDefaultContact(105L);
		loggedSecuredAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/secured/contacts/105");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Not enought authorizations"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.delete("/secured/contacts/105");
	}

	@Test
	public void testDeleteContactSecured() {
		final Map<String, Object> newContact = createDefaultContact(105L);
		loggedSecuredAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/secured/contacts/105");

		loggedSecuredAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.delete("/secured/contacts/105");
	}

	//=========================================================================

	private static RequestSpecification given() {
		return RestAssured.given();
	}

	private ResponseSpecification loggedAndExpect() {
		return RestAssured.given()
				.filter(loggedSessionFilter)
				.expect().log().ifValidationFails();
	}

	private ResponseSpecification loggedSecuredAndExpect() {
		return RestAssured.given()
				.filter(loggedSecuredSessionFilter)
				.expect().log().ifValidationFails();
	}

	private ResponseSpecification loggedAndExpect(final RequestSpecification given) {
		return given
				.filter(loggedSessionFilter)
				.expect().log().ifValidationFails();
	}

	private ResponseSpecification loggedSecuredAndExpect(final RequestSpecification given) {
		return given
				.filter(loggedSecuredSessionFilter)
				.expect().log().ifValidationFails();
	}

	private static Map<String, Object> createDefaultContact(final Long conId) {
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
