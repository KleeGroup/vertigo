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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.locale.MessageText;
import io.vertigo.lang.Assertion;

/**
 * Liste des erreurs d'un objet métier.
 * @author pchretien, npiedeloup
 */
public final class DtObjectErrors {

	private final List<MessageText> objectErrors = new ArrayList<>();
	private final Map<String, List<MessageText>> fieldsErrors = new LinkedHashMap<>();

	/**
	 * Returns if the object has any error
	 * @return true if the object has at least one error (global or field related)
	 */
	public boolean hasError() {
		return !objectErrors.isEmpty() || !fieldsErrors.isEmpty();
	}

	/**
	 * Returns if a given field has errors.
	 * @param fieldName the fieldName (camelCase)
	 * @return true if an error is linked to the field
	 */
	public boolean hasError(final String fieldName) {
		return fieldsErrors.containsKey(fieldName);
	}

	/**
	 * Clear all errors linked to a specific field
	 * @param fieldName the fieldName (camelCase)
	 */
	public void clearErrors(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		fieldsErrors.remove(fieldName);
	}

	/**
	 * Clears all errors.
	 */
	public void clearErrors() {
		objectErrors.clear();
		fieldsErrors.clear();
	}

	/**
	 * Add a global error on the object (for example two fields values that a not compatibles)
	 * @param messageText the error message
	 */
	public void addError(final MessageText messageText) {
		objectErrors.add(messageText);
	}

	/**
	 * Add an error associated to a field with a specific message
	 * @param fieldName the fieldName (camelCase) concerned by the error
	 * @param messageText the error message
	 */
	public void addError(final String fieldName, final MessageText messageText) {
		List<MessageText> errors = fieldsErrors.get(fieldName);
		if (errors == null) {
			errors = new ArrayList<>();
			fieldsErrors.put(fieldName, errors);
		}
		errors.add(messageText);
	}

	/**
	 * Flush all messages in the given messageStack
	 * @param contextKey the key that will be associated with the error (for display)
	 * @param uiMessageStack the stack in which messages must be stored
	 */
	public void flushIntoMessageStack(final String contextKey, final UiMessageStack uiMessageStack) {
		for (final MessageText errorMessage : objectErrors) {
			uiMessageStack.addGlobalMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay());
		}
		for (final Map.Entry<String, List<MessageText>> entry : fieldsErrors.entrySet()) {
			for (final MessageText errorMessage : entry.getValue()) {
				uiMessageStack.addFieldMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay(), contextKey, entry.getKey());
			}
		}
	}
}
