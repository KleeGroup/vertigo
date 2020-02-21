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

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.commons.peg.PegWordRule;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery.EscapeMode;

/**
 * Parsing rule for query.
 * (preQuery)(term)(postQuery)
 * @author npiedeloup
 */
final class DslTermQueryRule extends AbstractRule<DslTermQuery, List<Object>> {

	DslTermQueryRule() {
		super(createMainRule(), "query");
	}

	private static PegRule<List<Object>> createMainRule() {
		final PegRule<PegChoice> escapeModeRule = PegRules.choice(
				PegRules.term("?(removeReserved)"), //choice 0
				PegRules.term("?(escapeReserved)")); //choice 1

		final PegRule<List<Object>> defaultValueRule = PegRules.sequence(
				PegRules.term("!("),
				PegRules.word(false, ")", PegWordRule.Mode.REJECT, "[^)]"), //1
				PegRules.term(")"));

		final PegRule<List<Object>> termRule = PegRules.sequence(
				DslSyntaxRules.TERM_MARK,
				DslSyntaxRules.PRE_MODIFIER_VALUE, //1
				DslSyntaxRules.WORD, //2
				DslSyntaxRules.POST_MODIFIER_VALUE, //3
				DslSyntaxRules.TERM_MARK,
				PegRules.optional(escapeModeRule), //5
				PegRules.optional(defaultValueRule)); //6

		return PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				DslSyntaxRules.PRE_MODIFIER_VALUE, //1
				termRule, //2
				DslSyntaxRules.POST_MODIFIER_VALUE); //3
	}

	/** {@inheritDoc} */
	@Override
	protected DslTermQuery handle(final List<Object> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String preQuery = (String) parsing.get(1);

		final List<Object> term = (List<Object>) parsing.get(2);
		final String preTerm = (String) term.get(1);
		final String termField = (String) term.get(2);
		final String postTerm = (String) term.get(3);
		final Optional<PegChoice> escapeRule = (Optional<PegChoice>) term.get(5);
		final EscapeMode escapeMode;
		if (escapeRule.isPresent()) {
			switch (escapeRule.get().getChoiceIndex()) {
				case 0:
					escapeMode = EscapeMode.remove;
					break;
				case 1:
					escapeMode = EscapeMode.escape;
					break;
				default:
					throw new IllegalArgumentException("case " + escapeRule.get().getChoiceIndex() + " not implemented");
			}
		} else {
			escapeMode = EscapeMode.none;
		}
		final Optional<List<Object>> defaultRuleOpt = (Optional<List<Object>>) term.get(6);
		final Optional<String> defaultValue = defaultRuleOpt.map(defaultRule -> (String) defaultRule.get(1));

		final String postQuery = (String) parsing.get(3);
		//final String postSpaces = (String) parsing.get(4);
		return new DslTermQuery(DslUtil.concat(preSpaces, preQuery), preTerm, termField, postTerm, escapeMode, defaultValue, postQuery);
	}

}
