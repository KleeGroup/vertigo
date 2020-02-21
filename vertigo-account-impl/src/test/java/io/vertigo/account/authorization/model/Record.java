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
package io.vertigo.account.authorization.model;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Dossier.
 */
public final class Record implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long dosId;

	private Long regId;
	private Long depId;
	private Long comId;

	private Long typId;
	private String title;
	private Double amount;
	private Long utiIdOwner;

	private String etaCd;

	/** {@inheritDoc} */
	@Override
	public UID<Record> getUID() {
		return UID.of(this);
	}

	@Field(domain = "DoId", type = "ID", required = true, label = "Id")
	public final Long getDosId() {
		return dosId;
	}

	public final void setDosId(final Long dosId) {
		this.dosId = dosId;
	}

	@Field(domain = "DoId", label = "Region")
	public final Long getRegId() {
		return regId;
	}

	public final void setRegId(final Long regId) {
		this.regId = regId;
	}

	@Field(domain = "DoId", label = "Département")
	public final Long getDepId() {
		return depId;
	}

	public final void setDepId(final Long depId) {
		this.depId = depId;
	}

	@Field(domain = "DoId", label = "Commune")
	public final Long getComId() {
		return comId;
	}

	public final void setComId(final Long comId) {
		this.comId = comId;
	}

	@Field(domain = "DoId", required = true, label = "Type dossier")
	public final Long getTypId() {
		return typId;
	}

	public final void setTypId(final Long typId) {
		this.typId = typId;
	}

	@Field(domain = "DoLabel", required = true, label = "Title")
	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	@Field(domain = "DoMontant", required = true, label = "Amount")
	public final Double getAmount() {
		return amount;
	}

	public final void setAmount(final Double amount) {
		this.amount = amount;
	}

	@Field(domain = "DoId", required = true, label = "Créateur")
	public final Long getUtiIdOwner() {
		return utiIdOwner;
	}

	public final void setUtiIdOwner(final Long utiIdOwner) {
		this.utiIdOwner = utiIdOwner;
	}

	@Field(domain = "DoCode", required = true, label = "Etat dossier")
	public final String getEtaCd() {
		return etaCd;
	}

	public final void setEtaCd(final String etaCd) {
		this.etaCd = etaCd;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
