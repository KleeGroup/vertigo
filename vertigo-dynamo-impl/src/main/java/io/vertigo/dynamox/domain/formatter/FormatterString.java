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
package io.vertigo.dynamox.domain.formatter;

import java.util.Locale;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Gestion des formattages de String.
 *
 * @author pchretien
 */
public final class FormatterString implements Formatter {

	private static final Locale TO_UPPER_CASE_LOCALE = Locale.FRANCE;

	/**
	 * Mode utilisé.
	 * Pour tous les mode un "trim" à droite et à gauche est effectué.
	 * Le trim à droite est obligatoire.
	 * Concernant le trim à gauche, il est possible de s'en passer
	 * il convient alors de créer un formatter ad hoc.
	 */
	public enum Mode {
		/**
		 * Aucun formattage.
		 */
		BASIC,
		/**
		 * Met en majuscules toutes les lettres.
		 */
		UPPER,
		/**
		 * Met en minuscules toutes les lettres.
		 */
		LOWER,
		/**
		 * Met en majuscules les premières lettres de chaque mot et en minuscules les suivantes
		 * Les séparateurs utilisés sont l'espace, "_" et "-.
		 */
		UPPER_FIRST
	}

	private final Mode mode;

	/**
	 * Constructor.
	 */
	public FormatterString(final String args) {
		//Si args non renseigné on prend le mode par défaut
		mode = args == null ? Mode.BASIC : Mode.valueOf(args);
	}

	/** {@inheritDoc} */
	@Override
	public String stringToValue(final String strValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.String, "Formatter ne s'applique qu'aux Strings");
		//-----
		return apply(strValue);
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.String, "Formatter ne s'applique qu'aux Strings");
		//-----
		final String result = apply((String) objValue);
		if (result == null) {
			return "";
		}
		return result;
	}

	private String apply(final String strValue) {
		final String result;
		final String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		if (sValue == null) {
			result = null;
		} else {
			switch (mode) {
				case BASIC:
					result = sValue;
					break;
				case UPPER:
					result = sValue.toUpperCase(TO_UPPER_CASE_LOCALE);
					break;
				case LOWER:
					result = sValue.toLowerCase(TO_UPPER_CASE_LOCALE);
					break;
				case UPPER_FIRST:
					result = firstLetterUpper(sValue);
					break;
				default:
					throw new IllegalAccessError("cas non implémenté");
			}
		}
		return result;
	}

	private static String firstLetterUpper(final String str) {
		final char[] letters = str.toCharArray();
		letters[0] = Character.toUpperCase(letters[0]);
		for (int i = 1; i < letters.length; i++) {
			if (letters[i - 1] == ' ' || letters[i - 1] == '-' || letters[i - 1] == '_') {
				letters[i] = Character.toUpperCase(letters[i]);
			} else {
				letters[i] = Character.toLowerCase(letters[i]);
			}
		}
		return new String(letters);
	}
}
