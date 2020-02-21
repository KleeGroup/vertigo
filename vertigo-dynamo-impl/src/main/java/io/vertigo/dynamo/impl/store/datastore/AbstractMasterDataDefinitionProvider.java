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
package io.vertigo.dynamo.impl.store.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheData;
import io.vertigo.dynamo.store.datastore.MasterDataDefinition;

public abstract class AbstractMasterDataDefinitionProvider implements SimpleDefinitionProvider {

	private static final int CACHE_DURATION_LONG = 3600;
	private static final int CACHE_DURATION_SHORT = 600;

	private final List<Definition> tempList = new ArrayList<>();

	@Override
	public final List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		declareMasterDataLists();
		return tempList;
	}

	public abstract void declareMasterDataLists();

	protected <O extends DtObject> void registerDtMasterDatas(final Class<O> dtObjectClass) {
		registerDtMasterDatas(dtObjectClass, Collections.emptyMap(), true);

	}

	protected <O extends DtObject> void registerDtMasterDatas(final Class<O> dtObjectClass, final boolean isReloadedByList) {
		registerDtMasterDatas(dtObjectClass, Collections.emptyMap(), isReloadedByList);

	}

	protected <O extends DtObject> void registerDtMasterDatas(final Class<O> dtObjectClass, final Map<String, Predicate> namedLists, final boolean isReloadedByList) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectClass);
		// Si la durée dans le cache n'est pas précisé, on se base sur le type de la clé primaire pour déterminer la durée
		final int cacheDuration;
		if (dtDefinition.getStereotype() == DtStereotype.StaticMasterData) {
			cacheDuration = CACHE_DURATION_LONG;
		} else {
			cacheDuration = CACHE_DURATION_SHORT;
		}

		tempList.add(new CacheDefinition(CacheData.getContext(dtDefinition), true, 1000, cacheDuration, cacheDuration / 2, isReloadedByList));

		namedLists.entrySet()
				.forEach(entry -> tempList.add(new MasterDataDefinition("Md" + dtDefinition.getName() + entry.getKey(), new DtListURIForMasterData(dtDefinition, entry.getKey()), entry.getValue())));

		tempList.add(new MasterDataDefinition("Md" + dtDefinition.getName(), new DtListURIForMasterData(dtDefinition, null), o -> true));

	}

}
