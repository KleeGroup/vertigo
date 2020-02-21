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

import java.util.StringTokenizer;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Gestion des formattages de booléens.
 *
 * Args contient deux arguments séparés par des points virgules ';'
 * Le premier argument est obligatoire il représente le format d'affichage de la valeur vraie.
 *
 * Exemple 1 d'arguments si les ressources ne sont pas toutes externalisées : "OUI ; NON"
 * Exemple 2 d'arguments si les ressources sont externalisées : "TXT_OUI ; TXT_NON"
 *
 * @author pchretien
 */
public final class FormatterBoolean implements Formatter {
	/**
	 * MessageText pour les boolean à true
	 */
	private final String truePattern;

	/**
	 * MessageText pour les boolean à false
	 */
	private final String falsePattern;

	/**
	 * Constructor.
	 */
	public FormatterBoolean(final String args) {
		// Les arguments ne doivent pas être vides.
		assertArgs(args != null, args);
		//-----
		final StringTokenizer st = new StringTokenizer(args, ";");

		//OUI
		assertArgs(st.hasMoreTokens(), args);
		truePattern = st.nextToken().trim();

		//NON
		assertArgs(st.hasMoreTokens(), args);
		falsePattern = st.nextToken().trim();

		//C'est fini plus de texte attendu
		assertArgs(!st.hasMoreTokens(), args);
	}

	private static void assertArgs(final boolean test, final String args) {
		Assertion.checkArgument(test, "Les arguments pour la construction de FormatterBoolean sont invalides: format oui; format non (ici:{0})", args);
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.Boolean, "Formatter ne s'applique qu'aux booléens");
		//-----
		return booleanToString((Boolean) objValue);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		Assertion.checkArgument(dataType == DataType.Boolean, "Formatter ne s'applique qu'aux booléens");
		//-----
		final String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		return stringToBoolean(sValue);
	}

	/**
	 * Convertit une valeur String en booléen.
	 * @param booleanString valeur booléenne sous forme de chaine
	 * @return valeur typée en Boolean.
	 * @throws FormatterException Erreur de parsing
	 */
	private Boolean stringToBoolean(final String booleanString) throws FormatterException {
		final Boolean result;
		if (null == booleanString) {
			result = null;
		} else if ("true".equals(booleanString) || "1".equals(booleanString) || truePattern.equals(booleanString)) {
			result = Boolean.TRUE;
		} else if ("false".equals(booleanString) || "0".equals(booleanString) || falsePattern.equals(booleanString)) {
			result = Boolean.FALSE;
		} else {
			throw new FormatterException(Resources.DYNAMOX_BOOLEAN_NOT_FORMATTED);
		}
		return result;
	}

	/**
	 * Convertit une valeur boolean en chaine.
	 * @param booleanValue Valeur booléenne
	 * @return Valeur formattée en String
	 */
	private String booleanToString(final Boolean booleanValue) {
		final String boolString;
		if (booleanValue == null) {
			boolString = null;
		} else {
			boolString = booleanValue ? truePattern : falsePattern;
		}
		return boolString;
	}
}
