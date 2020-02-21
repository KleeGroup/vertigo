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
package io.vertigo.vega.webservice.data.domain;

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.webservice.validation.AbstractDtObjectValidator;
import io.vertigo.vega.webservice.validation.DtObjectErrors;

/**
 * Check that PK was NOT set in this object.
 * @author npiedeloup (4 nov. 2014 12:32:31)
 * @param <O> Object type
 */
public final class EmptyPkValidator<O extends DtObject> extends AbstractDtObjectValidator<O> {

	/** {@inheritDoc} */
	@Override
	protected void checkMonoFieldConstraints(final O dtObject, final DtField dtField, final DtObjectErrors dtObjectErrors) {
		final String camelCaseFieldName = dtField.getName();
		if (dtField.getType().isId() && !dtObjectErrors.hasError(camelCaseFieldName)) {
			dtObjectErrors.addError(camelCaseFieldName, MessageText.of("Id must not be set"));
		}
	}
}
