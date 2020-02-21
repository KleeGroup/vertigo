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
package io.vertigo.studio.impl.masterdata;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.masterdata.MasterDataManager;
import io.vertigo.studio.masterdata.MasterDataValues;

/**
 * MasterDataManager for studio.
 *
 * @author mlaroche
 */
public final class MasterDataManagerImpl implements MasterDataManager {
	private final List<MasterDataValueProviderPlugin> masterDataValueProviderPlugins;

	@Inject
	public MasterDataManagerImpl(
			final List<MasterDataValueProviderPlugin> masterDataValueProviderPlugins) {
		//-----
		this.masterDataValueProviderPlugins = masterDataValueProviderPlugins;

	}

	@Override
	public MasterDataValues getValues() {
		final MasterDataValues result = new MasterDataValues();
		for (final MasterDataValueProviderPlugin masterDataValueProviderPlugin : masterDataValueProviderPlugins) {
			final MasterDataValues masterDataValues = masterDataValueProviderPlugin.getValues();

			// we aggregate the results of all files
			masterDataValues.entrySet()
					.stream()
					.forEach(entry -> {
						result.computeIfPresent(entry.getKey(), (key, value) -> {
							entry.getValue()
									.entrySet()
									.stream()
									// we check that a name is unique for an object type
									.peek(newEntry -> Assertion.checkState(!value.containsKey(newEntry.getKey()), "Value with name '{0}' for MasterData '{1}' is declared in two files", newEntry.getKey(), entry.getKey()))
									.forEach(newEntry -> value.put(newEntry.getKey(), newEntry.getValue()));
							return value;
						});
						result.computeIfAbsent(entry.getKey(), key -> entry.getValue());
					});

		}
		return result;
	}

}
