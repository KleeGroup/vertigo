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
package io.vertigo.commons.impl.codec.csv;

import io.vertigo.commons.codec.Encoder;

/**
 * Codec des CSV.
 * Spécifications encodage CSV :
 * http://www.rfc-editor.org/rfc/rfc4180.txt
 *
 * Règle : If double-quotes are used to enclose fields, then a double-quote
 *	       appearing inside a field must be escaped by preceding it with
 *	       another double quote. For example:       "aaa","b""bb","ccc"
 *
 * @author pchretien
 */
public final class CsvEncoder implements Encoder<String, String> {
	// caractere 11 remplace le caractère 13 (retour chariot)
	private static final char CHAR_13 = (char) 13;
	private static final char CHAR_11 = (char) 11;

	/** {@inheritDoc} */
	@Override
	public String encode(final String toEncode) {
		if (toEncode == null || toEncode.length() == 0) { // perf
			return "";
		}
		//On double les double-quotes et le retour chariot 13 (sinon carré dans Excel)
		// ' '
		return toEncode.replace("\"", "\"\"").replace(CHAR_13, CHAR_11);

	}
}
