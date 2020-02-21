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
package io.vertigo.account.identityprovider.model;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * User.
 */
public final class User implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String usrId;
	private String fullName;
	private String email;

	/** {@inheritDoc} */
	@Override
	public UID<User> getUID() {
		return UID.of(this);
	}

	@Field(domain = "DoCode", type = "ID", required = true, label = "Id")
	public final String getUsrId() {
		return usrId;
	}

	public final void setUsrId(final String usrId) {
		this.usrId = usrId;
	}

	@Field(domain = "DoLabel", required = true, label = "FullName")
	public final String getFullName() {
		return fullName;
	}

	public final void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	@Field(domain = "DoLabel", required = true, label = "Email")
	public final String getEmail() {
		return email;
	}

	public final void setEmail(final String email) {
		this.email = email;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
