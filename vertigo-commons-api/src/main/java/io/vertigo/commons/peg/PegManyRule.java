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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * The manyRule.
 * If the pattern AB is searched and
 * the text is
 *  - empty : an empty list is returned only if emptyAccepted.
 *  - ABAB : a list with a size of 2 is returned
 *  - ABABC : a list with a size of 2 is returned only if repeat is false.
 *
 *  if repeat is true then all the text must be consumed during the evaluation.
 *
 * @author pchretien
 * @param <R> Type of the product text parsing
 */
final class PegManyRule<R> implements PegRule<List<R>> {
	private final PegRule<R> rule;
	private final String expression;
	private final boolean zeroAccepted;
	private final boolean repeat;

	/**
	 * Constructor.
	 * @param rule the rule that's will be evaluated
	 * @param zeroAccepted zeroOrMore else oneOrMore
	 * @param repeat if the evaluation must be repeated
	 */
	PegManyRule(final PegRule<R> rule, final boolean zeroAccepted, final boolean repeat) {
		Assertion.checkNotNull(rule);
		//-----
		this.rule = rule;
		this.zeroAccepted = zeroAccepted;
		this.repeat = repeat;
		expression = rule.getExpression() + (zeroAccepted ? "*" : "+");
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	boolean isEmptyAccepted() {
		return zeroAccepted;
	}

	PegRule<R> getRule() {
		return rule;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<List<R>> parse(final String text, final int start) throws PegNoMatchFoundException {
		int index = start;
		//-----
		final List<R> results = new ArrayList<>();
		PegNoMatchFoundException best = null;
		try {
			int prevIndex = -1;
			while (index < text.length() && index > prevIndex) {
				prevIndex = index;
				final PegResult<R> parserCursor = getRule()
						.parse(text, index);
				index = parserCursor.getIndex();
				if (index > prevIndex) {
					//celé signifie que l"index n a pas avancé, on sort
					results.add(parserCursor.getValue());
				}
			}
		} catch (final PegNoMatchFoundException e) {
			best = e;
			if (best.getIndex() > index) {
				//Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
				throw best;
			}
		}
		if (!isEmptyAccepted() && results.isEmpty()) {
			throw new PegNoMatchFoundException(text, start, best, "Aucun élément de la liste trouvé : {0}", getExpression());
		}
		if (repeat && text.length() > index) {
			throw new PegNoMatchFoundException(text, start, best, "{0} élément(s) trouvé(s), éléments suivants non parsés selon la règle :{1}", results.size(), getExpression());
		}
		return new PegResult<>(index, results);
	}
}
