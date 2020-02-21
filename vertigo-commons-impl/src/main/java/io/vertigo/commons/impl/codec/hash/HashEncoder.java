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
package io.vertigo.commons.impl.codec.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation des hachages.
 * L'encodage n'autorise pas les données null.
 * La fonction de décodage n'existe pas.
 *
 * @author  pchretien
 */
public final class HashEncoder implements Encoder<byte[], byte[]> {
	/**
	 * Méthode de hashage autorisées.
	 *
	 */
	public enum Hash {
		/**
		 * MD5. Unsure already cracked.
		 */
		MD5("MD5"),
		/**
		 * SHA1. Unsure attack in progress.
		 */
		SHA1("SHA-1"),
		/**
		 * SHA256.
		 */
		SHA256("SHA-256");
		//-----
		//the name of the algo
		private final String algoName;

		Hash(final String algoName) {
			this.algoName = algoName;
		}

		MessageDigest getMessageDigest() {
			try {
				return MessageDigest.getInstance(algoName);
			} catch (final NoSuchAlgorithmException e) {
				throw WrappedException.wrap(e, algoName);
			}
		}
	}

	private final Hash hash;

	/**
	 * Constructor.
	 * @param hash méthode de hashage
	 */
	public HashEncoder(final Hash hash) {
		Assertion.checkNotNull(hash);
		//-----
		this.hash = hash;
	}

	/** {@inheritDoc} */
	@Override
	public byte[] encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//-----
		final MessageDigest messageDigest = hash.getMessageDigest();
		messageDigest.update(data);
		return messageDigest.digest();
	}
}
