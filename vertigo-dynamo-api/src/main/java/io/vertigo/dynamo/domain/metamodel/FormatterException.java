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
package io.vertigo.dynamo.domain.metamodel;

import java.io.Serializable;

import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;

/**
 * Exception lancée en cas d'échec de formattage.
 *
 * @author pchretien
 */
public final class FormatterException extends Exception {
	private static final long serialVersionUID = -7317938262923785123L;
	private final MessageText messageText;

	/**
	 * Constructor.
	 *
	 * @param key Clé du message externalisé explicitant la raison du non formattage.
	 * @param params Paramètres de la ressource
	 */
	public FormatterException(final MessageKey key, final Serializable... params) {
		messageText = MessageText.of(key, params);
	}

	public MessageText getMessageText() {
		return messageText;
	}
}
