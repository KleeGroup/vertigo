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
package io.vertigo.dynamo.store;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.dynamo.store.filestore.FileStore;

/**
* Gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public interface StoreManager extends Manager {

	/** Main DataSpace's name. */
	String MAIN_DATA_SPACE_NAME = "main";

	/**
	 * @return FileStore
	 */
	FileStore getFileStore();

	/**
	 * @return DataStore
	 */
	DataStore getDataStore();

	/**
	 * @return Configuration du composant de persistance
	 */
	DataStoreConfig getDataStoreConfig();

	/**
	 * @return Configuration MDM
	 */
	MasterDataConfig getMasterDataConfig();
}
