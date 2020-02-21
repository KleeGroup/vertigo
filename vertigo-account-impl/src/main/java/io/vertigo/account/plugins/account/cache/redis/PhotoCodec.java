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
package io.vertigo.account.plugins.account.cache.redis;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.MapBuilder;

final class PhotoCodec {
	private static final int CODEC_BUFFER_SIZE = 3 * 1024;
	private final CodecManager codecManager;

	/**
	 * Constructor
	 * @param codecManager the codecManager
	 */
	PhotoCodec(final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
	}

	/**
	 * Encodes a photo to a map of metadata.
	 * The photo is encoded in base64.
	 * @param vFile the photo
	 * @return the photo as a map of metadata
	 */
	Map<String, String> vFile2Map(final VFile vFile) {
		Assertion.checkNotNull(vFile);
		//-----
		final String lastModified = vFile.getLastModified().toString();
		final String base64Content = encode2Base64(vFile);
		return new MapBuilder<String, String>()
				.put("fileName", vFile.getFileName())
				.put("mimeType", vFile.getMimeType())
				.put("length", String.valueOf(vFile.getLength()))
				.put("lastModified", lastModified)
				.put("base64Content", base64Content)
				.build();
	}

	/**
	 * Decodes a vFile from a map of metadata
	 * @param vFileMap the metadata as a map of String
	 * @return the photo
	 */
	VFile map2vFile(final Map<String, String> vFileMap) {
		Assertion.checkNotNull(vFileMap);
		//-----
		try {
			final String fileName = vFileMap.get("fileName");
			final String mimeType = vFileMap.get("mimeType");
			final Long length = Long.valueOf(vFileMap.get("length"));
			final Instant lastModified = Instant.parse(vFileMap.get("lastModified"));
			final String base64Content = vFileMap.get("base64Content");
			return new Base64File(codecManager, fileName, mimeType, length, lastModified, base64Content);
		} catch (final DateTimeParseException e) {
			throw WrappedException.wrap(e, "A problem occured when decoding a file from base64");
		}
	}

	private String encode2Base64(final VFile vFile) {
		final StringBuilder sb = new StringBuilder();
		final Codec<byte[], String> base64Codec = codecManager.getBase64Codec();
		try (final InputStream in = vFile.createInputStream()) {
			final byte[] buf = new byte[CODEC_BUFFER_SIZE];
			while (true) {
				final int rc = in.read(buf);
				if (rc <= 0) {
					break;
				}
				if (rc == CODEC_BUFFER_SIZE) {
					sb.append(base64Codec.encode(buf));
				} else {
					// il faut mettre uniquement la taille pertinente
					final byte[] buf2 = new byte[rc];
					System.arraycopy(buf, 0, buf2, 0, rc);
					sb.append(base64Codec.encode(buf2));
				}
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "A problem occured when encoding a file to base64");
		}
		return sb.toString();
	}
}
