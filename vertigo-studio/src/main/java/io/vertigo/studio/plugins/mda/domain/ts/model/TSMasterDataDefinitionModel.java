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
package io.vertigo.studio.plugins.mda.domain.ts.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.masterdata.MasterDataValue;

/**
 * Model TS des materdata.
 *
 * @author npiedeloup
 */
public final class TSMasterDataDefinitionModel {
	private final TSDtDefinitionModel tsDtDefinitionModel;
	private final List<TSMasterDataValueModel> tsMasterDataValueModels;

	public TSMasterDataDefinitionModel(final DtDefinition dtDefinition, final Map<String, MasterDataValue> masterDataValuesByDtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		tsDtDefinitionModel = new TSDtDefinitionModel(dtDefinition);
		tsMasterDataValueModels = masterDataValuesByDtDefinition
				.entrySet()
				.stream()
				.map(entry -> new TSMasterDataValueModel(dtDefinition, entry.getValue()))
				.collect(Collectors.toList());
	}

	public TSDtDefinitionModel getDefinition() {
		return tsDtDefinitionModel;
	}

	public String getIdFieldName() {
		return tsDtDefinitionModel.getDtDefinition().getIdField().get().getName();
	}

	public List<TSMasterDataValueModel> getValues() {
		return tsMasterDataValueModels;
	}

}
