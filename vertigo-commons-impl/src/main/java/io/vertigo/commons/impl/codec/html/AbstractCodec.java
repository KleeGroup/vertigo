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
package io.vertigo.commons.impl.codec.html;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Encodeur/décodeur.
 *  - Encode des char en String,
 *  - Décode les String en char,
 *  - Il existe un unique caractère d'échappement de type char,
 *  - Les caractères A-Z a-z 0-9 ne sont jamais encodés.
 *
 * Dans le cas du HTML le caractère d'échappement est &
 *
 * @author  pchretien, npiedeloup
 */
abstract class AbstractCodec implements Codec<String, String> {
	private final char startEscape;
	private final char endEscape;
	private final char[] toReplace;

	/**
	 * Liste  des chaines remplacantes triée dans le même ordre que to_replace .
	 * <br>Cette Liste est remplie dans les fils par la méthode getCharacters
	 */
	private final String[] replaceBy;
	private final Map<String, Character> replaceByMap;
	private int replaceByMaxSize;

	/**
	 * Constructor.
	 * Génération des tables de remplacement.
	 * Contrôle des chaines de remplacement
	 * Elles doivent commencer par LE caractère d'échappement et finir par le caractère de terminaison.
	 * @param startEscape Caractère d'échappement
	 * @param endEscape Caractère de terminaison
	 * @param characters table des caracteres HTML
	 */
	AbstractCodec(final char startEscape, final char endEscape, final String[] characters) {
		this.startEscape = startEscape;
		this.endEscape = endEscape;
		Arrays.sort(characters);
		toReplace = new char[characters.length];
		replaceBy = new String[characters.length];
		replaceByMap = new HashMap<>(characters.length);
		for (int i = 0; i < characters.length; i++) {
			// premier caractere dans la table des caractères a remplacer
			toReplace[i] = characters[i].charAt(0);
			// à partir du troisième caractere :
			// dans la table des codes remplacants
			replaceBy[i] = characters[i].substring(2);

			//Pour gérer le cas des encodages non bijectifs comme l'apostrophe word et windows
			//on donne priorité à la premiere définition pour le décodage.
			if (!replaceByMap.containsKey(replaceBy[i])) {
				replaceByMap.put(replaceBy[i], toReplace[i]);
				//Assertion.isNull(old, "Double insertion pour {0}", replaceBy[i])
				Assertion.checkArgument(replaceBy[i].charAt(0) == startEscape, "Les caractères encodés ({1}) doivent commencer par le caractère {0}", startEscape, replaceBy[i]);
				Assertion.checkArgument(replaceBy[i].charAt(replaceBy[i].length() - 1) == endEscape, "Les caractères encodés ({1}) doivent terminer par le caractère {0}", endEscape, replaceBy[i]);
				if (replaceBy[i].length() > replaceByMaxSize) {
					replaceByMaxSize = replaceBy[i].length();
				}
			}
		}
	}

	/**
	 * Ajoute une chaine a un StringBuilder après l'avoir encodée.
	 * Plus la chaine à encoder est longue,plus les gains de perfs sont sensibles.
	 * @param sb StringBuilder à appender.
	 * @param stringToEncode Chaine à encoder et à ajouter à <CODE>sb</CODE>
	 */
	private void encodeString(final StringBuilder sb, final String stringToEncode) {
		final int len = stringToEncode.length();
		sb.ensureCapacity(sb.length() + len + len / 4);
		//Réserve de la place dans le StringBuilder.
		//L'encodage allonge les données on réserver 25% en plus.

		int index;
		char c;
		//On prend les char les uns après les autres et on les traite en les encodant.
		for (int i = 0; i < len; i++) {
			c = stringToEncode.charAt(i);
			// Optimisation qui représente 90% des cas...
			if (StringUtil.isSimpleLetterOrDigit(c) || !shouldEncode(c, i, stringToEncode)) {
				//Les caractères A-Z a-z 0-9 ne sont jamais encodés
				sb.append(c);
			} else {
				// Recherche dans le tableau des caractères si il existe une chaine d'encodage.
				index = Arrays.binarySearch(toReplace, c);
				if (index >= 0) {
					// si trouve on trouve une chaine d'encodage alors on l'ajoute.
					sb.append(replaceBy[index]);
				} else {
					// sinon on ajoute le caractère sans le modifier.
					sb.append(c);
				}
			}
		}
	}

	/**
	 * Permet d'optimiser en déterminant si le caractère doit-être encodé
	 * @param c Caractère à encoder
	 * @param index Index dans la chaine à encoder
	 * @param stringToEncode Chaine à encoder
	 * @return Si ce caractère doit être encodé
	 */
	protected abstract boolean shouldEncode(final char c, final int index, final String stringToEncode);

	private void decodeString(final StringBuilder sb, final String s) {
		final int len = s.length();
		//Réserve de la place dans le StringBuilder.
		//Le décodage compresse les données on se contente donc de réserver
		//autant de place  que la longueur de la chaine à décoder.
		sb.ensureCapacity(sb.length() + len);

		for (int i = 0; i < len; i++) {
			final char c = s.charAt(i);
			// petite optimisation (qui represente 90% des cas...
			if (c != startEscape) {
				sb.append(c);
			} else {
				//On a trouvé le caractère d'échappement
				//On cherche le caractère de fin d'échappement
				final int j = indexOfEndEscape(i, s, len);
				//s'il on ne trouve pas la fin on passe, sinon on remplace
				if (j < replaceByMaxSize && i + j < len) {
					replaceCodecChar(sb, s, i, c, j);
					i += j;
				} else {
					sb.append(s.substring(i, i + j));
					i += j - 1;
				}
			}
		}
	}

	private void replaceCodecChar(final StringBuilder sb, final String s, final int i, final char c, final int j) {
		//La chaine encodée se trouve entre l'indice i et l'indice i+j
		//On l'extrait et on cherche le caractère qu'elle représente.
		final String stringToDecode = s.substring(i, i + j + 1);
		final Character decodedChar = replaceByMap.get(stringToDecode);
		if (decodedChar != null) {
			sb.append(decodedChar);//on a trouvé un caractère
		} else if (!shouldEncode(c, 0, stringToDecode)) {
			sb.append(stringToDecode);//si le caractère ne devait pas être encodé, on n'essai pas de le décoder.
		} else {
			throw new IllegalArgumentException("décodage non trouvé pour " + stringToDecode);
		}
	}

	private int indexOfEndEscape(final int i, final String s, final int len) {
		int j = 0;
		do {
			j++;
			//Tant que l'on n'a pas trouvé le caractère de fin
			//ou dépassé le nombre de caractères max d'encodage ou que l'on est arrivé à la fin.
		} while (j < replaceByMaxSize && i + j < len && s.charAt(i + j) != endEscape);
		return j;
	}

	/**
	 * Effectue l'encodage.
	 * @param s Chaine à encoder
	 * @return Chaine encodée
	 */
	final String doEncode(final String s) {
		Assertion.checkNotNull(s);
		//-----
		if (s.length() == 0) { // perf
			return "";
		}
		final StringBuilder sb = new StringBuilder(s.length() + s.length() / 4);
		encodeString(sb, s);
		return sb.toString();

	}

	/**
	 * Effectue le décodage.
	 * @param s Chaine à décoder
	 * @return Chaine décodée
	 */
	final String doDecode(final String s) {
		Assertion.checkNotNull(s);
		//-----
		if (s.length() == 0) { // perf
			return "";
		}
		final StringBuilder sb = new StringBuilder(s.length() + s.length() / 4);
		decodeString(sb, s);
		return sb.toString();
	}
}
