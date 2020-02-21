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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamox.search.dsl.model.DslBlockQuery;
import io.vertigo.dynamox.search.dsl.model.DslQuery;

/**
 * Parsing rule for query.
 * (preMultiQuery)\((queries|rangeQuery|fixedQuery|multiQuery|)+\)(postMultiQuery)
 * @author npiedeloup
 */
final class DslMultiQueryRule extends AbstractRule<DslBlockQuery, List<Object>> {
	private static final int MAX_DEPTH = 3;

	/**
	 * Constructor.
	 */
	DslMultiQueryRule() {
		this(0);
	}

	private DslMultiQueryRule(final int level) {
		super(createMainRule(level), "multiQuery-" + level);
	}

	private static PegRule<List<Object>> createMainRule(final int level) {
		if (level > MAX_DEPTH) {
			return (PegRule<List<Object>>) DslSyntaxRules.DEPTH_OVERFLOW;
		}

		final PegRule<PegChoice> queriesRule = PegRules.choice(//"single or multiple")
				new DslTermQueryRule(), //0
				new DslRangeQueryRule(), //1
				new DslMultiQueryRule(level + 1), //2
				new DslFixedQueryRule() //3
		);

		final PegRule<List<PegChoice>> manyQueriesRule = PegRules.oneOrMore(queriesRule, false);
		return PegRules.sequence(
				DslSyntaxRules.PRE_MODIFIER_VALUE, //0
				DslSyntaxRules.BLOCK_START,
				manyQueriesRule, //2
				DslSyntaxRules.BLOCK_END,
				DslSyntaxRules.POST_MODIFIER_VALUE); //4
	}

	/** {@inheritDoc} */
	@Override
	protected DslBlockQuery handle(final List<Object> parsing) {
		final String preQuery = (String) parsing.get(0);
		final List<DslQuery> queryDefinitions = new ArrayList<>();
		final String postQuery = (String) parsing.get(4);
		final List<PegChoice> manyQueries = (List<PegChoice>) parsing.get(2);
		for (final PegChoice item : manyQueries) {
			queryDefinitions.add((DslQuery) item.getValue());
		}
		return new DslBlockQuery(preQuery, queryDefinitions, postQuery);
	}
}
