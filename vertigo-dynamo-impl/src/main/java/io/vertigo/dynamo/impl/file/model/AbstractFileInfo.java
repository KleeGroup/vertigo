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
package io.vertigo.dynamo.impl.file.model;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * Class générique de définition d'un fichier.
 * @author npiedeloup
 */
public abstract class AbstractFileInfo implements FileInfo {
	private static final long serialVersionUID = 1L;
	private final VFile vFile;
	private final DefinitionReference<FileInfoDefinition> fileInfoDefinitionRef;
	private FileInfoURI uri;

	/**
	 * Constructor.
	 * Associe un fichier à des méta-données
	 * @param fileInfoDefinition Definition du FileInfo
	 * @param vFile Données du fichier
	*/
	protected AbstractFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
		Assertion.checkNotNull(fileInfoDefinition);
		Assertion.checkNotNull(vFile);
		//-----
		fileInfoDefinitionRef = new DefinitionReference<>(fileInfoDefinition);
		this.vFile = vFile;
	}

	/** {@inheritDoc} */
	@Override
	public final FileInfoURI getURI() {
		return uri;
	}

	/** {@inheritDoc} */
	@Override
	public final void setURIStored(final FileInfoURI storedUri) {
		Assertion.checkNotNull(storedUri);
		Assertion.checkState(uri == null, "Impossible de setter deux fois l'uri de stockage");
		Assertion.checkArgument(getDefinition().getName().equals(storedUri.<FileInfoDefinition> getDefinition().getName()),
				"L''URI ({0}) n''est pas compatible avec ce FileInfo ({1})", storedUri, fileInfoDefinitionRef);
		//-----
		uri = storedUri;
	}

	/** {@inheritDoc} */
	@Override
	public final FileInfoDefinition getDefinition() {
		return fileInfoDefinitionRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public final VFile getVFile() {
		return vFile;
	}
}
