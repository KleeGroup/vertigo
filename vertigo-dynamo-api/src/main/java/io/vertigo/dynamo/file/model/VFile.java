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
package io.vertigo.dynamo.file.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;

/**
 * Représentation d'un Fichier logique.
 * Ce FileInfo fournit :
 * - le contenu du fichier
 * - son nom d'origine
 * - son type mime
 * - sa taille
 * - sa dernière date de modification
 *
 * Ce fichier peut être un fichier physique, un pointeur vers un fichier stocké en base ou sur internet peu importe.

 * @author npiedeloup
 */
public interface VFile extends Serializable {

	/**
	 * @return Nom d'origine du fichier
	 */
	String getFileName();

	/**
	 * @return Taille du fichier
	 */
	Long getLength();

	/**
	 * @return Date de modification du fichier en milli-secondes.
	 */
	Instant getLastModified();

	/**
	 * @return Type mime du fichier
	 */
	String getMimeType();

	/**
	 * Create a inputStream : It must be closed by caller !!
	 * @return Stream représentant le document physique.
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;
}
