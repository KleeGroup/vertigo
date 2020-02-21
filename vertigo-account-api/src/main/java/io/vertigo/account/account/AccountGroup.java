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
 * @author pchretien
 */
public final class AccountGroup implements Entity {
	private static final long serialVersionUID = -4463291583101516140L;

	@Field(type = "ID", domain = "DoXAccountId", required = true, label = "id")
	private final String id;

	@Field(domain = "DoXAccountName", label = "displayName")
	private final String displayName;

	/**
	 * @param id Id
	 * @param displayName Display name
	 */
	public AccountGroup(final String id, final String displayName) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkArgNotEmpty(displayName);
		//-----
		this.displayName = displayName;
		this.id = id;
	}

	/** {@inheritDoc} */
	@Override
	public UID<AccountGroup> getUID() {
		return UID.of(this);
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Display name
	 */
	public String getDisplayName() {
		return displayName;
	}
}
