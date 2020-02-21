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
package io.vertigo.commons.script.parser;

/**
 * Permet de traiter un script parsé.
 * Notification des nouveaux paramètres trouvés lors du parsing d'un script.
 * - Soit on trouve des expressions entre deux séparateurs.
 * - Soit on trouve du texte.
 *
 * - Les séparateurs sont définis par
 * - un caractère (En ce cas le doubler signifie que l'on souhaite conserver le caractère en tant que texte)
 * - une chaine de début et une de fin.
 *
 * @author  pchretien
 */
public interface ScriptParserHandler {
	/**
	 * Evénement lors du parsing d'un paramètre compris entre 2 séparateurs.
	 *
	 * @param expression String Chaine comprise entre les 2 séparateurs
	 * @param separator Séparateur de début
	 */
	void onExpression(String expression, ScriptSeparator separator);

	/**
	 * Evénement lors du parsing indique la zone entre deux expressions.
	 * @param text Texte compris entre les 2 séparateurs
	 */
	void onText(String text);
}
