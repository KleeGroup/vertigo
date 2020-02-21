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
package io.vertigo.commons.codec.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

/**
 * Test du codec de validation pour un fichier CVS.
 * @author dchallas
 */
public final class CSVCodecTest extends AbstractEncoderTest<Encoder<String, String>, String, String> {
	/** {@inheritDoc} */
	@Override
	public Encoder<String, String> obtainCodec(final CodecManager codecManager) {
		return codecManager.getCsvEncoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() {
		assertEquals("", codec.encode(null));
		// assertNull(csvCodec.decode(null));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		checkEncode("", "");
		checkEncode("1", "1");
		checkEncode("2,2", "2,2");
		checkEncode("3,3,", "3,3,");
		checkEncode(",4,4 4,4 ", ",4,4 4,4 ");
		checkEncode("\"5,5", "\"\"5,5");
		checkEncode("abc:def\13", "abc:def" + (char) 11);
	}
}
