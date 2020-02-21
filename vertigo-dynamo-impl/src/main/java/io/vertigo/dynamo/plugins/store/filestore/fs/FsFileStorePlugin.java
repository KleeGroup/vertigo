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
package io.vertigo.dynamo.plugins.store.filestore.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsFileStorePlugin implements FileStorePlugin, Activeable {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 *
	 * @author npiedeloup
	 */
	private enum DtoFields {
		/** Champ FileName */
		fileName,
		/** Champ MimeType */
		mimeType,
		/** Champ LastModified */
		lastModified,
		/** Champ Length */
		length,
		/** Champ FilePath */
		filePath
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;
	private final String name;
	private final String documentRoot;
	private DtField storeIdField;
	private DtDefinition storeDtDefinition;
	private final String storeDtDefinitionName;
	private final VTransactionManager transactionManager;

	/**
	 * Constructor.
	 * @param name Store name
	 * @param storeDtDefinitionName Nom du dt de stockage
	 * @param fileManager Manager de gestion des fichiers
	 * @param path le chemin jndi pour récupérer le paramètre path dans le context
	 * @param transactionManager Manager des transactions
	 */
	@Inject
	public FsFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			@ParamValue("storeDtName") final String storeDtDefinitionName,
			@ParamValue("path") final String path,
			final VTransactionManager transactionManager,
			final FileManager fileManager) {
		Assertion.checkNotNull(name);
		Assertion.checkArgNotEmpty(storeDtDefinitionName);
		Assertion.checkArgNotEmpty(path);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgument(path.endsWith("/"), "store path must ends with / ({0})", path);
		//-----
		this.name = name.orElse(StoreManager.MAIN_DATA_SPACE_NAME);
		readOnly = false;
		this.transactionManager = transactionManager;
		this.fileManager = fileManager;
		documentRoot = FileUtil.translatePath(path);
		this.storeDtDefinitionName = storeDtDefinitionName;
	}

	@Override
	public void start() {
		storeDtDefinition = Home.getApp().getDefinitionSpace().resolve(storeDtDefinitionName, DtDefinition.class);
		storeIdField = storeDtDefinition.getIdField().get();
	}

	/* (non-Javadoc)
	 * @see io.vertigo.core.component.Activeable#stop()
	 */
	@Override
	public void stop() {
		//NOP
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		// récupération de l'objet en base
		final UID<Entity> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getStoreManager().getDataStore().readOne(dtoUri);

		// récupération du fichier
		final String fileName = getValue(fileInfoDto, DtoFields.fileName, String.class);
		final String mimeType = getValue(fileInfoDto, DtoFields.mimeType, String.class);
		final Instant lastModified = getValue(fileInfoDto, DtoFields.lastModified, Instant.class);
		final Long length = getValue(fileInfoDto, DtoFields.length, Long.class);
		final String filePath = getValue(fileInfoDto, DtoFields.filePath, String.class);

		final InputStreamBuilder inputStreamBuilder = new FileInputStreamBuilder(new File(documentRoot + filePath));
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		// retourne le fileinfo avec le fichier et son URI
		final FsFileInfo fsFileInfo = new FsFileInfo(uri.getDefinition(), vFile);
		fsFileInfo.setURIStored(uri);
		return fsFileInfo;
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	private Entity createFileInfoEntity(final FileInfo fileInfo) {
		final Entity fileInfoDto = createFileInfoEntity(fileInfo.getDefinition());
		//-----
		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.fileName, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.mimeType, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.lastModified, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.length, vFile.getLength());
		if (fileInfo.getURI() == null) {
			// cas de la création, on ajoute en base un chemin fictif (colonne not null)
			setValue(fileInfoDto, DtoFields.filePath, "/dev/null");
		} else {
			// cas de l'update
			setIdValue(fileInfoDto, fileInfo.getURI());

			// récupération de l'objet en base pour récupérer le path du fichier et ne pas modifier la base
			final UID<Entity> dtoUri = createDtObjectURI(fileInfo.getURI());
			final DtObject fileInfoDtoBase = getStoreManager().getDataStore().readOne(dtoUri);
			final String pathToSave = getValue(fileInfoDtoBase, DtoFields.filePath, String.class);
			setValue(fileInfoDto, DtoFields.filePath, pathToSave);
		}
		return fileInfoDto;
	}

	private void saveFile(final FileInfo fileInfo, final String pathToSave) {
		try (InputStream inputStream = fileInfo.getVFile().createInputStream()) {
			getCurrentTransaction().addAfterCompletion(new FileActionSave(inputStream, documentRoot + pathToSave));
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read uploaded file.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		//-----
		final Entity fileInfoDto = createFileInfoEntity(fileInfo);
		//-----
		getStoreManager().getDataStore().create(fileInfoDto);

		// cas de la création
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.checkNotNull(fileInfoDtoId, "File's id must be set.");
		final FileInfoURI uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);

		// on met a jour la base
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd/", Locale.FRANCE);
		final String pathToSave = format.format(LocalDate.now()) + fileInfoDtoId;
		setValue(fileInfoDto, DtoFields.filePath, pathToSave);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);
		//-----
		saveFile(fileInfo, pathToSave);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		//-----
		final Entity fileInfoDto = createFileInfoEntity(fileInfo);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);

		final String pathToSave = getValue(fileInfoDto, DtoFields.filePath, String.class);
		//-----
		saveFile(fileInfo, pathToSave);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);

		final UID<Entity> dtoUri = createDtObjectURI(uri);
		//-----suppression du fichier
		final DtObject fileInfoDto = getStoreManager().getDataStore().readOne(dtoUri);
		final String path = getValue(fileInfoDto, DtoFields.filePath, String.class);
		getCurrentTransaction().addAfterCompletion(new FileActionDelete(documentRoot + path));
		//-----suppression en base
		getStoreManager().getDataStore().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 *
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private UID<Entity> createDtObjectURI(final FileInfoURI uri) {
		Assertion.checkNotNull(uri, "file uri must be provided.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return UID.of(storeDtDefinition, uri.getKeyAs(storeIdField.getDomain().getDataType()));
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 *
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private Entity createFileInfoEntity(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition must be provided.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return DtObjectUtil.createEntity(storeDtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static <V> V getValue(final DtObject dto, final DtoFields field, final Class<V> valueClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return valueClass.cast(dtField.getDataAccessor().getValue(dto));
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static void setIdValue(final DtObject dto, final FileInfoURI uri) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getIdField().get();
		dtField.getDataAccessor().setValue(dto, uri.getKeyAs(dtField.getDomain().getDataType()));
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {

		private final File file;

		FileInputStreamBuilder(final File file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return Files.newInputStream(file.toPath());
		}
	}

	private static StoreManager getStoreManager() {
		return Home.getApp().getComponentSpace().resolve(StoreManager.class);
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	/**
	 * récupère la valeur de documentRoot.
	 *
	 * @return valeur de documentRoot
	 */
	public String getDocumentRoot() {
		return documentRoot;
	}

}
