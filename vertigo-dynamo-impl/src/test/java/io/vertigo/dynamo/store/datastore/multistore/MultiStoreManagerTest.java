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
package io.vertigo.dynamo.store.datastore.multistore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.filestore.fs.FsFullFileStorePlugin;
import io.vertigo.dynamo.store.StoreCacheDefinitionProvider;
import io.vertigo.dynamo.store.data.fileinfo.FileInfoFs;
import io.vertigo.dynamo.store.data.fileinfo.FileInfoTemp;
import io.vertigo.dynamo.store.datastore.AbstractStoreManagerTest;
import io.vertigo.dynamo.store.datastore.SqlUtil;
import io.vertigo.util.ListBuilder;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class MultiStoreManagerTest extends AbstractStoreManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withScript()
						.withMemoryCache()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.withC3p0(
								Param.of("name", "otherBase"),
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database2"))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withSqlStore()
						.withSqlStore(
								Param.of("dataSpace", "otherStore"),
								Param.of("connectionName", "otherBase"))
						.withDbFileStore(Param.of("storeDtName", "DtVxFileInfo"))
						.withFsFileStore(Param.of("name", "fsStore"),
								Param.of("path", "${java.io.tmpdir}/testFsVertigo/"),
								Param.of("storeDtName", "DtVxFileInfo"))
						.withFsFullFileStore(
								Param.of("name", "temp"),
								Param.of("path", "${java.io.tmpdir}/testVertigo/"),
								Param.of("purgeDelayMinutes", "0"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/store/data/executionOtherStore.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.store.data.DtDefinitions")
								.build())
						.addDefinitionProvider(StoreCacheDefinitionProvider.class)
						.build())
				.build();
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		initOtherStore();
	}

	protected void initOtherStore() {
		//A chaque test on recrée la table famille dans l'autre base
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateOtherStoreRequests(),
				"TkInitOther",
				Optional.of("otherStore"));
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();

		final Path tempFsDir = Paths.get(FileUtil.translatePath("${java.io.tmpdir}/testFsVertigo/"));
		removeAllPath(tempFsDir);
	}

	private void removeAllPath(final Path pathToBeDeleted) throws IOException {
		if (Files.exists(pathToBeDeleted)) {
			Files.walk(pathToBeDeleted)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
	}

	@Override
	protected List<String> getCreateMainStoreRequests() {
		//On retire famille du main store
		return new ListBuilder<String>()
				.addAll(getCreateCarRequests())
				.addAll(getCreateFileInfoRequests())
				.build();
	}

	private List<String> getCreateOtherStoreRequests() {
		final List<String> requests = getCreateFamilleRequests();
		return requests;
	}

	@Test
	public void testOtherStoreFile() throws Exception {
		final VFile vFile = TestUtil.createVFile(fileManager, "../data/lautreamont.txt", AbstractStoreManagerTest.class);
		//1.Création du fichier depuis un fichier texte du FS
		final FileInfo fileInfo = new FileInfoTemp(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = storeManager.getFileStore().create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)
			System.out.println("testOtherStoreFile " + createdFileInfo.getURI().toURN());

		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//3.relecture du fichier
			final FileInfo readFileInfo = storeManager.getFileStore().read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new ByteArrayOutputStream()) {
				try (final InputStream sourceIS = vFile.createInputStream()) {
					FileUtil.copy(sourceIS, sourceOS);
				}
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new ByteArrayOutputStream()) {
				try (final InputStream in = readFileInfo.getVFile().createInputStream()) {
					FileUtil.copy(in, readOS);
				}
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read);
			Assertions.assertTrue(read.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"));

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé
			//Assertions.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}

	@Test
	public void testCreateTempFile() throws Exception {
		doCreateFile(this::createTempFileInfo);
	}

	@Test
	public void testUpdateTempFile() throws Exception {
		doUpdateFile(this::createTempFileInfo);
	}

	@Test
	public void testDeleteTempFile() throws Exception {
		doDeleteFile(this::createTempFileInfo);
	}

	@Test
	public void testCreateFsFile() throws Exception {
		doCreateFile(this::createFsFileInfo);
	}

	@Test
	public void testUpdateFsFile() throws Exception {
		doUpdateFile(this::createFsFileInfo);
	}

	@Test
	public void testDeleteFsFile() throws Exception {
		doDeleteFile(this::createFsFileInfo);
	}

	protected FileInfo createTempFileInfo(final VFile vFile) {
		return new FileInfoTemp(vFile);
	}

	protected FileInfo createFsFileInfo(final VFile vFile) {
		return new FileInfoFs(vFile);
	}

	@Inject
	private List<FileStorePlugin> fileStorePlugins;

	@Test
	public void testRemovedOldFileDaemon() throws Exception {
		final Path tempDir = Paths.get(FileUtil.translatePath("${java.io.tmpdir}/testVertigo/"));
		final long firstCount = fileCount(tempDir);

		Thread.sleep(1000); //wait remove dir

		final VFile vFile = TestUtil.createVFile(fileManager, "../data/lautreamont.txt", AbstractStoreManagerTest.class);
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//1.Création du fichier depuis un fichier texte du FS
			final FileInfo fileInfo = new FileInfoTemp(vFile);
			//2. Sauvegarde en Temp
			final FileInfo createdFileInfo = storeManager.getFileStore().create(fileInfo);
			System.out.println(createdFileInfo.getURI().toURN());
			transaction.commit(); //can't read file if not commited (TODO ?)
		}

		final long beforeCount = fileCount(tempDir);

		Assertions.assertEquals(firstCount + 2, beforeCount, "Created file wasn't store in temp dir");

		Thread.sleep(1000); //wait remove dir

		fileStorePlugins.stream()
				.filter((plugin) -> plugin instanceof FsFullFileStorePlugin)
				.findFirst().ifPresent(
						(plugin) -> ((FsFullFileStorePlugin) plugin).deleteOldFiles());

		Thread.sleep(1000); //wait remove dir

		final long afterCount = fileCount(tempDir);

		Assertions.assertEquals(0, afterCount, "Clean of temp dir wasn't complete");

	}

	public long fileCount(final Path dir) throws IOException {
		try (Stream<Path> subFiles = Files.walk(dir)) {
			return subFiles.filter(p -> !Files.isDirectory(p))
					.count();
		}
	}

}
