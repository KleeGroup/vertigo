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
package io.vertigo.commons.peg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

/**
 * The first rule that matches is taken.
 * If no rule is found then an notFoundException is thrown.
 *
 * @author pchretien
 */
final class PegChoiceRule implements PegRule<PegChoice> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 * @param rules the list of rules to test
	 */
	PegChoiceRule(final List<PegRule<?>> rules) {
		Assertion.checkNotNull(rules);
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		//A choice of rules/expressions is like that : (e1 | e2 | e3)
		expression = "("
				+ rules.stream()
						.map(PegRule::getExpression)
						.collect(Collectors.joining(" | "))
				+ ")";
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	List<PegRule<?>> getRules() {
		return rules;
	}

	@Override
	public PegResult<PegChoice> parse(final String text, final int start) throws PegNoMatchFoundException {
		PegNoMatchFoundException best = null;
		int bestIndex = -1;
		for (int choiceIndex = 0; choiceIndex < getRules().size(); choiceIndex++) {
			try {
				final PegResult<?> parserCursor = getRules().get(choiceIndex).parse(text, start);
				final int end = parserCursor.getIndex();
				if (end < bestIndex) {
					Assertion.checkNotNull(best, "best exception should be set at same time of bestIndex");
					//Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
					throw best;
				}
				final PegChoice value = new PegChoice(choiceIndex, parserCursor.getValue());
				return new PegResult<>(end, value);
			} catch (final PegNoMatchFoundException e) {
				//Tant que l'on a des erreurs sur l'évaluation des règles
				//on recommence jusqu'à trouver la première qui fonctionne.
				if (e.getIndex() > bestIndex) {
					bestIndex = e.getIndex();
					best = e;
				}
			}
		}
		//Nothing has been found
		if (best == null) {
			throw new PegNoMatchFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", getExpression());
		}
		throw best;
	}
}
