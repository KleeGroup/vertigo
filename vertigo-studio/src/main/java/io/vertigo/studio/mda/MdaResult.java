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
package io.vertigo.studio.mda;

import java.io.PrintStream;

import io.vertigo.util.StringUtil;

/**
 * Résultat de la génération.
 *
 * @author pchretien
 */
public final class MdaResult {
	/** Nombre de fichiers écrits . */
	private final int createdFiles;
	private final int updatedFiles;
	/** Nombre de fichiers en erreurs. */
	private final int errorFiles;
	/** Nombre de fichiers détruits. */
	private final int deletedFiles;
	/** Nombre de fichiers identiques. */
	private final int identicalFiles;

	private final long durationMillis;

	MdaResult(final int createdFiles, final int updatedFiles, final int errorFiles, final int identicalFiles, final int deletedFiles, final long durationMillis) {
		this.createdFiles = createdFiles;
		this.updatedFiles = updatedFiles;
		this.errorFiles = errorFiles;
		this.deletedFiles = deletedFiles;
		this.identicalFiles = identicalFiles;
		this.durationMillis = durationMillis;
	}

	/**
	 * Static method factory for MdaResultBuilder
	 * @return MdaResultBuilder
	 */
	public static MdaResultBuilder builder() {
		return new MdaResultBuilder();
	}

	/**
	 * Affichage du résultat de la génération dans la console.
	 */
	public void displayResultMessage(final PrintStream out) {
		out.append(StringUtil.format("\nCréation de {0} fichiers, Mise à jour de {1} fichiers, {2} fichiers identiques et {3} problemes en {4} ms",
				createdFiles, updatedFiles, identicalFiles, errorFiles, durationMillis));
	}

	/** Nombre de fichiers écrits . */
	public int getCreatedFiles() {
		return createdFiles;
	}

	public int getUpdatedFiles() {
		return updatedFiles;
	}

	/** Nombre de fichiers en erreurs. */
	public int getErrorFiles() {
		return errorFiles;
	}

	/** Nombre de fichiers identiques. */
	public int getIdenticalFiles() {
		return identicalFiles;
	}

	public int getDeletedFiles() {
		return deletedFiles;
	}

	public long getDurationMillis() {
		return durationMillis;
	}

}
