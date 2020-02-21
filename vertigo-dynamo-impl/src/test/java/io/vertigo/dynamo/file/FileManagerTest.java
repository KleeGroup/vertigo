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
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.time.Instant;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.lang.WrappedException;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class FileManagerTest extends AbstractTestCaseJU5 {

	@Inject
	private FileManager fileManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DynamoFeatures()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/file/data/execution.kpr")
								.build())
						.build())
				.build();
	}

	@Test
	public void testCreateTempFile() {
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = fileManager.createFile(file);
		checkVFile(vFile, "testFile.txt", null, "text/plain", 71092L);
	}

	@Test
	public void testObtainReadOnlyFile() {
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = fileManager.createFile(file);
		checVFile(fileManager.obtainReadOnlyFile(vFile), file);
	}

	@Test
	public void testCreateTempFileWithFixedNameAndMime() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = fileManager.createFile(fileName, typeMime, file);
		checkVFile(vFile, fileName, null, typeMime, 71092L);
	}

	@Test
	public void testCreateTempFileWithNoFileNoMime() {
		final String fileName = "monTestFile.txt";
		final Instant lastModified = Instant.now();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			@Override
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final VFile vFile = fileManager.createFile(fileName, lastModified, length, inputStreamBuilder);
		checkVFile(vFile, fileName, lastModified, "text/plain", length);
	}

	@Test
	public void testCreateTempFileWithNoFile() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final Instant lastModified = Instant.now();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			@Override
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final VFile vFile = fileManager.createFile(fileName, typeMime, lastModified, length, inputStreamBuilder);
		checkVFile(vFile, fileName, lastModified, typeMime, length);
	}

	private static void checkVFile(final VFile vFile, final String fileName, final Instant lastModified, final String mimeType, final Long length) {
		Assertions.assertEquals(fileName, vFile.getFileName());
		if (lastModified != null) { //le lastModified peut être inconnu du test
			Assertions.assertEquals(lastModified, vFile.getLastModified());
		}
		Assertions.assertEquals(mimeType, vFile.getMimeType());
		Assertions.assertEquals(length, vFile.getLength(), length * 0.1); //+ or - 10%

		try (InputStream in = vFile.createInputStream()) {
			nop(in);
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static void checVFile(final File outFile, final File inFile) {
		Assertions.assertEquals(inFile.getAbsolutePath(), outFile.getAbsolutePath());
	}
}
