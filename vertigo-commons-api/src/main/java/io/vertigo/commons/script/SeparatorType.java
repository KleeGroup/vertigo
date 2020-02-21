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
package io.vertigo.commons.script;

import io.vertigo.commons.script.parser.ScriptSeparator;

/**
 * Type de séparateur.
 * Permet de définir les types de séparateurs utilisés dans les fichiers.
 *
 * @author  pchretien
 */
public enum SeparatorType {

	/**
	 * Séparateurs de type XML/HTML.
	 */
	XML("&lt;%", "%&gt;"),
	/**
	 * Séparateur de type text.
	 */
	CLASSIC(SeparatorType.BEGIN_SEPARATOR_CLASSIC, SeparatorType.END_SEPARATOR_CLASSIC),
	/**
	 * Séparateur de code dans du XML.
	 */
	XML_CODE("&lt;#", "#&gt;");

	/**
	 * Début d'une balise d'évaluation classique.
	 */
	public static final String BEGIN_SEPARATOR_CLASSIC = "<%";

	/**
	 * Fin d'une balise d'évaluation classique.
	 */
	public static final String END_SEPARATOR_CLASSIC = "%>";
	private final ScriptSeparator separator;

	SeparatorType(final String startExpression, final String endExpression) {
		separator = new ScriptSeparator(startExpression, endExpression);
	}

	/**
	 * @return Liste des ScriptSeparator pour ce SeparatorType.
	 */
	public ScriptSeparator getSeparator() {
		return separator;
	}
}
