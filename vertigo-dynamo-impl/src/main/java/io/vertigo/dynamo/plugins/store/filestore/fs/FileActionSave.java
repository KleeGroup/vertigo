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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.transaction.VTransactionAfterCompletionFunction;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuple;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;

/**
 * Handling saving file to disk.
 *
 * @author skerdudou
 */
final class FileActionSave implements VTransactionAfterCompletionFunction {
	private static final String EXT_NEW = "toSave";
	private static final char EXT_SEPARATOR = '.';
	private static final Logger LOG = LogManager.getLogger(FileActionSave.class.getName());

	//ref the file before this save action
	private final List<Tuple<File, File>> txSaveFiles = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param inputStream File inputStream
	 * @param path Location of the file
	 */
	public FileActionSave(final InputStream inputStream, final String path) {
		add(inputStream, path);
	}

	/**
	 * Add same live span file to save.
	 *
	 * @param inputStream File inputStream
	 * @param path Location of the file
	 */
	public FileActionSave add(final InputStream inputStream, final String path) {
		Assertion.checkNotNull(inputStream);
		Assertion.checkNotNull(path);
		//-----
		final File txFinalFile = new File(path);
		final File txNewFile = new File(path + EXT_SEPARATOR + System.currentTimeMillis() + EXT_SEPARATOR + EXT_NEW);

		// Creation of the folder containing the file
		// the double check of exists() is for concurrency check, another process can have created the folder between our instructions
		if (!txNewFile.getParentFile().exists() && !txNewFile.getParentFile().mkdirs() && !txNewFile.getParentFile().exists()) {
			LOG.error("Can't create temp directories {}", txNewFile.getAbsolutePath());
			throw new VSystemException("Can't create temp directories");
		}

		// Creation of the temporary file inside the destination folder
		try {
			if (!txNewFile.createNewFile()) {
				LOG.error("Can't create temp file {}", txNewFile.getAbsolutePath());
				throw new VSystemException("Can't create temp file.");
			}
		} catch (final IOException e) {
			LOG.error("Can't save temp file {}", txNewFile.getAbsolutePath());
			throw WrappedException.wrap(e, "Can't save temp file.");
		}

		// Write data into the temp file. By doing this before the commit phase, we ensure we have enough space left.
		try {
			FileUtil.copy(inputStream, txNewFile);
		} catch (final IOException e) {
			LOG.error("Can't copy uploaded file to : {}", txNewFile.getAbsolutePath());
			throw WrappedException.wrap(e, "Can't save uploaded file.");
		}

		txSaveFiles.add(Tuple.of(txNewFile, txFinalFile));

		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void afterCompletion(final boolean txCommited) {
		if (txCommited) {
			doCommit();
		} else {
			doRollback();
		}
	}

	private void doCommit() {
		for (final Tuple<File, File> tuple : txSaveFiles) {
			final File txNewFile = tuple.getVal1();
			final File txFinalFile = tuple.getVal2();

			// Clean old file if exist
			if (txFinalFile.exists() && !txFinalFile.delete()) {
				LOG.fatal("Can't save file. Error replacing previous file ({}). A copy of the new file to save is kept into {}", txFinalFile.getAbsolutePath(), txNewFile.getAbsolutePath());
				throw new VSystemException("An error occured while saving the file.");
			}
			// we move the temp file to it's final destination
			if (!txNewFile.renameTo(txFinalFile)) {
				LOG.fatal("Can't save file. Error moving the file {} to it's final location {}", txNewFile.getAbsolutePath(), txFinalFile.getAbsolutePath());
				throw new VSystemException("An error occured while saving the file.");
			}
		}
	}

	private void doRollback() {
		// cleaning temp file on error
		for (final Tuple<File, File> tuple : txSaveFiles) {
			final File txNewFile = tuple.getVal1();
			if (txNewFile.exists() && !txNewFile.delete()) {
				LOG.error("Can't delete file {} on rollback", txNewFile.getAbsolutePath());
			}
		}

	}
}
