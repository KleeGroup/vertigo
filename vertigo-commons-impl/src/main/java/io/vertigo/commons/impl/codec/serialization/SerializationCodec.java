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
package io.vertigo.commons.impl.codec.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation standard threadSafe des mécanismes permettant de sérialiser/ dé-sérialiser un objet.
 *
 * @author  mcrouzet, pchretien
 */
public final class SerializationCodec implements Codec<Serializable, byte[]> {

	/** {@inheritDoc} */
	@Override
	public byte[] encode(final Serializable object) {
		Assertion.checkNotNull(object);
		//-----
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(object);
			oos.flush();
			return baos.toByteArray();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Serialization : write stream exception for {0}", object.getClass().getName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Serializable decode(final byte[] serializedObject) {
		Assertion.checkNotNull(serializedObject);
		//-----
		try (final InputStream bais = new ByteArrayInputStream(serializedObject); final ObjectInputStream ois = new ObjectInputStream(bais)) {
			return (Serializable) ois.readObject();
		} catch (final IOException | ClassNotFoundException e) {
			throw WrappedException.wrap(e, "Deserialization : read stream exception");
		}
	}
}
