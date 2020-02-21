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
package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.commons.peg.PegResult;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.commons.peg.PegWordRule;

/**
 *
 * Les lettres interdites dans les mots sont les suivantes espace
 * =();[]"/.
 *
 * @author npiedeloup
 */
final class DslSyntaxRules {
	/** Liste des caractères réservés. */
	private static final String RESERVED = "()[]\"!#$%&'*+,-./:;<=>?@\\^`|~";
	/** Liste des caractères blancs. */
	private static final String WHITE_SPACE = " \t\n\r";

	private static final String PRE_MODIFIER = "~+-*?\"";
	private static final String POST_MODIFIER = "~+-*?^0123456789\"";

	/** Liste des délimiteurs. */
	private static final String DELIMITERS = RESERVED + WHITE_SPACE;

	/** règle de lectures des blancs. */
	static final PegRule<?> SPACES = PegRules.word(true, WHITE_SPACE, PegWordRule.Mode.ACCEPT, "_");

	/** array start. */
	static final PegRule<String> ARRAY_START = PegRules.term("["); //like arrays in json syntax
	/** array end. */
	static final PegRule<String> ARRAY_END = PegRules.term("]");
	/** array separator. */
	static final PegRule<String> ARRAY_SEPARATOR = PegRules.term(",");

	/** block start. */
	static final PegRule<String> BLOCK_START = PegRules.term("(");
	/** block end. */
	static final PegRule<String> BLOCK_END = PegRules.term(")");

	/** term mark. */
	static final PegRule<String> TERM_MARK = PegRules.term("#");
	/** field end. */
	static final PegRule<String> FIELD_END = PegRules.term(":");

	/** premodifier. */
	static final PegRule<String> PRE_MODIFIER_VALUE = PegRules.word(true, PRE_MODIFIER + WHITE_SPACE, PegWordRule.Mode.ACCEPT, "[~+-*?\" \t\n\r]*");
	/** postmodifier. */
	static final PegRule<String> POST_MODIFIER_VALUE = PegRules.word(true, POST_MODIFIER, PegWordRule.Mode.ACCEPT, "[~+-*?^0123456789\"]*");

	//Il faut gérer le caractère d'évitement.
	/** word. */
	static final PegRule<String> WORD = PegRules.word(false, DELIMITERS, PegWordRule.Mode.REJECT, "WORD");

	/** fixed word. */
	static final PegRule<String> FIXED_WORD = PegRules.word(false, WHITE_SPACE + "]),", PegWordRule.Mode.REJECT, "!_");

	/** depth overflow. */
	static final PegRule<?> DEPTH_OVERFLOW = new DepthOverflowRule();

	private static class DepthOverflowRule implements PegRule<Void> {

		@Override
		public String getExpression() {
			return "'depthOverflow'";
		}

		@Override
		public PegResult<Void> parse(final String text, final int start) throws PegNoMatchFoundException {
			throw new PegNoMatchFoundException(text, start, null, "Too deep", getExpression());
		}
	}

	private DslSyntaxRules() {
		//Classe sans état
	}

}
