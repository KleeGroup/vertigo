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
package io.vertigo.account.impl.authorization;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.authorization.UserAuthorizations;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.metamodel.SecuredEntity;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression;
import io.vertigo.account.impl.authorization.dsl.translator.CriteriaSecurityRuleTranslator;
import io.vertigo.account.impl.authorization.dsl.translator.SearchSecurityRuleTranslator;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * Main authorizations manager implementation.
 * @author npiedeloup
 */
public final class AuthorizationManagerImpl implements AuthorizationManager {
	private static final String USER_SESSION_ACL_KEY = "vertigo.account.authorizations";

	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security manager
	 */
	@Inject
	public AuthorizationManagerImpl(final VSecurityManager securityManager) {
		Assertion.checkNotNull(securityManager);
		//-----
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	@Override
	public UserAuthorizations obtainUserAuthorizations() {
		return getUserPermissionsOpt().orElseThrow(() -> new IllegalArgumentException("Can't getUserPermissions, check your have create an UserSession before."));

	}

	private Optional<UserAuthorizations> getUserPermissionsOpt() {
		final Optional<UserSession> userSessionOpt = securityManager.getCurrentUserSession();
		if (!userSessionOpt.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Optional.empty();
		}
		UserAuthorizations userAuthorizations = userSessionOpt.get().getAttribute(USER_SESSION_ACL_KEY);
		if (userAuthorizations == null) {
			userAuthorizations = new UserAuthorizations();
			userSessionOpt.get().putAttribute(USER_SESSION_ACL_KEY, userAuthorizations);
		}
		return Optional.of(userAuthorizations);

	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAuthorization(final AuthorizationName... permissionNames) {
		Assertion.checkNotNull(permissionNames);
		//---
		return getUserPermissionsOpt()
				.map(userPermissions -> userPermissions.hasAuthorization(permissionNames))
				// Si il n'y a pas de userPermissions alors pas d'autorisation.
				.orElse(false);

	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> boolean isAuthorized(final K keyConcept, final OperationName<K> operationName) {
		Assertion.checkNotNull(keyConcept);
		Assertion.checkNotNull(operationName);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserPermissionsOpt();
		if (!userPermissionsOpt.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return false;
		}

		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		return userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operationName.name())
						|| permission.getOverrides().contains(operationName.name()))
				.flatMap(permission -> permission.getRules().stream())
				.anyMatch(rule -> new CriteriaSecurityRuleTranslator<K>()
						.on(securedEntity)
						.withRule(rule)
						.withCriteria(userPermissions.getSecurityKeys())
						.toCriteria()
						.toPredicate().test(keyConcept));
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> Criteria<K> getCriteriaSecurity(final Class<K> keyConceptClass, final OperationName<K> operation) {
		Assertion.checkNotNull(keyConceptClass);
		Assertion.checkNotNull(operation);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserPermissionsOpt();
		if (!userPermissionsOpt.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Criterions.alwaysFalse();
		}

		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		final List<Criteria<K>> criterions = userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operation.name())
						|| permission.getOverrides().contains(operation.name()))
				.flatMap(permission -> permission.getRules().stream())
				.map(rule -> new CriteriaSecurityRuleTranslator<K>()
						.on(securedEntity)
						.withRule(rule)
						.withCriteria(userPermissions.getSecurityKeys())
						.toCriteria())
				.collect(Collectors.toList());

		if (criterions.isEmpty()) {
			// Si il n'y a pas de droits alors pas d'autorisation.
			return Criterions.alwaysFalse();
		}

		Criteria<K> securityCriteria = null;
		for (final Criteria<K> ruleCriteria : criterions) {
			if (securityCriteria == null) {
				securityCriteria = ruleCriteria;
			} else {
				securityCriteria = securityCriteria.or(ruleCriteria);
			}
		}
		return securityCriteria;
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> String getSearchSecurity(final Class<K> keyConceptClass, final OperationName<K> operationName) {
		Assertion.checkNotNull(keyConceptClass);
		Assertion.checkNotNull(operationName);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserPermissionsOpt();
		if (!userPermissionsOpt.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return ""; //Attention : pas de *:*
		}

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final SearchSecurityRuleTranslator securityRuleTranslator = new SearchSecurityRuleTranslator()
				.on(securedEntity)
				.withCriteria(userPermissions.getSecurityKeys());

		final List<Authorization> permissions = userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operationName.name()))
				.collect(Collectors.toList());
		for (final Authorization permission : permissions) {
			for (final RuleMultiExpression ruleExpression : permission.getRules()) {
				securityRuleTranslator.withRule(ruleExpression);
			}
		}
		return securityRuleTranslator.toSearchQuery();
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> List<String> getAuthorizedOperations(final K keyConcept) {
		Assertion.checkNotNull(keyConcept);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserPermissionsOpt();
		if (!userPermissionsOpt.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Collections.emptyList();
		}
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		return userPermissionsOpt.get().getEntityAuthorizations(dtDefinition).stream()
				.map(permission -> permission.getOperation().get())
				.collect(Collectors.toList());
	}

	/**
	 * Finds the SecuredEntity from a type of 'dtDefinition'
	 * @param dtDefinition the 'dtDefinition'
	 * @return SecuredEntity
	 */
	public static SecuredEntity findSecuredEntity(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---
		final String name = DefinitionUtil.getPrefix(SecuredEntity.class) + dtDefinition.getName();
		return Home.getApp().getDefinitionSpace().resolve(name, SecuredEntity.class);
	}

}
