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
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamox.search.dsl.model.DslFixedQuery;

/**
 * Parsing rule for fixedQuery.
 * (fixedQuery)
 * @author npiedeloup
 */
final class DslFixedQueryRule extends AbstractRule<DslFixedQuery, List<Object>> {

	DslFixedQueryRule() {
		super(createMainRule(), "fixedQuery");
	}

	private static PegRule<List<Object>> createMainRule() {
		return PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				DslSyntaxRules.FIXED_WORD); //1
	}

	/** {@inheritDoc} */
	@Override
	protected DslFixedQuery handle(final List<Object> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String fixedQuery = (String) parsing.get(1);
		return new DslFixedQuery(DslUtil.concat(preSpaces, fixedQuery));
	}

}
