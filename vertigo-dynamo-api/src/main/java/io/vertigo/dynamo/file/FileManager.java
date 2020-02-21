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
package io.vertigo.dynamo.file;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;

/**
 * Gestionnaire de la notion de fichier.
 * @author npiedeloup
 */
public interface FileManager extends Manager {

	/**
	 * @param vFile VFile à lire
	 * @return Fichier physique readOnly (pour lecture d'un FileInfo)
	 * @Deprecated Use obtainReadOnlyFile with nio.Path api instead
	 */
	@Deprecated
	File obtainReadOnlyFile(final VFile vFile);

	/**
	 * @param vFile VFile à lire
	 * @return Fichier physique readOnly (pour lecture d'un FileInfo) (nio api)
	 */
	Path obtainReadOnlyPath(final VFile vFile);

	/**
	 * Crée un VFile à partir d'un fichier physique.
	 * Charge au developpeur d'assurer sa persistence si nécessaire
	 * @param fileName Nom du fichier
	 * @param typeMime Type mime
	 * @param file Fichier physique
	 * @return VFile crée
	 * @Deprecated Use createFile with nio.Path api instead
	 */
	@Deprecated
	VFile createFile(final String fileName, final String typeMime, final File file);

	/**
	 * Crée un VFile à partir d'un fichier physique.
	 * Charge au developpeur d'assurer sa persistence si nécessaire
	 * @param fileName Nom du fichier
	 * @param typeMime Type mime
	 * @param file Fichier physique (nio api)
	 * @return VFile crée
	 */
	VFile createFile(final String fileName, final String typeMime, final Path file);

	/**
	 * Crée un VFile à partir d'un fichier physique.
	 * Charge au developpeur d'assurer sa persistence si nécessaire
	 * @param file Fichier physique
	 * @return VFile crée
	 * @Deprecated Use createFile with nio.Path api instead
	 */
	@Deprecated
	VFile createFile(final File file);

	/**
	 * Crée un VFile à partir d'un fichier physique.
	 * Charge au developpeur d'assurer sa persistence si nécessaire
	 * @param file Fichier physique (nio api)
	 * @return VFile crée
	 * @throws IOException Erreur I/O
	 */
	VFile createFile(final Path file) throws IOException;

	/**
	 * Crée un VFile à partir d'une URL.
	 * @param fileName Nom du fichier
	 * @param typeMime Type mime
	 * @param ressourceUrl URL du fichier
	 * @return VFile crée
	 */
	VFile createFile(final String fileName, final String typeMime, final URL ressourceUrl);

	/**
	 * Crée un VFile temporaire à partir d'un Builder du flux des données.
	 * Le typeMime sera déterminé à partir du fileName.
	 *
	 * @param fileName Nom du fichier
	 * @param lastModified Date de dernière modification
	 * @param length Taille du fichier
	 * @param inputStreamBuilder Builder du flux des données
	 * @return VFile créé
	 */
	VFile createFile(final String fileName, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder);

	/**
	 * Crée un VFile temporaire à partir d'un Builder du flux des données.
	 * @param fileName Nom du fichier
	 * @param typeMime Type mime
	 * @param lastModified Date de dernière modification
	 * @param length Taille du fichier
	 * @param inputStreamBuilder Builder du flux des données
	 * @return VFile crée
	 */
	VFile createFile(final String fileName, final String typeMime, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder);

	/**
	 * Crée un VFile temporaire à partir d'un Builder du flux des données.
	 * @param fileName Nom du fichier
	 * @param typeMime Type mime
	 * @param lastModified Date de dernière modification
	 * @param length Taille du fichier
	 * @param inputStreamBuilder Builder du flux des données
	 * @return VFile crée
	 * @Deprecated Use createFile with Instant lastModified instead
	 */
	@Deprecated
	VFile createFile(final String fileName, final String typeMime, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder);

}
