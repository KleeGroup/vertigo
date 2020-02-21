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
package io.vertigo.vega.webservice.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;

/**
 * Class d'enregistrement des messages.
 * @author npiedeloup
 */
public final class VegaUiMessageStack implements UiMessageStack {

	private static final long serialVersionUID = -2650689827844300786L;

	private final List<String> globalErrors = new ArrayList<>();
	private final List<String> globalWarnings = new ArrayList<>();
	private final List<String> globalInfos = new ArrayList<>();
	private final List<String> globalSuccess = new ArrayList<>();

	private final Map<String, List<String>> fieldErrors = new HashMap<>();
	private final Map<String, List<String>> fieldWarnings = new HashMap<>();
	private final Map<String, List<String>> fieldInfos = new HashMap<>();

	private final Map<String, Map<String, List<String>>> objectFieldErrors = new HashMap<>();
	private final Map<String, Map<String, List<String>>> objectFieldWarnings = new HashMap<>();
	private final Map<String, Map<String, List<String>>> objectFieldInfos = new HashMap<>();

	@JsonExclude
	private final transient UiContextResolver uiContextResolver;

	/**
	 * Constructor.
	 * @param uiContextResolver Resolver object to contextKey in request
	 */
	public VegaUiMessageStack(final UiContextResolver uiContextResolver) {
		Assertion.checkNotNull(uiContextResolver);
		//-----
		this.uiContextResolver = uiContextResolver;
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 */
	@Override
	public void addGlobalMessage(final Level level, final String message) {
		switch (level) {
			case ERROR:
				globalErrors.add(message);
				break;
			case WARNING:
				globalWarnings.add(message);
				break;
			case INFO:
				globalInfos.add(message);
				break;
			case SUCCESS:
				globalSuccess.add(message);
				break;
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
	}

	/**
	 * @param message Message d'erreur
	 */
	@Override
	public void error(final String message) {
		addGlobalMessage(Level.ERROR, message);
	}

	/**
	 * @param message Message d'alerte
	 */
	@Override
	public void warning(final String message) {
		addGlobalMessage(Level.WARNING, message);
	}

	/**
	 * @param message Message d'info
	 */
	@Override
	public void info(final String message) {
		addGlobalMessage(Level.INFO, message);
	}

	/**
	 * @param message Message d'info
	 */
	@Override
	public void success(final String message) {
		addGlobalMessage(Level.SUCCESS, message);
	}

	/**
	 * @param message Message d'erreur
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	@Override
	public void error(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.ERROR, message, dto, fieldName);
	}

	/**
	 * @param message Message d'alerte
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	@Override
	public void warning(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.WARNING, message, dto, fieldName);
	}

	/**
	 * @param message Message d'info
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	@Override
	public void info(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.INFO, message, dto, fieldName);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.validation.UiMessageStack#addFieldMessage(io.vertigo.vega.webservice.validation.UiMessageStack.Level, java.lang.String, io.vertigo.dynamo.domain.model.DtObject, java.lang.String)
	 */
	@Override
	public void addFieldMessage(final Level level, final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(level, message, uiContextResolver.resolveContextKey(dto), fieldName);

	}

	/**
	 * @param level Message level
	 * @param message Message text
	 * @param contextKey contextKey in request
	 * @param fieldName field name
	 */
	@Override
	public void addFieldMessage(final Level level, final String message, final String contextKey, final String fieldName) {
		if (contextKey.isEmpty()) {
			addFieldMessage(level, message, fieldName);
		} else {
			addObjectFieldMessage(level, message, contextKey, fieldName);
		}
	}

	private void addFieldMessage(final Level level, final String message, final String fieldName) {
		final Map<String, List<String>> fieldMessageMap;
		switch (level) {
			case ERROR:
				fieldMessageMap = fieldErrors;
				break;
			case WARNING:
				fieldMessageMap = fieldWarnings;
				break;
			case INFO:
				fieldMessageMap = fieldInfos;
				break;
			case SUCCESS: //unsupported for fields
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
		final String fieldKey = fieldName;
		List<String> messages = fieldMessageMap.get(fieldKey);
		if (messages == null) {
			messages = new ArrayList<>();
			fieldMessageMap.put(fieldKey, messages);
		}
		messages.add(message);
	}

	private void addObjectFieldMessage(final Level level, final String message, final String contextKey, final String fieldName) {
		final Map<String, Map<String, List<String>>> fieldMessageMap;
		switch (level) {
			case ERROR:
				fieldMessageMap = objectFieldErrors;
				break;
			case WARNING:
				fieldMessageMap = objectFieldWarnings;
				break;
			case INFO:
				fieldMessageMap = objectFieldInfos;
				break;
			case SUCCESS: //unsupported for fields
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
		Map<String, List<String>> objectMessages = fieldMessageMap.get(contextKey);
		if (objectMessages == null) {
			objectMessages = new HashMap<>();
			fieldMessageMap.put(contextKey, objectMessages);
		}
		List<String> messages = objectMessages.get(fieldName);
		if (messages == null) {
			messages = new ArrayList<>();
			objectMessages.put(fieldName, messages);
		}
		messages.add(message);
	}

	/**
	 * @return if there are errors in this stack.
	 */
	@Override
	public boolean hasErrors() {
		return !globalErrors.isEmpty() || !fieldErrors.isEmpty() || !objectFieldErrors.isEmpty();
	}

}
