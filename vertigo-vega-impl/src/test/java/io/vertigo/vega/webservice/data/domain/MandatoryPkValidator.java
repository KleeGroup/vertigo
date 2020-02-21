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

import java.util.Set;

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.vega.webservice.validation.AbstractDtObjectValidator;
import io.vertigo.vega.webservice.validation.DtObjectErrors;

/**
 * Check that PK was set in this object.
 * @author npiedeloup (4 nov. 2014 12:32:31)
 * @param <E> the type of entity
 */
public final class MandatoryPkValidator<E extends Entity> extends AbstractDtObjectValidator<E> {

	/**
	 * NO checkMonoFieldConstraints.
	 * Can't check that PK was set in a checkMonoFieldConstraints.
	 * Because it was called for modified fields only, if PK is undefined it will not be checked.
	 */

	/** {@inheritDoc} */
	@Override
	protected void checkMultiFieldConstraints(final E entity, final Set<String> modifiedFieldNameSet, final DtObjectErrors dtObjectErrors) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final DtField idField = dtDefinition.getIdField().get();
		final String camelCaseFieldName = idField.getName();
		if (!dtObjectErrors.hasError(camelCaseFieldName)) {
			if (DtObjectUtil.getId(entity) == null) {
				dtObjectErrors.addError(camelCaseFieldName, MessageText.of("Id is mandatory"));
			}
		}
	}
}
