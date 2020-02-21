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
package io.vertigo.dynamo.plugins.collections.lucene_6_6;

/**
 * Centralisation du paramétrage de l'analyseur lucene.
 *
 * @author  pchretien
 */
final class LuceneConstants {

	/**
	 * Tableau des mots vides en Français et en Anglais.
	 */
	static final String[] OUR_STOP_WORDS = {
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
			// Anglais
			"an", "and", "are", "as", "at", "be", "by", "for", "in", "into", "is",
			"it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then",
			"there", "these", "they", "this", "to", "was", "with", "will", "over", "he", "she",
			// Français
			"et", "ou", "au", "aux", "le", "la", "les", "du", "des", "leur", "leurs",
			"dans", "en", "ce", "ca", "ces", "cet", "cette", "qui", "que", "quoi",
			"dont", "quel", "quels", "quelle", "quelles", "pour", "comme", "il", "elle",
			"lui", "par", "avec", "cela", "celui-ci", "celui-là", "celui", "ceux", "celles",
			"non", "oui", "sur", "car", "de", "donc", "dont", "elles", "est", "ils", "je",
			"lors", "me", "mes", "mon", "ni", "nos", "notre", "nôtre", "nous", "on", "sa",
			"se", "ses", "si", "son", "ta", "tes", "ton", "tous", "tout", "toutes", "tu",
			"un", "une", "unes", "uns", "voici", "voilà", "vos", "votre", "vous", "vôtre"
	};

	/**
	 * Tableau des elisions.
	 */
	static final String[] ELISION_ARTICLES = { "l", "m", "t", "qu", "n", "s", "j" };

	private LuceneConstants() {
		//
	}
}
