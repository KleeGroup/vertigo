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
package io.vertigo.commons.impl.codec.compressedserialization;

import java.io.Serializable;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;

/**
 * Implémentation standard ThreadSafe gérant les mécanismes permettant de
 * sérialiser de façon compressée un objet en format binaire (byte[]).
 *
 * @author pchretien
 */
public final class CompressedSerializationCodec implements Codec<Serializable, byte[]> {
	private final Codec<Serializable, byte[]> serializationCodec;
	private final Codec<byte[], byte[]> compressionCodec;

	/**
	 * Constructor.
	 * @param serializationCodec Codec
	 * @param compressionCodec Codec
	 */
	public CompressedSerializationCodec(final Codec<Serializable, byte[]> serializationCodec, final Codec<byte[], byte[]> compressionCodec) {
		Assertion.checkNotNull(serializationCodec);
		Assertion.checkNotNull(compressionCodec);
		//-----
		this.serializationCodec = serializationCodec;
		this.compressionCodec = compressionCodec;
	}

	/** {@inheritDoc} */
	@Override
	public byte[] encode(final Serializable data) {
		Assertion.checkNotNull(data);
		//-----
		return compressionCodec.encode(serializationCodec.encode(data));

	}

	/** {@inheritDoc} */
	@Override
	public Serializable decode(final byte[] data) {
		Assertion.checkNotNull(data);
		//-----
		return serializationCodec.decode(compressionCodec.decode(data));
	}
}
