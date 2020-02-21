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
package io.vertigo.commons.impl.codec.hex;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Hex transforme en chaine de caractères un tableau d'octets
 *
 * @author  pchretirn
 */
public final class HexEncoder implements Encoder<byte[], String> {
	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	/** {@inheritDoc} */
	@Override
	public String encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//-----
		final char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			final int v = data[i] & 0xFF;
			chars[i * 2] = HEX_ARRAY[v >>> 4];
			chars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(chars);
	}
}
