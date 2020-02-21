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
package io.vertigo.vega.impl.servlet.filter;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.Home;
import io.vertigo.vega.webservice.exception.SessionException;

/**
 * Filtre de gestion des sessions utilisateurs bindées sur HTTP.
 *
 * @author npiedeloup
 */
public final class SecurityFilter extends AbstractFilter {

	/**
	 * Nom de l'objet Session dans la session J2EE
	 */
	private static final String USER_SESSION = "io.vertigo.Session";

	private static final String NO_AUTHENTIFICATION_PATTERN_PARAM_NAME = "url-no-authentification";

	/**
	 * Le gestionnaire de sécurité
	 */
	private VSecurityManager securityManager;

	private Optional<Pattern> noAuthentificationPattern;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		securityManager = Home.getApp().getComponentSpace().resolve(VSecurityManager.class);
		noAuthentificationPattern = parsePattern(getFilterConfig().getInitParameter(NO_AUTHENTIFICATION_PATTERN_PARAM_NAME));
	}

	/** {@inheritDoc} */
	@Override
	public void doMyFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		doSecurityFilter(!isUrlMatch(req, noAuthentificationPattern), (HttpServletRequest) req, (HttpServletResponse) res, chain);
	}

	private void doSecurityFilter(final boolean needsAuthentification, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain)
			throws IOException, ServletException {
		final boolean hasSession = httpRequest.getSession(false) != null;

		// On récupère la session de l'utilisateur
		final UserSession user = obtainUserSession(httpRequest);

		try {
			// on place la session en ThreadLocal
			securityManager.startCurrentUserSession(user);

			// 1. Persistance de UserSession dans la session HTTP.
			bindUser(httpRequest, user);

			// 2. Vérification que l'utilisateur est authentifié si l'adresse demandée l'exige
			if (needsAuthentification && !user.isAuthenticated()) {
				/*
				 * il ne faut pas continuer
				 * - si la session a expiré
				 * - ou si aucune session utilisateur n'existe.
				 */
				if (!hasSession) {
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session Expired"); //No session found
					httpRequest.setAttribute("SessionExpired", true);
					throw new ServletException(new SessionException("Session Expired"));//will override the 401 error code and send a 500
				} else {
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED); //User not authenticated
				}
				//} else if (checkRequestAccess && needsAuthentification && false) { //TODO
				//	httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				chain.doFilter(httpRequest, httpResponse);
			}
		} finally {
			// On retire le user du ThreadLocal (il est déjà en session)
			securityManager.stopCurrentUserSession();
		}
	}

	// ==========================================================================
	// =================GESTION DE LA SESSION UTILISATEUR========================
	// ==========================================================================
	/**
	 * Lie l'utilisateur à la session en cours.
	 *
	 * @param request Request
	 * @param user User
	 */
	private static void bindUser(final HttpServletRequest request, final UserSession user) {
		final HttpSession session = request.getSession(true);
		final Object o = session.getAttribute(USER_SESSION);
		if (o == null || !o.equals(user)) {
			session.setAttribute(USER_SESSION, user);
		}
	}

	/**
	 * Retourne la session utilisateur.
	 *
	 * @return Session utilisateur
	 * @param request HTTPRequest
	 */
	private UserSession obtainUserSession(final HttpServletRequest request) {
		final HttpSession session = request.getSession(false);
		UserSession user = getUserSession(session);
		// Si la session user n'est pas créée on la crée
		if (user == null) {
			user = securityManager.createUserSession();
			if (session != null) {
				session.setAttribute(USER_SESSION, user);
			}
		}
		return user;
	}

	/**
	 * Récupération de l'utilisateur lié à la session.
	 *
	 * @param session HttpSession
	 * @return UserSession Utilisateur bindé sur la session (peut être null)
	 */
	private static UserSession getUserSession(final HttpSession session) {
		return session == null ? null : (UserSession) session.getAttribute(USER_SESSION);
	}
}
