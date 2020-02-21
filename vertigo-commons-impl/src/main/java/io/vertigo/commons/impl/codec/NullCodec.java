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
package io.vertigo.commons.impl.codec;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;

/**
 *
 * @author pchretien
 */
final class NullCodec<S, T> implements Codec<S, T> {
	private final Codec<S, T> delegateCodec;

	NullCodec(final Codec<S, T> delegateCodec) {
		Assertion.checkNotNull(delegateCodec);
		//-----
		this.delegateCodec = delegateCodec;
	}

	/** {@inheritDoc} */
	@Override
	public S decode(final T encoded) {
		if (encoded == null) {
			return null;
		}
		return delegateCodec.decode(encoded);
	}

	/** {@inheritDoc} */
	@Override
	public T encode(final S toEncode) {
		if (toEncode == null) {
			return null;
		}
		return delegateCodec.encode(toEncode);
	}
}
