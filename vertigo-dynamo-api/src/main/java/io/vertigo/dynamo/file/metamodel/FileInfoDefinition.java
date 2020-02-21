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
package io.vertigo.dynamo.file.metamodel;

import io.vertigo.app.Home;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.lang.Assertion;

/**
 * Définition d'un FileInfo.
 *
 * La définition n'est pas serializable.
 * Elle doit être invariante (non mutable) dans le temps.
 * Par défaut elle est chargée au (re)démarrage du serveur.
 *
 * @author  npiedeloup, pchretien
 */
@DefinitionPrefix("Fi")
public final class FileInfoDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;
	/**
	 * StoreName des fichiers de ce type.
	 */
	private final String storeName;

	/**
	 * Constructor.
	 * @param name Nom de la définition
	 * @param storeName Nom du store de ces fichiers
	 */
	public FileInfoDefinition(final String name, final String storeName) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(storeName);
		//-----
		this.name = name;
		this.storeName = storeName;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Store d'accès aux FI.
	 */
	public String getStoreName() {
		return storeName;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	//=========================================================================
	//===========================STATIC========================================
	//=========================================================================
	/**
	 * @param fileInfoClass fileInfo Class
	 * @return FileInfoDefinition from class
	 */
	public static FileInfoDefinition findFileInfoDefinition(final Class<? extends FileInfo> fileInfoClass) {
		Assertion.checkNotNull(fileInfoClass);
		//-----
		final String name = DefinitionUtil.getPrefix(FileInfoDefinition.class) + fileInfoClass.getSimpleName();
		return Home.getApp().getDefinitionSpace().resolve(name, FileInfoDefinition.class);
	}
}
