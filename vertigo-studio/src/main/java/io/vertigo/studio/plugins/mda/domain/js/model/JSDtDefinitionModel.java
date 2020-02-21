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
package io.vertigo.studio.plugins.mda.domain.js.model;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.lang.Assertion;

/**
 * Model used by FreeMarker.
 *
 * @author pchretien
 */
public final class JSDtDefinitionModel {
	private final DtDefinition dtDefinition;
	private final List<JSDtFieldModel> dtFieldModels;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public JSDtDefinitionModel(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

		dtFieldModels = dtDefinition.getFields().stream()
				.filter(dtField -> FieldType.COMPUTED != dtField.getType())
				.map(JSDtFieldModel::new)
				.collect(Collectors.toList());
	}

	/**
	 * @return DT définition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * @return Liste de champs
	 */
	public List<JSDtFieldModel> getFields() {
		return dtFieldModels;
	}
}
