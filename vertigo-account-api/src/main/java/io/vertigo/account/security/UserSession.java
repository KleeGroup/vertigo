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

import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Session d'un utilisateur.
 * Un utilisateur
 * <ul>
 * <li>est authentifie ou non,</li>
 * <li>possède une liste d'attributs serialisables</li>.
 * </ul>
 *
 * @author alauthier, pchretien, npiedeloup
 */
public abstract class UserSession implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 467617818948129397L;

	/**
	 * Cle de la session (utilise comme cle unique de connexion).
	 */
	private final UUID sessionUUID = createUUID();

	/**
	 * Attributs supplémentaires associées à la session.
	 */
	private final Map<String, Serializable> attributes = new HashMap<>();

	/**
	 * Indique si l'utilisateur est authentifie.
	 * Par defaut l'utilisateur n'est pas authentifie.
	 */
	private boolean authenticated;

	private static UUID createUUID() {
		//On utilise le mecanisme de creation standard.
		return UUID.randomUUID();
	}

	//===========================================================================
	//=======================GESTION DES AUTHENTIFICATIONS=======================
	//===========================================================================

	/**
	 * @return UUID Indentifiant unique de cette connexion.
	 */
	public final UUID getSessionUUID() {
		return sessionUUID;
	}

	/**
	 * Indique si l'utilisateur est authentifie.
	 * L'authentification est actée par l'appel de la méthode <code>authenticate()</code>
	 *
	 * @return boolean Si l'utilisateur s'est authentifié.
	 */
	public final boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * Méthode permettant d'indiquer que l'utilisateur est authentifié.
	 */
	public final void authenticate() {
		authenticated = true;
	}

	/**
	 * Méthode permettant d'indiquer que l'utilisateur n'est plus authentifié.
	 */
	public final void logout() {
		authenticated = false;
	}

	/**
	 * Ajout d'attribut supplémentaire.
	 * @param key Key
	 * @param value Value
	 */
	public final void putAttribute(final String key, final Serializable value) {
		attributes.put(key, value);
	}

	/**
	 * Get d'attribut supplémentaire.
	 * @param key Key
	 * @param <O> Value type
	 * @return attribute value.
	 */
	public final <O extends Serializable> O getAttribute(final String key) {
		return (O) attributes.get(key);
	}

	/**
	 * Gestion multilingue.
	 * @return Locale associée à l'utilisateur.
	 */
	public abstract Locale getLocale();

	/**
	 * Gestion des times zones.
	 * Could be override.
	 * @return ZoneId associée à l'utilisateur.
	 */
	public ZoneId getZoneId() {
		return null; //null mean use default
	}

}
