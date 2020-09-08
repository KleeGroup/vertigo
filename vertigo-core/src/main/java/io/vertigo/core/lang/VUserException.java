/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.io.Serializable;

import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;

/**
 * Root Class for all user exceptions.
 * User Exceptions are built with a (localized) message
 *
 * A cause can be added by setting 'initCause' method
 *
 * @author fconstantin, pchretien
 */
public class VUserException extends RuntimeException {
	private static final long serialVersionUID = 33066445931128456L;
	private final MessageText messageText;

	/**
	 * Constructor.
	 * @param messageText Message de l'exception
	 */
	public VUserException(final MessageText messageText) {
		//Attention il convient d'utiliser une méthode qui ne remonte d'exception.
		super(messageText.getDisplay());
		// On rerentre sur l'API des Exception en passant le message.
		this.messageText = messageText;
	}

	/**
	 * Constructor.
	 * @param defaultMsg the default msg (required)
	 * @param params  list of params (optional)
	 */
	public VUserException(final String defaultMsg, final Serializable... params) {
		this((MessageText.of(defaultMsg, params)));
	}

	/**
	 * Constructor.
	 * @param key  the msg key (required)
	 * @param params  list of params (optional)
	 */
	public VUserException(final MessageKey key, final Serializable... params) {
		this((MessageText.of(key, params)));
	}

	/**
	 * Gestion des messages d'erreur externalisés.
	 * @return messageText.
	 */
	public final MessageText getMessageText() {
		return messageText;
	}
}
