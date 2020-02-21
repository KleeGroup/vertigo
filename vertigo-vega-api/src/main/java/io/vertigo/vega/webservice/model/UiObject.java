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
package io.vertigo.vega.webservice.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * UiObject is used as an Input buffer from client.
 * It managed to :
 * - merge a serverSideObject and an inputBufferObject
 * - check validators
 * - return merged Object
 *
 * @author pchretien, npiedeloup
 * @param <D> DtObject type
 */
public interface UiObject<D extends DtObject> extends Serializable {

	/**
	 * @return DtDefinition de l'objet métier
	 */
	DtDefinition getDtDefinition();

	/**
	 * @param inputKey Object reference keep in this request context (for error handling)
	 */
	void setInputKey(final String inputKey);

	/**
	 * @return Object reference keep in this request context (for error handling)
	 */
	String getInputKey();

	/**
	 * @return the version stored in the server.
	 */
	D getServerSideObject();

	/**
	 * Affect the server's version.
	 * @param dtObject the objet held by the server
	 */
	void setServerSideObject(D dtObject);

	String getServerSideToken();

	void setServerSideToken(String asString);

	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return if the object is valid (no format errors) if it's not valid you must not call mergeAndCheckInput
	 */
	boolean checkFormat(final UiMessageStack uiMessageStack);

	/**
	 * Merge et Valide l'objet d'IHM et place les erreurs rencontrées dans la stack.
	 * @param dtObjectValidators Validateurs à utiliser, peut-Ãªtre spécifique Ã  l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise Ã  jour
	 * @return Objet métier mis Ã  jour
	 */
	D mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack);

	/**
	 * @param fieldName Champs
	 * @return Si le champs à été modifié dans le UiObject
	 */
	boolean isModified(final String fieldName);

	/**
	 * @return if object is modified (anyfield)
	 */
	boolean isModified();

	/**
	 * @return All modified fieldNames (camel)
	 */
	Set<String> getModifiedFields();

	/**
	 * Get the value in the buffer.
	 * @param fieldName name of the field (camelCase)
	 * @return the value in the buffer
	 */
	String getInputValue(String fieldName);

	/**
	 * Set the value in the buffer.
	 * @param fieldName name of the field (camelCase)
	 * @param stringValue the value as String
	 */
	void setInputValue(String fieldName, String stringValue);

	/**
	 * Set a typed value in the buffer.
	 * @param fieldName name of the field (camelCase)
	 * @param value the typed value
	 */
	void setTypedValue(String fieldName, Serializable value);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	 */
	Integer getInteger(String fieldName);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	 */
	Long getLong(String fieldName);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	  */
	String getString(String fieldName);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	 */
	Boolean getBoolean(String fieldName);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	 */
	Date getDate(String fieldName);

	/**
	 * @param fieldName Nom du champs
	 * @return Valeur typée
	 */
	BigDecimal getBigDecimal(String fieldName);

}
