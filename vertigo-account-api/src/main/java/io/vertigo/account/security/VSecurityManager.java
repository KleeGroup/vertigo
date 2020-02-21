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
package io.vertigo.account.security;

import java.util.Optional;

import io.vertigo.core.component.Manager;

/**
 * Gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public interface VSecurityManager extends Manager {
	//=========================================================================
	//===============Gestion de l'utilisateur (porteur des droits)=============
	//=========================================================================
	/**
	 * Accroche l'utilisateur au thread courant.
	 * S'effectue dans la servlet. Ne pas utiliser directement.
	 * @param userSession Session utilisateur (not null)
	 */
	void startCurrentUserSession(final UserSession userSession);

	/**
	 * Reinitialise la session courante.
	 */
	void stopCurrentUserSession();

	/**
	 * Recuperation de la session utilisateur courante.
	 * @param <U> Session utilisateur
	 * @return Session utilisateur courante.
	 */
	<U extends UserSession> Optional<U> getCurrentUserSession();

	/**
	 * Creation de nouveaux utilisateurs.
	 * @param <U> Type de l'utilisateur
	 * @return Nouvel utilisateur
	 */
	<U extends UserSession> U createUserSession();
}
