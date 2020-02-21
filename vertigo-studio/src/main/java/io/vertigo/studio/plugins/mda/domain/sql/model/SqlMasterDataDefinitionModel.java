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
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.masterdata.MasterDataValue;

/**
 * Model Sql des materdata.
 *
 * @author mlaroche
 */
public final class SqlMasterDataDefinitionModel {
	private final SqlDtDefinitionModel sqlDtDefinitionModel;
	private final List<SqlMasterDataValueModel> sqlMasterDataValueModels;

	public SqlMasterDataDefinitionModel(final DtDefinition dtDefinition, final Map<String, MasterDataValue> masterDataValuesByDtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		sqlDtDefinitionModel = new SqlDtDefinitionModel(dtDefinition);
		sqlMasterDataValueModels = masterDataValuesByDtDefinition
				.entrySet()
				.stream()
				.map(entry -> new SqlMasterDataValueModel(dtDefinition, entry.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * @return Nom de la table
	 */
	public String getTableName() {
		return sqlDtDefinitionModel.getLocalName();
	}

	public SqlDtDefinitionModel getDefinition() {
		return sqlDtDefinitionModel;
	}

	public List<SqlMasterDataValueModel> getValues() {
		return sqlMasterDataValueModels;
	}

}
