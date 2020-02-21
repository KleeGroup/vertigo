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
package io.vertigo.dynamo.store.data.domain.fileinfo;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public final class VxFileInfo implements Entity {
	private static final long serialVersionUID = 1L;

	private Long filId;
	private String fileName;
	private String mimeType;
	private Long length;
	private java.time.Instant lastModified;
	private String filePath;
	private io.vertigo.lang.DataStream fileData;

	/** {@inheritDoc} */
	@Override
	public UID<VxFileInfo> getUID() {
		return UID.of(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Identifiant'.
	 * @return Long filId <b>Obligatoire</b>
	 */
	@Field(domain = "DoId", type = "ID", required = true, label = "Identifiant")
	public Long getFilId() {
		return filId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Identifiant'.
	 * @param filId Long <b>Obligatoire</b>
	 */
	public void setFilId(final Long filId) {
		this.filId = filId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Nom'.
	 * @return String fileName <b>Obligatoire</b>
	 */
	@Field(domain = "DoString", required = true, label = "Nom")
	public String getFileName() {
		return fileName;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom'.
	 * @param fileName String <b>Obligatoire</b>
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Type mime'.
	 * @return String mimeType <b>Obligatoire</b>
	 */
	@Field(domain = "DoString", required = true, label = "Type mime")
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Type mime'.
	 * @param mimeType String <b>Obligatoire</b>
	 */
	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Taille'.
	 * @return Long length <b>Obligatoire</b>
	 */
	@Field(domain = "DoLong", required = true, label = "Taille")
	public Long getLength() {
		return length;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Taille'.
	 * @param length Long <b>Obligatoire</b>
	 */
	public void setLength(final Long length) {
		this.length = length;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date de derniÃ¨re modification'.
	 * @return java.time.Instant lastModified <b>Obligatoire</b>
	 */
	@Field(domain = "DoInstant", required = true, label = "Date de derniÃ¨re modification")
	public java.time.Instant getLastModified() {
		return lastModified;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de derniÃ¨re modification'.
	 * @param lastModified java.time.Instant <b>Obligatoire</b>
	 */
	public void setLastModified(final java.time.Instant lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'filePath'.
	 * @return String filePath
	 */
	@Field(domain = "DoString", required = false, label = "File Path")
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'filePath'.
	 * @param filePath String
	 */
	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'data'.
	 * @return io.vertigo.lang.DataStream fileData
	 */
	@Field(domain = "DoStream", label = "data")
	public io.vertigo.lang.DataStream getFileData() {
		return fileData;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'data'.
	 * @param fileData io.vertigo.lang.DataStream
	 */
	public void setFileData(final io.vertigo.lang.DataStream fileData) {
		this.fileData = fileData;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
