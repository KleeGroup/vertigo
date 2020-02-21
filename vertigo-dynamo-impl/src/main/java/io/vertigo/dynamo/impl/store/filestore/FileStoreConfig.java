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
package io.vertigo.dynamo.impl.store.filestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.lang.Assertion;

/**
 * Implémentation Standard du StoreProvider.
 *
 * @author pchretien
 */
public final class FileStoreConfig {

	/** Map des stores utilisés spécifiquement */
	private final Map<String, FileStorePlugin> fileStoresMap = new HashMap<>();

	/**
	 * @param fileStorePlugins FileStore plugins
	 */
	public FileStoreConfig(final List<FileStorePlugin> fileStorePlugins) {
		Assertion.checkNotNull(fileStorePlugins);
		//-----
		for (final FileStorePlugin fileStorePlugin : fileStorePlugins) {
			final String name = fileStorePlugin.getName();
			final FileStorePlugin previous = fileStoresMap.put(name, fileStorePlugin);
			Assertion.checkState(previous == null, "FileStorePlugin {0}, was already registered", name);
		}
	}

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param definition Définition
	 * @return Store utilisé pour cette definition
	 */
	public FileStorePlugin getPhysicalFileStore(final FileInfoDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		final FileStorePlugin fileStore = fileStoresMap.get(definition.getStoreName());
		Assertion.checkNotNull(fileStore, "No FileStore found for this definition '{0}'", definition.getName());
		return fileStore;
	}
}
