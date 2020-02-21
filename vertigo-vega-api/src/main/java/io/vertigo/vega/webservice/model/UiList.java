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

import java.util.List;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public interface UiList<D extends DtObject> extends List<UiObject<D>> {

	/**
	 * @return the object type of the list
	 *
	 */
	Class<D> getObjectType();

	/**
	 * @return the definition of the elements of the list
	 */
	DtDefinition getDtDefinition();

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier valid�e.
	 */
	DtList<D> mergeAndCheckInput(List<DtObjectValidator<D>> dtObjectValidators, UiMessageStack uiMessageStack);

	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return if the object is valid (no format errors) if it's not valid you must not call mergeAndCheckInput
	 */
	boolean checkFormat(UiMessageStack uiMessageStack);

}
