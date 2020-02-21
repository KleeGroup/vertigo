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

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.ConstraintException;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Default DtObject validation : check domain's constraints on modified fields.
 * @author npiedeloup
 * @param <O> Type d'objet
 */
public final class DefaultDtObjectValidator<O extends DtObject> extends AbstractDtObjectValidator<O> {

	/** {@inheritDoc} */
	@Override
	protected void checkMonoFieldConstraints(final O dtObject, final DtField dtField, final DtObjectErrors dtObjectErrors) {
		final Object value = dtField.getDataAccessor().getValue(dtObject);
		//pas d'assertion notNull, car le champs n'est pas forcément obligatoire
		if (value == null && dtField.isRequired()) {
			dtObjectErrors.addError(dtField.getName(), MessageText.of("Le champ doit être renseigné"));
		} else {
			try {
				// Le typage est OK
				// Si non null, on vérifie la validité de la valeur par rapport au champ/domaine.
				dtField.getDomain().checkConstraints(value);
			} catch (final ConstraintException e) {
				// Erreur lors du check de la valeur,
				// la valeur est toutefois correctement typée.
				dtObjectErrors.addError(dtField.getName(), e.getMessageText());
			}
		}
	}
}
