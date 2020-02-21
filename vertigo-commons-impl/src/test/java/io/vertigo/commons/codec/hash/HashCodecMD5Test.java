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
package io.vertigo.commons.codec.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

/**
 * Test l'encodeur de Hash.
 * @author pchretien
 */
public final class HashCodecMD5Test extends AbstractEncoderTest<Encoder<byte[], byte[]>, byte[], byte[]> {
	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getMD5Encoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() {
		/*
		* Test de création de l'empreinte MD5.
		* On vérifie que null ne respecte pas le contrat.
		*/
		Assertions.assertThrows(NullPointerException.class, () -> codec.encode(null));
	}

	/**
	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Caractères simples sans encodage
		final Encoder<byte[], String> hexEncoder = getCodecManager().getHexEncoder();
		final byte[] encoded = codec.encode(TEXT.getBytes());
		assertEquals(128 / 8, codec.encode(TEXT.getBytes()).length);
		assertEquals("8e51e6dffd416849980ec365bee3f8bd", hexEncoder.encode(encoded));
	}
}
