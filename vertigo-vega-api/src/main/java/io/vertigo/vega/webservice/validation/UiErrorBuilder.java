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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.validation.UiMessageStack.Level;

/**
 * Pile de message d'erreur.
 * @author npiedeloup
 */
public final class UiErrorBuilder {
	private final List<UiError> uiObjectErrors = new ArrayList<>();
	private final Map<DtObject, Set<DtField>> uiErrorIndex = new HashMap<>();

	/**
	 * @return Si il y a des erreurs
	 */
	public boolean hasError() {
		return !uiObjectErrors.isEmpty();
	}

	/**
	 * @param dtObject Objet
	 * @return Si l'objet a des erreurs
	 */
	public boolean hasError(final DtObject dtObject) {
		return !obtainUiErrorIndex(dtObject).isEmpty();
	}

	private Set<DtField> obtainUiErrorIndex(final DtObject dtObject) {
		Set<DtField> dtFieldError = uiErrorIndex.get(dtObject);
		if (dtFieldError == null) {
			dtFieldError = new HashSet<>();
			uiErrorIndex.put(dtObject, dtFieldError);
		}
		return dtFieldError;
	}

	/**
	 * @param dtObject Objet
	 * @param dtField Champ
	 * @return si le champ de l'objet porte des erreurs
	 */
	public boolean hasError(final DtObject dtObject, final DtField dtField) {
		return obtainUiErrorIndex(dtObject).contains(dtField);
	}

	/**
	 * Vide les erreurs d'un objet
	 * @param dtObject Objet
	 */
	void clearErrors(final DtObject dtObject) {
		uiObjectErrors.removeIf(uiError -> uiError.getDtObject().equals(dtObject));
		obtainUiErrorIndex(dtObject).clear();
	}

	/**
	 * Vide les erreurs d'un champ
	 * @param dtObject Objet
	 * @param dtField Champ
	 */
	void clearErrors(final DtObject dtObject, final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//-----
		uiObjectErrors.removeIf(uiError -> uiError.getDtObject().equals(dtObject) && uiError.getDtField().equals(dtField));
		obtainUiErrorIndex(dtObject).remove(dtField);
	}

	/**
	 * Ajoute une erreur sur le champ d'un objet.
	 * @param dtObject Objet porteur de l'erreur
	 * @param dtField Champ porteur de l'erreur
	 * @param messageText Message d'erreur
	 */
	public void addError(final DtObject dtObject, final DtField dtField, final MessageText messageText) {
		uiObjectErrors.add(new UiError(dtObject, dtField, messageText));
		obtainUiErrorIndex(dtObject).add(dtField);
	}

	/**
	 * Ajoute une erreur sur le champ d'un objet.
	 * @param dtObject Objet porteur de l'erreur
	 * @param fieldName Champ porteur de l'erreur
	 * @param messageText Message d'erreur
	 */
	public void addError(final DtObject dtObject, final String fieldName, final MessageText messageText) {
		addError(dtObject, getDtField(dtObject, fieldName), messageText);
	}

	/**
	 * Vérifie l'égalité des champs.
	 * @param dto Object a tester
	 * @param fieldName1 Champs 1
	 * @param fieldName2 Champs 2
	 * @param messageText Message à appliquer si erreur
	 */
	public void checkFieldEquals(final DtObject dto, final String fieldName1, final String fieldName2, final MessageText messageText) {
		final DtField dtField1 = getDtField(dto, fieldName1);
		final DtField dtField2 = getDtField(dto, fieldName2);
		final Object value1 = getValue(dto, dtField1);
		final Object value2 = getValue(dto, dtField2);
		//value1 et value2 == null ou value1 equals value2, sinon error
		if (value1 != null && !value1.equals(value2) || value1 == null && value2 != null) {
			addError(dto, dtField2, messageText);
		}
	}

	/**
	 * Vérifie que la date du champ 2 est après (strictement) la date du champ 1.
	 * @param dto Object a tester
	 * @param fieldName1 Champs 1
	 * @param fieldName2 Champs 2
	 * @param messageText Message à appliquer si erreur
	 */
	public void checkFieldDateAfter(final DtObject dto, final String fieldName1, final String fieldName2, final MessageText messageText) {
		final DtField dtField1 = getDtField(dto, fieldName1);
		final DtField dtField2 = getDtField(dto, fieldName2);
		final LocalDate value1 = (LocalDate) getValue(dto, dtField1); //la valeur typée peut être null
		final LocalDate value2 = (LocalDate) getValue(dto, dtField2);
		if (value1 != null && value2 != null && !value2.isAfter(value1)) {
			addError(dto, dtField2, messageText);
		}
	}

	/**
	 * Vérifie que le Long du champ 2 est après (strictement) le Long du champ 1.
	 * @param dto Object a tester
	 * @param fieldName1 Champs 1
	 * @param fieldName2 Champs 2
	 * @param messageText Message à appliquer si erreur
	 */
	public void checkFieldLongAfter(final DtObject dto, final String fieldName1, final String fieldName2, final MessageText messageText) {
		final DtField dtField1 = getDtField(dto, fieldName1);
		final DtField dtField2 = getDtField(dto, fieldName2);
		final Long value1 = (Long) getValue(dto, dtField1); //la valeur typée peut être null
		final Long value2 = (Long) getValue(dto, dtField2);
		if (value1 != null && value2 != null && !(value2.compareTo(value1) > 0)) {
			addError(dto, dtField2, messageText);
		}
	}

	/**
	 * Vérifie que le champ est renseigner.
	 * @param dto Object a tester
	 * @param fieldName Champs
	 * @param messageText Message à appliquer si erreur
	 */
	public void checkFieldNotNull(final DtObject dto, final String fieldName, final MessageText messageText) {
		final DtField dtField = getDtField(dto, fieldName);
		final Object value = getValue(dto, dtField);
		if (value == null || value.toString().isEmpty()) {
			addError(dto, dtField, messageText);
		}
	}

	private static Object getValue(final DtObject dto, final DtField dtField) {
		return dtField.getDataAccessor().getValue(dto);
	}

	private static DtField getDtField(final DtObject dto, final String fieldName) {
		return DtObjectUtil.findDtDefinition(dto).getField(fieldName);
	}

	/**
	 * @throws ValidationUserException Si il y a des erreurs
	 */
	public void throwUserExceptionIfErrors() {
		if (!uiObjectErrors.isEmpty()) {
			throw new ValidationUserException(uiObjectErrors);
		}
	}

	/**
	 * Envoi le contenu des messages du validator dans la UiMessageStack.
	 * @param uiMessageStack Pile des message affichée.
	 */
	public void flushIntoMessageStack(final UiMessageStack uiMessageStack) {
		for (final UiError uiError : uiObjectErrors) {
			uiMessageStack.addFieldMessage(Level.ERROR, uiError.getErrorMessage().getDisplay(), uiError.getDtObject(), uiError.getFieldName());
		}
	}

}
