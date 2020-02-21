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
package io.vertigo.dynamo.plugins.environment.loaders.xml;

import io.vertigo.lang.Assertion;

/**
 * XMLUtil for loaders.
 * @author npiedeloup
 */
final class XmlUtil {

	private XmlUtil() {
		//private
	}

	/**
	 * Conversion français vers Java avec remplacement d'accents : Xxx éèà zzz -> XxxEeaZzz.
	 * @param str la chaine de caratères sur laquelle s'appliquent les transformation
	 * @return Renvoie le résultat
	 */
	static String french2Java(final String str) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "La chaine à modifier ne doit pas être vide.");
		//-----
		final StringBuilder suffix = new StringBuilder();
		char c;
		boolean upperNextChar = true;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			//On considère blanc, et ' comme des séparateurs de mots.
			if (c == ' ' || c == '\'') {
				upperNextChar = true;
			} else {
				c = stripAccents(c);
				if (Character.isLetterOrDigit(c)) {
					suffix.append(upperNextChar ? Character.toUpperCase(c) : c);
				}
				upperNextChar = false;
			}
		}
		return suffix.toString();
	}

	/**
	 * Remplacement de caractères accentués par leurs équivalents non accentués
	 * (par ex: accents dans rôles)
	 * @param c caractère accentué à traiter
	 * @return caractère traité (sans accent)
	 */
	private static char stripAccents(final char c) {
		switch (c) {
			case '\u00e0':
			case '\u00e2':
			case '\u00e4':
				return 'a';
			case '\u00e7':
				return 'c';
			case '\u00e8':
			case '\u00e9':
			case '\u00ea':
			case '\u00eb':
				return 'e';
			case '\u00ee':
			case '\u00ef':
				return 'i';
			case '\u00f4':
			case '\u00f6':
				return 'o';
			case '\u00f9':
			case '\u00fb':
			case '\u00fc':
				return 'u';
			default:
				return c;
		}
	}
}
