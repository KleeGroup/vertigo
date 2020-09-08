/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test de l'utilitaitre de TempFile.
 * @author pchretien
 */
public final class TempFileUtilTest {
	private static final String FILE_PREFIX = "tempFileJunit";

	@Test
	public void testCreateTempFile() throws IOException {
		final File temp = new TempFile(FILE_PREFIX, ".tmp");
		Assertions.assertTrue(temp.exists(), "TempFile must exists");
	}

	@Test
	public void testCreateTempFileSubDir() throws IOException {
		final File temp = new TempFile(FILE_PREFIX, ".tmp", "tempDirJunit");
		Assertions.assertTrue(temp.exists(), "TempFile must exists");
	}

	@Test
	public void testRemoveTempFile() throws IOException {
		//we create only one file, for create dir
		final File temp = new TempFile(FILE_PREFIX, ".tmp");

		//we check, old files has been removed (no more than this 1 file)
		long count;
		try (Stream<Path> subFiles = Files.walk(temp.toPath().getParent())) {
			count = subFiles
					.filter(p -> !Files.isDirectory(p))
					.filter(p -> p.getFileName().toString().startsWith(FILE_PREFIX))
					.count();
		}
		//au max le nombre de fichier de ce test
		Assertions.assertTrue(count <= 2, "Some temp files from old tests are found");

	}

}
