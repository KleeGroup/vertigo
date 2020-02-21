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
package io.vertigo.account.plugins.authorization.loaders;

import java.util.List;

import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.SecuredEntity;

/**
 * Configuration de la sécurité avancée.
 *
 * @author jgarnier
 */
final class AdvancedSecurityConfiguration {

	private final List<Authorization> globalAuthorizations;
	private final List<SecuredEntity> securedEntities;

	/**
	 * Construct an instance of AdvancedSecurityConfiguration.
	 *
	 * @param globalAuthorizations Authorizations attribuables aux utilisateurs.
	 * @param securedEntities Description des entités sécurisés.
	 */
	public AdvancedSecurityConfiguration(
			final List<Authorization> globalAuthorizations,
			final List<SecuredEntity> securedEntities) {
		super();
		this.globalAuthorizations = globalAuthorizations;
		this.securedEntities = securedEntities;
	}

	/**
	 * Give the value of permissions.
	 *
	 * @return the value of permissions.
	 */
	public List<Authorization> getGlobalAuthorizations() {
		return globalAuthorizations;
	}

	/**
	 * Give the value of securedEntities.
	 *
	 * @return the value of securedEntities.
	 */
	public List<SecuredEntity> getSecuredEntities() {
		return securedEntities;
	}
}
