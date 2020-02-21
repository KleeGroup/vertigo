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

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.commons.peg.PegWordRule;
import io.vertigo.dynamox.search.dsl.model.DslFixedQuery;
import io.vertigo.dynamox.search.dsl.model.DslQuery;
import io.vertigo.dynamox.search.dsl.model.DslRangeQuery;

/**
 * Parsing rule for query.
 * (preRangeQuery)\[(termQuery|fixedQuery) to (termQuery|fixedQuery)\](postRangeQuery)
 * @author npiedeloup
 */
final class DslRangeQueryRule extends AbstractRule<DslRangeQuery, List<Object>> {

	DslRangeQueryRule() {
		super(createMainRule(), "rangeQuery");
	}

	private static PegRule<List<Object>> createMainRule() {
		final PegRule<PegChoice> queriesRule = PegRules.choice(//"term or fixed")
				PegRules.term("*"), //0
				new DslTermQueryRule(), //1
				new DslFixedQueryRule() //2
		);

		return PegRules.sequence(
				DslSyntaxRules.PRE_MODIFIER_VALUE, //0
				PegRules.choice(PegRules.term("["), PegRules.term("{")), //1
				queriesRule, //2
				DslSyntaxRules.SPACES,
				PegRules.word(false, "TOto", PegWordRule.Mode.ACCEPT, "to"),
				DslSyntaxRules.SPACES,
				queriesRule, //6
				DslSyntaxRules.SPACES,
				PegRules.choice(PegRules.term("]"), PegRules.term("}")), //8
				DslSyntaxRules.POST_MODIFIER_VALUE); //9
	}

	/** {@inheritDoc} */
	@Override
	protected DslRangeQuery handle(final List<Object> parsing) {
		final String preQuery = (String) parsing.get(0);
		final PegChoice startChoice = (PegChoice) parsing.get(1);

		final PegChoice startTermQuery = (PegChoice) parsing.get(2);
		final DslQuery startQueryDefinitions;
		if (startTermQuery.getChoiceIndex() == 0) {
			startQueryDefinitions = new DslFixedQuery("*");
		} else {
			startQueryDefinitions = (DslQuery) startTermQuery.getValue();
		}

		final PegChoice endTermQuery = (PegChoice) parsing.get(6);
		final DslQuery endQueryDefinitions;
		if (endTermQuery.getChoiceIndex() == 0) {
			endQueryDefinitions = new DslFixedQuery("*");
		} else {
			endQueryDefinitions = (DslQuery) endTermQuery.getValue();
		}

		final PegChoice endChoice = (PegChoice) parsing.get(8);
		final String postQuery = (String) parsing.get(9);

		final String startRange = (String) startChoice.getValue();
		final String endRange = (String) endChoice.getValue();
		return new DslRangeQuery(preQuery, startRange, startQueryDefinitions, endQueryDefinitions, endRange, postQuery);
	}
}
