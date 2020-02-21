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

import java.lang.reflect.Parameter;
import java.util.Arrays;

import javax.inject.Inject;

import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.account.authorization.annotations.Secured;
import io.vertigo.account.authorization.annotations.SecuredOperation;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.lang.Assertion;

/**
 * Aspect pour la gestion des Secured au niveau de la couche service.
 * @author npiedeloup
 */
public final class AuthorizationAspect implements Aspect {
	private final AuthorizationManager authorizationManager;

	/**
	 * Constructor
	 * @param authorizationManager the authorizationManager
	 */
	@Inject
	public AuthorizationAspect(final AuthorizationManager authorizationManager) {
		Assertion.checkNotNull(authorizationManager);
		//-----
		this.authorizationManager = authorizationManager;
	}

	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation methodInvocation) {
		final Secured secured = methodInvocation.getMethod().getAnnotation(Secured.class) == null
				? methodInvocation.getMethod().getDeclaringClass().getAnnotation(Secured.class)
				: methodInvocation.getMethod().getAnnotation(Secured.class);

		Assertion.checkNotNull(secured, "No Aspect if not @Secured (on {0})", methodInvocation.getMethod());
		final AuthorizationName[] authorizationNames = Arrays.stream(secured.value()).map(value -> (AuthorizationName) () -> Authorization.PREFIX + value).toArray(AuthorizationName[]::new);
		if (!authorizationManager.hasAuthorization(authorizationNames)) {
			throw new VSecurityException(MessageText.of("Not enought authorizations"));//no too sharp info here : may use log
		}
		final Parameter[] parameters = methodInvocation.getMethod().getParameters();
		for (int i = 0; i < args.length; i++) {
			final Parameter parameter = parameters[i];
			final SecuredOperation securedOperation = parameter.getAnnotation(SecuredOperation.class);
			//On repère les paramètres qui ont le @SecuredOperation
			if (securedOperation != null) {
				//Ils doivent être de type KeyConcept (et même securedEntity mais il y aura une exception dans le isAuthorized)
				Assertion.checkArgument(args[i] instanceof KeyConcept, "Can't check authorization on arg{0} ({1})", i, args[i]);
				if (!authorizationManager.isAuthorized((KeyConcept) args[i], securedOperation::value)) {
					throw new VSecurityException(MessageText.of("Not enought authorizations"));//no too sharp info here : may use log
				}
			}
		}
		return methodInvocation.proceed(args);
	}

	@Override
	public Class<Secured> getAnnotationType() {
		return Secured.class;
	}
}
