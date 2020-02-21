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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * This class list User's Authorizations.
 *
 * @author  pchretien, npiedeloup
 */
public final class UserAuthorizations implements Serializable {

	private static final long serialVersionUID = -7924146007592711123L;

	/**
	 * Global authorizations list of this user.
	 */
	private final Map<String, DefinitionReference<Authorization>> authorizationRefs = new HashMap<>();

	/**
	 * KeyConcept dependent authorizations list by keyConcept of this user.
	 */
	private final Map<DefinitionReference<DtDefinition>, Set<DefinitionReference<Authorization>>> authorizationMapRefs = new HashMap<>();

	/**
	 * Accepted roles for this user.
	 * Use for asc-compatibility.
	 */
	private final Set<DefinitionReference<Role>> roleRefs = new HashSet<>();

	private final Map<String, List<Serializable>> mySecurityKeys = new HashMap<>();

	//===========================================================================
	//=======================GESTION DES ROLES===================================
	//===========================================================================
	/**
	 * Add a role to this User.
	 * Role must be previously declared.
	 *
	 * @param role Role to add
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations addRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		roleRefs.add(new DefinitionReference<>(role));
		return this;
	}

	/**
	 * Return roles set of this user.
	 * @return roles set
	 */
	public Set<Role> getRoles() {
		return roleRefs.stream()
				.map(DefinitionReference::get)
				.collect(Collectors.toSet());
	}

	/**
	 * @param role Role
	 * @return if user has this role
	 */
	public boolean hasRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		return roleRefs.contains(new DefinitionReference<>(role));
	}

	/**
	 * Clear all roles on this user. (but only roles : authorizations aren't cleared)
	 * Warning : no more rights after that.
	 */
	public void clearRoles() {
		roleRefs.clear();
	}

	/**
	 * Add a authorization to this User.
	 * Authorization must be previously declared.
	 *
	 * @param authorization Authorization to add
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations addAuthorization(final Authorization authorization) {
		Assertion.checkNotNull(authorization);
		//-----
		authorizationRefs.put(authorization.getName(), new DefinitionReference<>(authorization));
		if (authorization.getEntityDefinition().isPresent()) {
			authorizationMapRefs.computeIfAbsent(new DefinitionReference<>(authorization.getEntityDefinition().get()), key -> new HashSet<>())
					.add(new DefinitionReference<>(authorization));
			for (final Authorization grantedAuthorization : authorization.getGrants()) {
				if (!hasAuthorization(grantedAuthorization::getName)) { //On test pour ne pas créer de boucle
					addAuthorization(grantedAuthorization);
				}
			}
		}
		return this;
	}

	/**
	 * Return authorizations set for this type of entity.
	 *
	 * @param entityDefinition Entity definition
	 * @return Authorizations set
	 */
	public Set<Authorization> getEntityAuthorizations(final DtDefinition entityDefinition) {
		final Set<DefinitionReference<Authorization>> entityAuthorizationRefs = authorizationMapRefs.get(new DefinitionReference<>(entityDefinition));
		if (entityAuthorizationRefs != null) {
			return entityAuthorizationRefs.stream()
					.map(DefinitionReference::get)
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	/**
	 * @param authorizationNamse Authorization
	 * @return true if user has this authorization
	 */
	public boolean hasAuthorization(final AuthorizationName... authorizationNames) {
		Assertion.checkNotNull(authorizationNames);
		//-----
		return Arrays.stream(authorizationNames)
				.anyMatch(authorizationName -> authorizationRefs.containsKey(authorizationName.name()));
	}

	/**
	 * Clear all authorization on this user. (but only authorization : roles aren't cleared)
	 * Warning : no more rights after that.
	 */
	public void clearAuthorizations() {
		authorizationRefs.clear();
		authorizationMapRefs.clear();
	}

	/**
	 * Return the security keys of this user.
	 * Used for data dependent security rules.
	 * @return User's security keys.
	 */
	public Map<String, List<Serializable>> getSecurityKeys() {
		return mySecurityKeys;
	}

	/**
	 * Add a security key part of his security perimeter.
	 * A security key can be multi-valued (then withSecurityKeys is call multiple times).
	 * Value should be an array if this securityKey is a tree (hierarchical) key.
	 *
	 * @param securityKey Name
	 * @param value Value
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations withSecurityKeys(final String securityKey, final Serializable value) {
		mySecurityKeys.computeIfAbsent(securityKey, v -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * Clear Security Keys.
	 * Use when user change it security perimeter.
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations clearSecurityKeys() {
		mySecurityKeys.clear();
		return this;
	}
}
