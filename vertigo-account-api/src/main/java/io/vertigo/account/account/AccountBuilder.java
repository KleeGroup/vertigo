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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * @author pchretien
 */
public final class AccountBuilder implements Builder<Account> {
	private final String myId;
	private String myDisplayName;
	private String myEmail;
	private String myPhoto;
	private String myAuthToken;

	/**
	 * constructor
	 * @param id the id of the account
	 */
	AccountBuilder(final String id) {
		Assertion.checkArgNotEmpty(id);
		//-----
		myId = id;
	}

	/**
	 * Adds a displayName
	 * @param displayName the display name
	 * @return this builder
	 */
	public AccountBuilder withDisplayName(final String displayName) {
		Assertion.checkArgument(myDisplayName == null, "displayName already set");
		Assertion.checkArgNotEmpty(displayName);
		//-----
		myDisplayName = displayName;
		return this;
	}

	/**
	 * Adds an email
	 * @param email the email
	 * @return this builder
	 */
	public AccountBuilder withEmail(final String email) {
		Assertion.checkArgument(myEmail == null, "email already set");
		//email is nullable, we accept null value in case this builder is use by deserializer
		//-----
		myEmail = email;
		return this;
	}

	/**
	 * Adds an photo
	 * @param photo the photo
	 * @return this builder
	 */
	public AccountBuilder withPhoto(final String photo) {
		Assertion.checkArgument(myPhoto == null, "photo already set");
		//photo is nullable, we accept null value in case this builder is use by deserializer
		//-----
		myPhoto = photo;
		return this;
	}

	/**
	 * Adds an authToken
	 * @param authToken the authToken
	 * @return this builder
	 */
	public AccountBuilder withAuthToken(final String authToken) {
		Assertion.checkArgument(myAuthToken == null, "authToken already set");
		Assertion.checkArgNotEmpty(authToken);
		//-----
		myAuthToken = authToken;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Account build() {
		return new Account(myId, myDisplayName, myEmail, myPhoto, myAuthToken != null ? myAuthToken : myEmail);
	}
}
