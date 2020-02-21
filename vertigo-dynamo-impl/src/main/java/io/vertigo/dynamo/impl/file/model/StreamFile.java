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

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import io.vertigo.dynamo.file.model.InputStreamBuilder;

/**
 * VFile implementation from a provided InputStreamBuilder.
 * @author npiedeloup
 */
public final class StreamFile extends AbstractVFile {
	private static final long serialVersionUID = -4565434303879706815L;

	private final InputStreamBuilder inputStreamBuilder;

	/**
	 * Constructor.
	 * @param fileName File name
	 * @param mimeType Mime type
	 * @param lastModified Last modified date
	 * @param length file size
	 * @param inputStreamBuilder Data stream builder
	 */
	public StreamFile(final String fileName, final String mimeType, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		super(fileName, mimeType, lastModified, length);
		this.inputStreamBuilder = inputStreamBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public InputStream createInputStream() throws IOException {
		return inputStreamBuilder.createInputStream();
	}
}
