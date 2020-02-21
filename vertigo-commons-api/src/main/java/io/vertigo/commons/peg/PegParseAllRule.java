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
 *
 * Ensure inner rule match all input : start at idx 0 and finish at last idx
 * @author npiedeloup
 */
final class PegParseAllRule<O> implements PegRule<O> {
	private final PegRule<O> innerRule;
	private final String expression;

	/**
	 * Constructor.
	 */
	PegParseAllRule(final PegRule<O> innerRule) {
		Assertion.checkNotNull(innerRule);
		//-----
		this.innerRule = innerRule;
		//---
		expression = "^" + innerRule.getExpression() + "$";
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<O> parse(final String text, final int start) throws PegNoMatchFoundException {
		final O result;
		if (start > 0) {
			throw new PegNoMatchFoundException(text, start, null, "Can't parse from beginning");
		}
		int index = start;
		try {
			final PegResult<O> cursor = innerRule
					.parse(text, index);
			index = cursor.getIndex();
			result = cursor.getValue();
			if (index < text.length()) {
				throw new PegNoMatchFoundException(text, start, null, "Can't parse whole input (parse until {0})", index);
			}

		} catch (final PegNoMatchFoundException e) {
			throw new PegNoMatchFoundException(text, e.getIndex(), e, getExpression());
		}
		return new PegResult<>(index, result);
	}
}
