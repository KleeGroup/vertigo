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
package io.vertigo.vega.webservice.data.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.vertigo.core.component.Activeable;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.lang.Assertion;

public class ContactDao implements Activeable, StoreServices {
	private final Map<Long, Contact> contacts = new HashMap<>();

	private void appendContact(final Honorific honorific, final String name, final String firstName, final LocalDate birthday, final Address address, final String email, final String... tels) {
		final long conId = contacts.size() + 1;
		final Contact contact = new Contact();
		contact.setConId(conId);
		contact.setHonorificCode(honorific.getCode());
		contact.setName(name);
		contact.setFirstName(firstName);
		contact.setBirthday(birthday);
		contact.getAddressAccessor().set(address);
		contact.setEmail(email);
		contact.setTels(Arrays.asList(tels));
		contacts.put(conId, contact);
	}

	private static LocalDate parseDate(final String dateStr) {
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return LocalDate.parse(dateStr, dateTimeFormatter);
	}

	private static Address createAddress(final Long adrId, final String street1, final String street2, final String city, final String postalCode, final String country) {
		final Address address = new Address();
		address.setAdrId(adrId);
		address.setStreet1(street1);
		address.setStreet2(street2);
		address.setCity(city);
		address.setPostalCode(postalCode);
		address.setCountry(country);
		return address;
	}

	public void post(final Contact contact) {
		Assertion.checkNotNull(contact);
		Assertion.checkArgument(contact.getConId() == null, "post");
		//------
		final long nextId = getNextId();
		contact.setConId(nextId);
		contacts.put(nextId, contact);
	}

	public void put(final Contact contact) {
		Assertion.checkNotNull(contact);
		Assertion.checkNotNull(contact.getConId());
		//------
		contacts.put(contact.getConId(), contact);
	}

	public List<Contact> getList() {
		return new ArrayList<>(contacts.values());
	}

	public void remove(final Long id) {
		contacts.remove(id);
	}

	public Contact get(final Long id) {
		return contacts.get(id);
	}

	private long getNextId() {
		final long nextId = UUID.randomUUID().getMostSignificantBits();
		if (contacts.containsKey(nextId)) {
			return getNextId();
		}
		return nextId;
	}

	public boolean containsKey(final long id) {
		return contacts.containsKey(id);
	}

	@Override
	public void start() {
		appendContact(Honorific.Mr, "Martin", "Jean", parseDate("19/05/1980"),
				createAddress(1L, "1, rue de Rivoli", "", "Paris", "75001", "France"),
				"jean.martin@gmail.com", "01 02 03 04 05");
		appendContact(Honorific.Mr, "Martin", "Romain", parseDate("19/05/1977"),
				createAddress(1L, "1, rue de Rivoli", "", "Paris", "75001", "France"),
				"romain.martin@gmail.com", "01 02 03 04 06");
		appendContact(Honorific.Miss, "Dubois", "Marie", parseDate("20/06/1981"),
				createAddress(2L, "2, rue Beauregard", "", "Paris", "75002", "France"),
				"marie.dubois@gmail.com", "01 13 14 15 16");
		appendContact(Honorific.Cpt, "Petit", "Philippe", parseDate("18/04/1979"),
				createAddress(3L, "3, rue Meslay", "", "Paris", "75003", "France"),
				"philippe.petit@gmail.com", "01 24 25 26 27");
		appendContact(Honorific.Off, "Durant", "Nathalie", parseDate("21/07/1982"),
				createAddress(4L, "4, avenue Victoria", "", "Paris", "75004", "France"),
				"nathalie.durant@gmail.com", "01 35 36 37 38");
		appendContact(Honorific.PhD, "Leroy", "Michel", parseDate("17/03/1978"),
				createAddress(5L, "5, boulevard Saint-Marcel", "", "Paris", "75005", "France"),
				"michel.leroy@gmail.com", "01 46 47 48 49");
		appendContact(Honorific.Ms, "Moreau", "Isabelle", parseDate("22/08/1983"),
				createAddress(6L, "6, boulevard Raspail", "", "Paris", "75006", "France"),
				"isabelle.moreau@gmail.com", "01 57 58 59 50");
		appendContact(Honorific.Rev, "Lefebvre", "Alain", parseDate("16/02/1977"),
				createAddress(7L, "7, rue Cler", "", "Paris", "75007", "France"),
				"alain.lefebvre@gmail.com", "01 68 69 60 61");
		appendContact(Honorific.Dr, "Garcia", "Sylvie", parseDate("23/09/1984"),
				createAddress(8L, "8, rue de Ponthieu", "", "Paris", "75008", "France"),
				"sylvie.garcia@gmail.com", "01 79 70 71 72");
		appendContact(Honorific.Mst, "Roux", "Patrick", parseDate("15/01/1976"),
				createAddress(9L, "9, avenue Frochot", "", "Paris", "75009", "France"),
				"patrick.roux@gmail.com", "01 80 81 82 83");
		appendContact(Honorific.Mrs, "Fournier", "Catherine", parseDate("24/10/1985"),
				createAddress(10L, "10, avenue Claude Vellefaux", "", "Paris", "75010", "France"),
				"catherine.fournier@gmail.com", "01 91 92 93 94");
	}

	@Override
	public void stop() {
		contacts.clear();
	}
}
