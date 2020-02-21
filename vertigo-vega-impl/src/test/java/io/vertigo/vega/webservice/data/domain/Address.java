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

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.ListVAccessor;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;

public final class Address implements Entity {
	private static final long serialVersionUID = 8922834274442256496L;

	@Field(domain = "DoId", type = "ID", required = true, label = "address Id")
	private Long adrId;
	@Field(domain = "DoTexte50", label = "street1")
	private String street1;
	@Field(domain = "DoTexte50", label = "street2")
	private String street2;
	@Field(domain = "DoTexte50", label = "city")
	private String city;
	@Field(domain = "DoTexte50", label = "postal code")
	private String postalCode;
	@Field(domain = "DoTexte50", label = "country")
	private String country;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "AConAdr",
			fkFieldName = "adrId",
			primaryDtDefinitionName = "DtAddress",
			primaryIsNavigable = true,
			primaryRole = "Address",
			primaryLabel = "Address",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DtContact",
			foreignIsNavigable = false,
			foreignRole = "Contact",
			foreignLabel = "Contact",
			foreignMultiplicity = "0..*")
	private final ListVAccessor<Contact> contactAccessor = new ListVAccessor<>(this, "AConAdr", "Contact");

	/** {@inheritDoc} */
	@Override
	public UID<Address> getUID() {
		return UID.of(this);
	}

	public Long getAdrId() {
		return adrId;
	}

	public void setAdrId(final Long adrId) {
		this.adrId = adrId;
	}

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(final String street1) {
		this.street1 = street1;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(final String street2) {
		this.street2 = street2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(final String country) {
		this.country = country;
	}

	public ListVAccessor<Contact> getContactAccessor() {
		return contactAccessor;
	}

}
