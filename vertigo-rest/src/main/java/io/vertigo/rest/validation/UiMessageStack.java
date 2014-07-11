/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.rest.validation;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class d'enregistrement des messages. 
 * @author npiedeloup
 */
public final class UiMessageStack {

	private final List<String> globalErrorMessages = new ArrayList<>();
	private final List<String> globalWarningMessages = new ArrayList<>();
	private final List<String> globalInfoMessages = new ArrayList<>();

	private final Map<String, List<String>> fieldErrorMessages = new HashMap<>();
	private final Map<String, List<String>> fieldWarningMessages = new HashMap<>();
	private final Map<String, List<String>> fieldInfoMessages = new HashMap<>();

	/**
	 * Niveau du message.
	 * @author npiedeloup
	 */
	public static enum Level {
		/** Erreur. */
		ERROR,
		/** Warning. */
		WARNING,
		/** Info. */
		INFO;
	}

	/**
	 * Constructor.
	 */
	public UiMessageStack() {
		//nothing
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 */
	public final void addGlobalMessage(final Level level, final String message) {
		switch (level) {
			case ERROR:
				globalErrorMessages.add(message);
				break;
			case WARNING:
				globalWarningMessages.add(message);
				break;
			case INFO:
				globalInfoMessages.add(message);
				break;
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
	}

	/**
	 * @param message Message d'erreur
	 */
	public final void error(final String message) {
		addGlobalMessage(Level.ERROR, message);
	}

	/**
	 * @param message Message d'alerte
	 */
	public final void warning(final String message) {
		addGlobalMessage(Level.WARNING, message);
	}

	/**
	 * @param message Message d'info
	 */
	public final void info(final String message) {
		addGlobalMessage(Level.INFO, message);
	}

	/**
	 * @param message Message d'erreur
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void error(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.ERROR, message, dto, fieldName);
	}

	/**
	 * @param message Message d'alerte
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void warning(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.WARNING, message, dto, fieldName);
	}

	/**
	 * @param message Message d'info
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void info(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.INFO, message, dto, fieldName);
	}

	public final void addFieldMessage(final Level level, final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(level, message, DtObjectUtil.findDtDefinition(dto).getClassSimpleName(), fieldName);
	}

	public final void addFieldMessage(final Level level, final String message, final String contextKey, final String fieldName) {
		final Map<String, List<String>> fieldMessageMap;
		switch (level) {
			case ERROR:
				fieldMessageMap = fieldErrorMessages;
				break;
			case WARNING:
				fieldMessageMap = fieldWarningMessages;
				break;
			case INFO:
				fieldMessageMap = fieldInfoMessages;
				break;
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
		final String fieldKey = contextKey + "." + fieldName;
		List<String> messages = fieldMessageMap.get(fieldKey);
		if (messages == null) {
			messages = new ArrayList<>();
			fieldMessageMap.put(fieldKey, messages);
		}
		messages.add(message);
	}

	public boolean hasErrors() {
		return !globalErrorMessages.isEmpty() || !fieldErrorMessages.isEmpty();
	}

}