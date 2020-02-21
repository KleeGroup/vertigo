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
package io.vertigo.account.impl.security;

import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.LocaleProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Implementation standard de la gestion centralisee de la UserSession.
 *
 * @author npiedeloup
 */
public final class VSecurityManagerImpl implements VSecurityManager, Activeable {

	/**
	 * Thread local portant la session utilisteur.
	 * Utilisateur courant > peut etre null.
	 */
	private static final ThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new ThreadLocal<>();

	private final LocaleManager localeManager;
	private final String userSessionClassName;

	/**
	 * Constructeur.
	 * Les deux namespace ne sont pas types pour eviter des couplages forts (notamment sur UI).
	 * @param localeManager Manager des messages localises
	 * @param userSessionClassName ClassName de l'objet de session utilisateur
	 */
	@Inject
	public VSecurityManagerImpl(final LocaleManager localeManager, @ParamValue("userSessionClassName") final String userSessionClassName) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkArgNotEmpty(userSessionClassName);
		//-----
		this.localeManager = localeManager;
		this.userSessionClassName = userSessionClassName;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		localeManager.registerLocaleProvider(createLocaleProvider());
		localeManager.registerZoneProvider(createZoneIdProvider());
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	@Override
	public <U extends UserSession> U createUserSession() {
		return (U) ClassUtil.newInstance(userSessionClassName);
	}

	private LocaleProvider createLocaleProvider() {
		return () -> getCurrentUserSession()
				.map(userSession -> userSession.getLocale())
				.orElse(null);
	}

	private Supplier<ZoneId> createZoneIdProvider() {
		return () -> getCurrentUserSession()
				.map(userSession -> userSession.getZoneId())
				.orElse(null);
	}

	/** {@inheritDoc} */
	@Override
	public void startCurrentUserSession(final UserSession user) {
		Assertion.checkNotNull(user);
		//On verifie que la UserSession précédante a bien été retiree (securite et memoire).
		if (USER_SESSION_THREAD_LOCAL.get() != null) {
			throw new IllegalStateException("UserSession already created in this thread, check to close session by stopCurrentUserSession in a finally");
		}
		//-----
		USER_SESSION_THREAD_LOCAL.set(user);
	}

	/** {@inheritDoc} */
	@Override
	public void stopCurrentUserSession() {
		USER_SESSION_THREAD_LOCAL.remove();
	}

	/** {@inheritDoc} */
	@Override
	public <U extends UserSession> Optional<U> getCurrentUserSession() {
		final U userSession = (U) USER_SESSION_THREAD_LOCAL.get();
		return Optional.ofNullable(userSession);
	}

}
