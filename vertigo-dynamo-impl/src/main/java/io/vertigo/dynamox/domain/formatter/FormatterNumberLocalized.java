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

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import io.vertigo.app.Home;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.MessageText;
import io.vertigo.lang.Assertion;

/**
 * Formatteur multi-lingue.
 * Les séparateurs décimaux et de milliers sont des listes de char.
 * Le premier est celui utilisé par défaut dans les valueToString.
 * La liste des char peut être le code d'une resource multi-lingue.
 *
 * Exemple de saisie des args :
 * #,###.00 |.,;|/u00A0
 * format de rendu|séparateur décimalpar défaut puis ceux acceptés| séparateurs de milliers
 *
 * #,###.00 |SEP_DECIMAUX|SEP_MILLIER  et dans resources_fr.properties :
 *
 *
 * @author npiedeloup
 */
public class FormatterNumberLocalized extends FormatterNumber {

	//Pour chaque locale on conserve les symboles utilisés
	private Map<Locale, DecimalFormatSymbols> decimalFormatSymbolsMap;

	private MessageText decimalSep;
	private MessageText groupSep;

	/**
	 * Constructeur.
	 */
	public FormatterNumberLocalized(final String args) {
		super("#");//fake format
		initLocalizedParameters(args);
	}

	private void initLocalizedParameters(final String args) {
		decimalFormatSymbolsMap = java.util.Collections.synchronizedMap(new HashMap<Locale, DecimalFormatSymbols>());
		if (args != null) {
			//-----
			final StringTokenizer st = new StringTokenizer(args, "|");

			//Affichage des nombre renseignées
			assertArgs(st.hasMoreTokens());
			final String decimalFormat = st.nextToken().trim();

			super.initParameters(decimalFormat);

			//séparateur de décimal
			if (st.hasMoreTokens()) {
				decimalSep = MessageText.of(st.nextToken().trim());//trim => l'espace ne peut être un séparateur de décimal
				Assertion.checkArgNotEmpty(decimalSep.getDisplay(), "Il faut au moins un séparateur de décimal");
			}

			//séparateur de millier
			if (st.hasMoreTokens()) {
				groupSep = MessageText.of(st.nextToken());//pas de trim car il est probable que l'espace soit utilisé
			}
		}
	}

	@Override
	protected DecimalFormatSymbols getDecimalFormatSymbols() {
		//Il n'y a pas besoin de synchroniser la méthode car la map l'est déjà.
		final Locale currentLocale = Home.getApp().getComponentSpace().resolve(LocaleManager.class).getCurrentLocale();
		DecimalFormatSymbols decimalFormatSymbols = decimalFormatSymbolsMap.get(currentLocale);
		if (decimalFormatSymbols == null) {
			// si Locale.FRANCE cela donne la virugle comme séparateur décimal
			// et l'espace insécable comme séparateur de milliers
			decimalFormatSymbols = new java.text.DecimalFormatSymbols(currentLocale);
			if (decimalSep != null) {
				final String decimalSepValue = decimalSep.getDisplay();
				decimalFormatSymbols.setDecimalSeparator(decimalSepValue.charAt(0));
			}
			if (groupSep != null) {
				final String groupSepValue = groupSep.getDisplay();
				if (groupSepValue.length() > 0) {//s'il n'y a pas de séparateur de group, on laisse celui par défaut.
					decimalFormatSymbols.setGroupingSeparator(groupSepValue.charAt(0));
				}
			}

			//vérification des conflits
			checkConflict(currentLocale, decimalFormatSymbols);

			decimalFormatSymbolsMap.put(currentLocale, decimalFormatSymbols);
		}
		return decimalFormatSymbols;
	}

	@Override
	protected String cleanStringNumber(final String sValue, final DecimalFormatSymbols decimalFormatSymbols) {
		final char[] decimalSepChar = obtainAcceptedDecimalSepChar(decimalFormatSymbols);
		final char[] groupSepChar = obtainAcceptedGroupSepChar(decimalFormatSymbols);

		/**
		 * On commence par controller le format saisie.
		 * On est assez strict.
		 * Au passage on note les séparateurs utilisés (on n'accepte pas de mélange)
		 */
		final char[] decimalAndGroupCharUsed = getAndCheckDecimalAndGroupChar(sValue, decimalSepChar, groupSepChar);
		final char decimalCharUsed = decimalAndGroupCharUsed[0];
		final char groupCharUsed = decimalAndGroupCharUsed[1];

		/**
		 * Puis on transforme la chaine pour revenir à l'ecriture la plus simple.
		 * Cela pour utiliser le Number.valueOf plutot que le parse de NumberFormat.
		 */
		return cleanStringNumber(sValue, decimalCharUsed, groupCharUsed);
	}

	private static void assertArgs(final boolean test) {
		Assertion.checkArgument(
				test,
				"Les arguments pour la construction de FormatterNumber sont invalides: format d'affichage{|séparateur de décimal}{|séparateur de millier}");
	}

	private void checkConflict(final Locale currentLocale, final DecimalFormatSymbols decimalFormatSymbols) {
		if (decimalSep != null) {
			for (final char decimalChar : decimalSep.getDisplay().toCharArray()) {
				Assertion.checkArgument(
						decimalChar != decimalFormatSymbols.getGroupingSeparator(),
						"A decimal separator ({0}) is in conflict with a grouping separator {1}", decimalChar, currentLocale.getDisplayName());
				if (groupSep != null) {
					final String groupSepValue = groupSep.getDisplay();
					Assertion.checkArgument(
							groupSepValue.indexOf(decimalChar) == -1,
							"A decimal separator ({0}) is in conflict with a grouping separator {1}", decimalChar, currentLocale.getDisplayName());
				}
			}
		}
	}

	private static char[] getAndCheckDecimalAndGroupChar(final String sValue, final char[] decimalSepChar, final char[] groupSepChar) {
		final char[] result = { decimalSepChar[0], groupSepChar[0] };

		int decimalIndex = sValue.length();
		for (final char myDecimalChar : decimalSepChar) {
			final int myDecimalIndex = sValue.indexOf(myDecimalChar);
			if (myDecimalIndex != -1) {
				result[0] = myDecimalChar;
				decimalIndex = myDecimalIndex;
				if (decimalIndex != sValue.lastIndexOf(myDecimalChar)) {
					throw new NumberFormatException("Plusieurs séparateur de décimal sont présent (" + myDecimalChar + ").");
				}
				break;
			}
		}

		boolean groupCharFound = false;
		for (final char myGroupChar : groupSepChar) {
			int groupIndex = sValue.indexOf(myGroupChar);

			while (groupIndex != -1) {
				if ((decimalIndex - groupIndex) % (3 + 1) != 0) {
					throw new NumberFormatException("Un séparateur de millier est mal placé (" + myGroupChar + ").");
				}
				result[1] = myGroupChar;
				groupCharFound = true;
				groupIndex = sValue.indexOf(myGroupChar, groupIndex + 1);
			}
			if (groupCharFound) {
				break;
			}
		}
		return result;
	}

	private char[] obtainAcceptedGroupSepChar(final DecimalFormatSymbols decimalFormatSymbols) {
		final char[] groupSepChar;
		if (groupSep == null) {
			groupSepChar = new char[1];
			groupSepChar[0] = decimalFormatSymbols.getGroupingSeparator();
		} else {
			groupSepChar = groupSep.getDisplay().toCharArray();
		}
		return groupSepChar;
	}

	private char[] obtainAcceptedDecimalSepChar(final DecimalFormatSymbols decimalFormatSymbols) {
		final char[] decimalSepChar;
		if (decimalSep == null) {
			decimalSepChar = new char[1];
			decimalSepChar[0] = decimalFormatSymbols.getDecimalSeparator();
		} else {
			decimalSepChar = decimalSep.getDisplay().toCharArray();
		}
		return decimalSepChar;
	}
}
