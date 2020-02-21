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

import java.io.Serializable;

import io.vertigo.lang.Assertion;

/**
 * Gestion des Séparateurs utilisés par le parser.
 *
 * Un séparateur est défini
 * - soit par un caractère. (le même en début et fin)
 *      Exemple #  : #name#
 * - soit par des chaines de caractères.(qui peuvent être différentes)
 *      Exemple <% et %> : XXXX<%if (1=1){%>
 * ScriptSeparator implements Serializable because it's referenced by Serializable object (enum SeparatorType)
 * @author  pchretien
 */
public final class ScriptSeparator implements Serializable {
	private static final long serialVersionUID = -2124487462604625558L;

	/**
	 * Si le paramètre est délimité par une String.
	 * On distingue un séparateur de début et un autre de fin
	 */
	private final String beginSeparator;

	/**
	 * Séparateur de fin (String).
	 */
	private final String endSeparator;

	/**
	 * Constructor.
	 * Si le séparateur de début et de fin sont différents sous forme de String.
	 *
	 * @param beginSeparator Séparateur de début
	 * @param endSeparator Séparateur de fin
	 */
	public ScriptSeparator(final String beginSeparator, final String endSeparator) {
		Assertion.checkArgNotEmpty(beginSeparator);
		Assertion.checkArgNotEmpty(endSeparator);
		//-----
		this.beginSeparator = beginSeparator;
		this.endSeparator = endSeparator;
	}

	public String getBeginSeparator() {
		return beginSeparator;
	}

	public String getEndSeparator() {
		return endSeparator;
	}

	/**
	 * Returns the next position of the begin separator.
	 *
	 * @param script text
	 * @param start start
	 * @return the next position of the begin separator
	 */
	public int indexOfBeginCaracter(final String script, final int start) {
		return script.indexOf(beginSeparator, start);

	}

	/**
	 * Returns the next position of the end separator.
	 *
	 * @param script text
	 * @param start start
	 * @return the next position of the end separator
	 */
	public int indexOfEndCaracter(final String script, final int start) {
		return script.indexOf(endSeparator, start);

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return beginSeparator + " ; " + endSeparator;
	}
}
