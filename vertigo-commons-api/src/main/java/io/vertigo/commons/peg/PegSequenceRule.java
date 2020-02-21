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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

/**
 * As wikipedia says
 * The sequence operator e1 e2 first invokes e1,
 * and if e1 succeeds, subsequently invokes e2 on the remainder of the input string left unconsumed by e1,
 * and returns the result.
 * If either e1 or e2 fails, then the sequence expression e1 e2 fails.
 *
 * @author pchretien
 */
final class PegSequenceRule implements PegRule<List<Object>> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 */
	PegSequenceRule(final List<PegRule<?>> rules) {
		Assertion.checkNotNull(rules);
		Assertion.checkArgument(rules.size() > 1, "A sequence must contain at least 2 rules");
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		//A sequence of rules/expressions is like that : (e1 e2 e3)
		expression = "("
				+ this.rules.stream()
						.map(PegRule::getExpression)
						.collect(Collectors.joining(" "))
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

	/** {@inheritDoc} */
	@Override
	public PegResult<List<Object>> parse(final String text, final int start) throws PegNoMatchFoundException {
		final List<Object> results = new ArrayList<>();
		int index = start;
		try {
			for (final PegRule<? extends Object> rule : rules) {
				final PegResult<? extends Object> cursor = rule
						.parse(text, index);
				index = cursor.getIndex();
				results.add(cursor.getValue());
			}
		} catch (final PegNoMatchFoundException e) {
			throw new PegNoMatchFoundException(text, e.getIndex(), e, getExpression());
		}
		return new PegResult<>(index, results);
	}
}
