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

import java.util.Set;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Validator of DtObject.
 * Could check an object, for a modified fields set and append detected errors in an DtObjectErrors.
 * @author npiedeloup
 * @param <O> Type of DtObject
 */
public interface DtObjectValidator<O extends DtObject> {

	/**
	 * Effectue les validations prévu d'un objet.
	 * @param dtObject Objet à tester
	 * @param modifiedFieldNames Liste des champs modifiés
	 * @param dtObjectErrors Pile des erreurs
	 */
	void validate(O dtObject, Set<String> modifiedFieldNames, DtObjectErrors dtObjectErrors);

}
