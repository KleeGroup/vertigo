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
package io.vertigo.studio.plugins.mda.domain.sql.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Model used by FreeMarker.
 *
 * @author pchretien
 */
public final class SqlDtDefinitionModel {
	private final DtDefinition dtDefinition;
	private final boolean hasSequence;
	private final List<SqlDtFieldModel> dtFieldModels;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public SqlDtDefinitionModel(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

		final Optional<DtField> pkField = dtDefinition.getIdField();
		if (pkField.isPresent()) {
			final DataType pkDataType = pkField.get().getDomain().getDataType();
			hasSequence = pkDataType.isNumber();
		} else {
			hasSequence = false;
		}

		dtFieldModels = dtDefinition.getFields().stream()
				.filter(dtField -> FieldType.COMPUTED != dtField.getType())
				.map(SqlDtFieldModel::new)
				.collect(Collectors.toList());
	}

	public String getLocalName() {
		return StringUtil.camelToConstCase(dtDefinition.getLocalName());
	}

	/**
	 * @return Si l'on doit générer une sequence
	 */
	public boolean hasSequence() {
		return hasSequence;
	}

	/**
	 * @return Liste de champs
	 */
	public List<SqlDtFieldModel> getFields() {
		return dtFieldModels;
	}

}
