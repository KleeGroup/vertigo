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
package io.vertigo.account.authorization;

import java.util.List;

import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.domain.model.KeyConcept;

/**
 * Authorizations manager.
 * There is two type of authorisations :
 * - general, no-conditions authoriations : used for main features of application (menu, admin, ...)
 * - context dependents authorisations : used for operation on secured data
 *
 * @author npiedeloup
 */
public interface AuthorizationManager extends Manager {

	/**
	 * User authorization accessor to test or add authorizations.
	 * A UserSession must exists.
	 * @return UserAuthorizations
	 */
	UserAuthorizations obtainUserAuthorizations();

	/**
	 * Check on authorizations.
	 * Say if current user has this authorization.
	 *
	 * @param authorizationName authorization. (not null)
	 * @return if user has this authorization.
	 */
	boolean hasAuthorization(AuthorizationName... authorizationName);

	/**
	 * Check if current user can do this operation on this keyConcept.
	 *
	 * @param keyConcept secured data to check
	 * @param operation operation name
	 * @return true if current user can do this operation on this keyConcept.
	 * @param <K> keyConcept type
	 */
	<K extends KeyConcept> boolean isAuthorized(final K keyConcept, OperationName<K> operation);

	/**
	 * Return Criteria of security rules for this current user on this keyConceptClass.
	 *
	 * @param keyConceptClass secured data to check
	 * @param operation operation name
	 * @return Criteria of security rule for this current user on this keyConcept
	 * @param <K> keyConcept type
	 */
	<K extends KeyConcept> Criteria<K> getCriteriaSecurity(Class<K> keyConceptClass, OperationName<K> operation);

	/**
	 * Return Search query filter of security rules for this current user on this keyConceptClass.
	 *
	 * @param keyConceptClass secured data to check
	 * @param operation operation name
	 * @return Search query filter of security rules for this current user on this keyConcept.
	 * @param <K> keyConcept type
	 */
	<K extends KeyConcept> String getSearchSecurity(final Class<K> keyConceptClass, OperationName<K> operation);

	/**
	 * Get all operation doable on this object by current user.
	 * This can be use by IHM to show or not some features.
	 *
	 * @param keyConcept secured data to check
	 * @return operations list
	 * @param <K> keyConcept type
	 */
	<K extends KeyConcept> List<String> getAuthorizedOperations(final K keyConcept);

}
