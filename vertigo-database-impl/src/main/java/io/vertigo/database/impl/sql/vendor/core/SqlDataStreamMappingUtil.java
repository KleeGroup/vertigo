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
package io.vertigo.database.impl.sql.vendor.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.DataStream;
import io.vertigo.util.TempFile;

/**
 * Gestion native des blobs.
 *
 * @author pchretien
 */
public final class SqlDataStreamMappingUtil {
	private static final int MEMORY_MAX_LENTH = 10 * 1024; //10Koi
	private static final int FILE_MAX_LENGTH = 100 * 1024 * 1024; //100Moi

	private SqlDataStreamMappingUtil() {
		//StateLess.
	}

	/**
	 * Récupération d'un flux à partir d'un Blob.
	 * Si le blob est inférieur à un seuil, il est conservé en mémoire.
	 * En revanche s'il excéde ce seuil, il est conservé sous la forme d'un fichier temporaire.
	 * @param rs ResultSet
	 * @param col index of blob column in resultSet
	 *
	 * @return Flux correspondant au blob.
	 * @throws SQLException Exception SQL.
	 */
	public static DataStream getDataStream(final ResultSet rs, final int col) throws SQLException {
		Assertion.checkNotNull(rs);
		Assertion.checkNotNull(col);
		//-----
		try (final InputStream in = rs.getBinaryStream(col)) {
			if (in != null) { //le flux est null, s'il n'a jamais été setté (si champs non persistant par exemple)
				return createDataStream(in);
			}
			return null;
		} catch (final IOException e) {
			throw new SQLException("reading error", e);
		}
	}

	private static DataStream createDataStream(final InputStream in) throws SQLException, IOException {
		//5 Koi
		try (final ByteArrayOutputStream memoryOut = new ByteArrayOutputStream(MEMORY_MAX_LENTH / 4)) {
			try {
				copy(in, memoryOut, MEMORY_MAX_LENTH);
				return new ByteArrayDataStream(memoryOut.toByteArray());
			} catch (final SqlOffLimitsException e) {
				//We don't rethrow this dynamo specific exception, we just change the process
				//Cas où le blob dépasse les limites imposées à la mémoire.
				return createDataStream(in, memoryOut.toByteArray());
			}
		}
	}

	private static DataStream createDataStream(final InputStream in, final byte[] bytes) throws IOException, SqlOffLimitsException {
		//On crée  un fichier temporaire.
		final File tmpFile = new TempFile("kdata", ".tmp");
		//-----
		//1ere étape : on recopie le contenu de la mémoire dans le fichier. (car on ne peut pas relire le Blob)
		try (final OutputStream fileOut = Files.newOutputStream(tmpFile.toPath()); final InputStream memoryIn = new ByteArrayInputStream(bytes)) {
			copy(memoryIn, fileOut, FILE_MAX_LENGTH);
			Assertion.checkState(tmpFile.length() <= MEMORY_MAX_LENTH, "Le fichier n'a pas repris le debut de l'export (RAM)");
			//2eme Etape : on copie la suite
			final long length = copy(in, fileOut, FILE_MAX_LENGTH);
			//La longueur totale du fichier est la somme.
			return new FileDataStream(tmpFile, length + bytes.length);
		}
	}

	/*
	 * Copie d'un flux sous la condition de ne pas dépasser le seuil précisé.
	 * En réalité le seuil peut être dépassé, s'il n'est pas un multiple de la taille du buffer (10*1024)
	 */
	private static long copy(final InputStream in, final OutputStream out, final int maxLength) throws IOException, SqlOffLimitsException {
		long length = 0;
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			length += read;
			if (length >= maxLength) {
				throw new SqlOffLimitsException(maxLength);
			}
			read = in.read(bytes);//on ne relis, que si la taille est inférieur (Tous ce qui est lu, doit être écrit sinon c'est perdu)
		}
		return length;
	}

	private static final class ByteArrayDataStream implements DataStream {
		private final byte[] bytes;

		ByteArrayDataStream(final byte[] bytes) {
			Assertion.checkNotNull(bytes);
			//-----
			this.bytes = bytes;
		}

		@Override
		public InputStream createInputStream() {
			return new ByteArrayInputStream(bytes);
		}

		@Override
		public long getLength() {
			return bytes.length;
		}
	}

	private static final class FileDataStream implements DataStream {
		private final File tmpFile;
		private final long length;

		FileDataStream(final File tmpFile, final long length) {
			this.tmpFile = tmpFile;
			this.length = length;
		}

		@Override
		public InputStream createInputStream() throws IOException {
			return Files.newInputStream(tmpFile.toPath());
		}

		@Override
		public long getLength() {
			return length;
		}
	}
}
