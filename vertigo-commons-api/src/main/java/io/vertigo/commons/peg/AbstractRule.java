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
 * Une règle peut être vue comme
 * - la création d'une règle principale
 * - la gestion du résultat du parsing
 *
 * @author pchretien
 * @param <R> Type of the product text parsing
 * @param <M> Type of the parent parsing rule
 */
public abstract class AbstractRule<R, M> implements PegRule<R> {
	private final PegRule<M> mainRule;
	private final PegRule<M> innerRule;
	private final String expression;

	protected AbstractRule(final PegRule<M> mainRule) {
		Assertion.checkNotNull(mainRule);
		//-----
		this.innerRule = mainRule;
		this.mainRule = innerRule;
		this.expression = mainRule.getExpression();
	}

	protected AbstractRule(final PegRule<M> mainRule, final String ruleName) {
		Assertion.checkNotNull(mainRule);
		Assertion.checkArgNotEmpty(ruleName);
		//-----
		this.innerRule = mainRule;
		this.mainRule = PegRules.named(innerRule, ruleName);
		this.expression = mainRule.getExpression();
	}

	protected final PegRule<M> getMainRule() {
		return mainRule;
	}

	protected final String getRuleName() {
		return this.getClass().getSimpleName() + innerRule.getExpression().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public final String getExpression() {
		return expression;
	}

	protected abstract R handle(M parsing);

	/** {@inheritDoc} */
	@Override
	public final PegResult<R> parse(final String text, final int start) throws PegNoMatchFoundException {
		final PegResult<M> parserCursor = getMainRule()
				.parse(text, start);
		final int end = parserCursor.getIndex();
		//---
		final R result = handle(parserCursor.getValue());
		//---
		return new PegResult<>(end, result);
	}
}
