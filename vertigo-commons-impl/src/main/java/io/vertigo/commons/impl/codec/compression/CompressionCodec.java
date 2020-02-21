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
package io.vertigo.commons.impl.codec.compression;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation standard ThreadSafe gérant les mécanismes permettant de compresser/décompresser un format binaire (byte[]) en un binaire.
 *
 * @author pchretien
 */
public final class CompressionCodec implements Codec<byte[], byte[]> {
	/**
	 * Seuil exprimé en octets en deça duquel on ne compresse pas les données.
	 */
	public static final int MIN_SIZE_FOR_COMPRESSION = 100;

	/**
	 * Seuil maximal autorisé pour la compression, ce seuil est exprimé en octets.
	 */
	private static final int MAX_SIZE_FOR_COMPRESSION = 20 * 1024 * 1024; //au dela de 20Mo, on ne compresse pas en mémoire : le codec est inadapté

	/**
	 * Niveau de compression de 0(pas de compression) à 9 (max compression). se
	 * référer au classes implémentant zip pour des précisions sur le niveau de
	 * compression.
	 */
	private static final int COMPRESSION_LEVEL = 1;

	private static final byte[] COMPRESS_KEY = { 'C', 'O', 'M', 'P' };

	private final Deflater deflater = new Deflater(COMPRESSION_LEVEL);

	private final Inflater inflater = new Inflater();

	/**
	 * Compression d'un objet.
	 * @param unCompressedObject Objet non compressé
	 * @return Objet Compressé
	 */
	@Override
	public byte[] encode(final byte[] unCompressedObject) {
		Assertion.checkNotNull(unCompressedObject);
		checkMaxSize(unCompressedObject.length);
		//-----
		if (unCompressedObject.length < MIN_SIZE_FOR_COMPRESSION) {
			return unCompressedObject;
		}

		final int nonCompressedLength = unCompressedObject.length;
		final byte[] compressedObject = new byte[nonCompressedLength + 8];
		final int compressedSize;
		synchronized (deflater) { //deflater n'est pas multi-thread
			deflater.reset();
			deflater.setInput(unCompressedObject);
			deflater.finish();
			deflater.deflate(compressedObject);
			compressedSize = deflater.getTotalOut();
		}
		final byte[] newCompressedObject = new byte[compressedSize + COMPRESS_KEY.length + 4];
		System.arraycopy(COMPRESS_KEY, 0, newCompressedObject, 0, COMPRESS_KEY.length);
		newCompressedObject[COMPRESS_KEY.length] = (byte) ((nonCompressedLength >>> 24) & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 1] = (byte) ((nonCompressedLength >>> 16) & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 2] = (byte) ((nonCompressedLength >>> 8) & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 3] = (byte) (nonCompressedLength & 0xFF);

		System.arraycopy(compressedObject, 0, newCompressedObject, COMPRESS_KEY.length + 4, compressedSize);
		return newCompressedObject;
	}

	private static void checkMaxSize(final int length) {
		if (length >= MAX_SIZE_FOR_COMPRESSION) {
			throw new IllegalArgumentException("L'objet est trop gros pour être compressé en mémoire (" + length / (1024 * 1024) + " Mo)");
		}
	}

	/**
	 * Décompression d'un objet.
	 * @param compressedObject Objet compressé
	 * @return Objet décompressé
	 */
	@Override
	public byte[] decode(final byte[] compressedObject) {
		Assertion.checkNotNull(compressedObject);
		//-----
		byte[] uncompressedObject = compressedObject;
		final byte[] compressHeader = new byte[COMPRESS_KEY.length];
		if (compressedObject.length > COMPRESS_KEY.length + 4) {
			System.arraycopy(compressedObject, 0, compressHeader, 0, COMPRESS_KEY.length);
			if (Arrays.equals(COMPRESS_KEY, compressHeader)) {
				final int ch1 = compressedObject[COMPRESS_KEY.length] & 0xff;
				// le & 0xff est necessaire pour avoir un int de 0 à 255, sinon
				// on a un int signé de -127 à 128

				final int ch2 = compressedObject[COMPRESS_KEY.length + 1] & 0xff;
				final int ch3 = compressedObject[COMPRESS_KEY.length + 2] & 0xff;
				final int ch4 = compressedObject[COMPRESS_KEY.length + 3] & 0xff;
				final int unCompressedLength = ch4 + (ch3 << 8) + (ch2 << 16) + (ch1 << 24);
				checkMaxSize(unCompressedLength);

				try {
					synchronized (inflater) { //inflater n'est pas multi-thread
						inflater.reset();
						inflater.setInput(compressedObject, COMPRESS_KEY.length + 4, compressedObject.length - (COMPRESS_KEY.length + 4));
						uncompressedObject = new byte[unCompressedLength];
						inflater.inflate(uncompressedObject);
					}
				} catch (final DataFormatException e) {
					throw WrappedException.wrap(e);
				}
			}
		}
		return uncompressedObject;
	}
}
