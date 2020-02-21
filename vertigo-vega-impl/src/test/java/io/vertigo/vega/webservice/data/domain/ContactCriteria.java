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

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.stereotype.Field;

public final class ContactCriteria implements DtObject {

	private static final long serialVersionUID = 6839427455017031471L;

	//mandatory
	@Field(domain = "DoTexte50", label = "Name")
	private String name;
	@Field(domain = "DoTexte50", label = "Firstname")
	private String firstName;
	@Field(domain = "DoLocalDate", label = "Birthday min")
	private LocalDate birthdayMin;
	@Field(domain = "DoLocalDate", label = "Birthday max")
	private LocalDate birthdayMax;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public LocalDate getBirthdayMin() {
		return birthdayMin;
	}

	public void setBirthdayMin(final LocalDate birthdayMin) {
		this.birthdayMin = birthdayMin;
	}

	public LocalDate getBirthdayMax() {
		return birthdayMax;
	}

	public void setBirthdayMax(final LocalDate birthdayMax) {
		this.birthdayMax = birthdayMax;
	}

}
