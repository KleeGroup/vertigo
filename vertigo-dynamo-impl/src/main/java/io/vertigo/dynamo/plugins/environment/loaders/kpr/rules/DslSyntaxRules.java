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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.commons.peg.PegWordRule;

/**
 *
 * Les lettres interdites dans les mots sont les suivantes espace
 * =();[]"/.
 *
 * @author pchretien
 */
final class DslSyntaxRules {
	/** Liste des caractères réservés. */
	private static final String RESERVED = "\"=();[]/,{}:";
	/** Liste des caractères blancs. */
	private static final String WHITE_SPACE = " \t\n\r";
	/** Liste des délimiteurs. */
	private static final String DELIMITERS = RESERVED + WHITE_SPACE;

	/** règle de suppression des blancs. */
	static final PegRule<?> SPACES = PegRules.skipBlanks(WHITE_SPACE);

	static final PegRule<String> ARRAY_START = PegRules.term("["); //like arrays in json syntax
	static final PegRule<String> ARRAY_END = PegRules.term("]");
	static final PegRule<String> ARRAY_SEPARATOR = PegRules.term(",");

	static final PegRule<String> OBJECT_START = PegRules.term("{"); //like json { name:"john doe", city:"kjkjk"}
	static final PegRule<String> OBJECT_END = PegRules.term("}");
	static final PegRule<String> OBJECT_SEPARATOR = PegRules.term(",");

	static final PegRule<String> PAIR_SEPARATOR = PegRules.term(":"); //name:"bill"
	static final PegRule<String> QUOTATION_MARK = PegRules.term("\"");

	static final PegRule<String> PROPERTY_VALUE = PegRules.word(false, "\"", PegWordRule.Mode.REJECT_ESCAPABLE, "propertyValue"); //En fait il faut autoriser tous les caractères sauf les guillemets".
	//Il faut gérer le caractère d'évitement.
	static final PegRule<String> WORD = PegRules.word(false, DELIMITERS, PegWordRule.Mode.REJECT, "word");

	private DslSyntaxRules() {
		//Classe sans état
	}

}
