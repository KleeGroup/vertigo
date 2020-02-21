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

import java.util.Optional;

import io.vertigo.lang.Assertion;

/**
 * An optional rule.
 * This rule is usefull each time there is an optional part inside a more global expression.
 *
 * @author pchretien
 * @param <R> Type of the product text parsing
 */
final class PegOptionalRule<R> implements PegRule<Optional<R>> {
	private final String expression;
	private final PegRule<R> rule;

	/**
	 * Constructor.
	 * @param rule Optional rule
	 */
	PegOptionalRule(final PegRule<R> rule) {
		super();
		Assertion.checkNotNull(rule);
		//-----
		expression = rule.getExpression() + "?";
		this.rule = rule;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	PegRule<R> getRule() {
		return rule;
	}

	@Override
	public PegResult<Optional<R>> parse(final String text, final int start) throws PegNoMatchFoundException {
		try {
			final PegResult<R> result = rule.parse(text, start);
			final Optional<R> option = Optional.ofNullable(result.getValue());
			return new PegResult<>(result.getIndex(), option);
		} catch (final PegNoMatchFoundException e) {
			//As the rule is optional, if we found nothing then the index doesn't move and no exception is thrown.
			return new PegResult<>(start, Optional.empty());
		}
	}
}
