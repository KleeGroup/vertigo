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

import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.model.FileInfo;

/**
 * File store plugin.
 *
 * @author  npiedeloup
 */
public interface FileStorePlugin extends Plugin {

	/**
	 * @return Store name
	 */
	String getName();

	/**
	 * Load a file by its URI.
	 *
	 * @param uri FileURI requested
	 * @return FileInfo for this uri (null if not found).
	 */
	FileInfo read(FileInfoURI uri);

	//==========================================================================
	//=============================== Write operations =========================
	//==========================================================================
	/**
	 * Save a file.
	 * Input FileInfo must have an empty URI : insert mode
	 *
	 * @param fileInfo File to save (creation)
	 * @return the created FileInfo
	 */
	FileInfo create(FileInfo fileInfo);

	/**
	 * Save a file.
	 * Input FileInfo must have an URI : update mode
	 *
	 * @param fileInfo File to save  (modification)
	 */
	void update(FileInfo fileInfo);

	/**
	 * Delete a file.
	 * @param uri File's URI to remove
	 */
	void delete(FileInfoURI uri);

}
