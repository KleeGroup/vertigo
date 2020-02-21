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
package io.vertigo.account.account;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.lang.Assertion;

/**
 * This class defines the account of a user/organization/system.
 *
 * @author pchretien
 */
public final class Account implements Entity {
	private static final long serialVersionUID = 7509030642946579907L;

	@Field(type = "ID", domain = "DoXAccountId", required = true, label = "id")
	private final String id;

	@Field(domain = "DoXAccountName", label = "displayName")
	private final String displayName;

	@Field(domain = "DoXAccountEmail", label = "email")
	private final String email;

	@Field(domain = "DoXAccountPhoto", label = "photo")
	private final String photo;

	@Field(domain = "DoXAccountAuthToken", label = "authToken")
	private final String authToken;

	Account(final String id, final String displayName, final String email, final String photo, final String authToken) {
		Assertion.checkArgNotEmpty(id);
		//-----
		this.id = id;
		this.displayName = displayName;
		this.email = email;
		this.photo = photo;
		this.authToken = authToken;
	}

	/**
	 * Static method factory for AccountBuilder
	 * @param id the id of the account
	 * @return AccountBuilder
	 */
	public static AccountBuilder builder(final String id) {
		return new AccountBuilder(id);
	}

	/** {@inheritDoc} */
	@Override
	public UID<Account> getUID() {
		return UID.of(this);
	}

	/**
	 * @return the id of the account
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the diplayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the photo
	 */
	public String getPhoto() {
		return photo;
	}

	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}
}
