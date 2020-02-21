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
package io.vertigo.studio.plugins.mda.webservice;

/**
 * Classe utilitaire pour la gestion des chaines de caracteres.
 * @author rgrange
 *
 */
public final class JsFileNameUtil {

	private JsFileNameUtil() {
		//private
	}

	/**
	 * Converti le camel case en js-case.
	 * AaaaBbbbCcccc => aaa-bbb-ccc
	 * @param value Vaeur a parser.
	 * @return Valeur parsée.
	 */
	public static String convertCamelCaseToJsCase(final String value) {
		final StringBuilder parsedValue = new StringBuilder();
		for (final char character : value.toCharArray()) {
			if (Character.isUpperCase(character)) {
				if (parsedValue.length() > 0) {
					parsedValue.append('-');
				}
				parsedValue.append(Character.toLowerCase(character));
			} else {
				parsedValue.append(character);
			}
		}
		return parsedValue.toString();
	}

	/**
	 * Converti le js-case en camel case.
	 * aaa-bbb-ccc => aaaBbbCcc
	 * @param value Vaeur a parser.
	 * @return Valeur parsée.
	 */
	public static String convertJsCaseToCamelCase2(final String value) {
		final StringBuilder parsedValue = new StringBuilder();
		boolean isNextToUpper = false;
		for (final char character : value.toCharArray()) {
			if (character == '-') {
				isNextToUpper = true;
			} else {
				parsedValue.append(isNextToUpper ? Character.toUpperCase(character) : character);
				isNextToUpper = false;
			}
		}
		return parsedValue.toString();
	}
}
