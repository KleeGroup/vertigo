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

import io.vertigo.lang.Assertion;

/**
 * Rule to named a sub rules set.
 *
 * @author pchretien, npiedeloup
 * @param <R> Type of the product text parsing
 */
final class PegGrammarRule<R> implements PegRule<R> {

	private final String ruleName;
	private final PegRule<R> mainRule;

	PegGrammarRule(final PegRule<R> mainRule, final String ruleName) {
		Assertion.checkNotNull(mainRule, "MainRule is required");
		Assertion.checkArgNotEmpty(ruleName, "Name is required");
		//-----
		this.mainRule = mainRule;
		this.ruleName = ruleName;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return ruleName;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<R> parse(final String text, final int start) throws PegNoMatchFoundException {
		return mainRule.parse(text, start);
	}

	public String getRuleName() {
		return ruleName;
	}

	public PegRule<R> getRule() {
		return mainRule;
	}
}
