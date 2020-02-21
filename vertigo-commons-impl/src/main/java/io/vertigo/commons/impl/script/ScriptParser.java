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
package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.lang.Assertion;

/**
 * Parse une chaine de caractères.
 * (script SQL ou autre)
 * Notifie le handler des paramètres trouvés.
 *
 * @author  pchretien
 */
final class ScriptParser {
	/**
	 * Séparateurs admis pour cet analyseur.
	 */
	private final ScriptSeparator separator;

	/**
	 * Index de début du paramètre courant
	 * On commence au début.
	 * (-1 pour éviter de squizzer le premier caractère)
	 */
	private int currentBeginCar = -1;

	/**
	 * Type (du séparateur) de paramètre courant
	 */
	private ScriptSeparator currentSeparator;

	/**
	 * Constructor.
	 * @param separators Séparateurs pris en compte
	 */
	ScriptParser(final ScriptSeparator separator) {
		Assertion.checkNotNull(separator);
		//-----
		this.separator = separator;
	}

	/**
	 * Parse le script, notifie le handler.
	 * La grammaire est constituées de simples balises (Séparateurs).
	 *
	 * @param script Script à analyser
	 * @param scriptHandler Handler gérant la grammaire analysée
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler) {
		Assertion.checkNotNull(script);
		Assertion.checkNotNull(scriptHandler);
		//-----
		int index = 0;
		int endCar = -1;

		// On isole les chaines entre 2 séparateurs
		while (nextPosition(script, endCar + 1)) {
			endCar = currentSeparator.indexOfEndCaracter(script, currentBeginCar + 1);
			//Il faut qu'il y ait le caractère de fin correspondant.
			if (endCar == -1) {
				throw new IllegalStateException("Aucun séparateur de fin trouvé pour " + currentSeparator + " à la position " + currentBeginCar + " sur " + script);
			}

			onRequestText(scriptHandler, script, index, currentBeginCar);
			//Si le séparateur est une chaine de caractères, sa longueur doit être calculée.
			onRequestParam(scriptHandler, script, currentBeginCar + currentSeparator.getBeginSeparator().length(), endCar, currentSeparator);
			index = endCar + currentSeparator.getEndSeparator().length();
		}
		onRequestText(scriptHandler, script, index, script.length());
	}

	private static void onRequestParam(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar, final ScriptSeparator separator) {
		if (endCar == beginCar) {
			throw new IllegalArgumentException("Empty parameter");
		}
		scriptHandler.onExpression(script.substring(beginCar, endCar), separator);
	}

	private static void onRequestText(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar) {
		scriptHandler.onText(script.substring(beginCar, endCar));
	}

	/**
	 * Recherche dans une requête la position de la prochaine occurence
	 * d'un paramètre.
	 * @param script Script
	 * @param beginCar Index de début de recherche
	 *
	 * @return Si il existe une prochaine position trouvée.
	 */
	private boolean nextPosition(final String script, final int beginCar) {
		int minPosition = Integer.MAX_VALUE;
		ScriptSeparator found = null;
		final int position = separator.indexOfBeginCaracter(script, beginCar);
		if (position != -1 && position < minPosition) {
			minPosition = position;
			found = separator;
		}
		/*
		 * If there is a separator
		 * then we return its position
		 */
		if (found == null) {
			currentSeparator = null;
			currentBeginCar = -1;
			return false;
		}
		currentSeparator = found;
		currentBeginCar = minPosition;
		return true;

	}
}
