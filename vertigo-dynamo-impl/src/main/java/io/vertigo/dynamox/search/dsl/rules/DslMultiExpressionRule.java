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
import java.util.Optional;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamox.search.dsl.model.DslExpression;
import io.vertigo.dynamox.search.dsl.model.DslMultiExpression;

/**
 * Parsing rule for query.
 * (preMultiExpression)\((expression|multiExpression)+\)(postMultiExpression)
 * @author npiedeloup
 */
final class DslMultiExpressionRule extends AbstractRule<DslMultiExpression, PegChoice> {
	private static final int MAX_DEPTH = 3;

	/**
	 * Constructor.
	 */
	DslMultiExpressionRule() {
		this(0);
		//At the beginning the level is always 0
	}

	private DslMultiExpressionRule(final int level) {
		super(createMainRule(level));
	}

	private static PegRule<PegChoice> createMainRule(final int level) {
		if (level > MAX_DEPTH) {
			return (PegRule<PegChoice>) DslSyntaxRules.DEPTH_OVERFLOW;
		}
		final PegRule<PegChoice> expressionsRule = PegRules.choice(//"single or multiple")
				new DslExpressionRule(), //0
				new DslMultiExpressionRule(level + 1) //1
		);
		final PegRule<List<PegChoice>> manyExpressionRule = PegRules.oneOrMore(expressionsRule, false);
		final PegRule<List<Object>> blockExpressionRule = PegRules.sequence(
				PegRules.optional(new DslBooleanOperatorRule()), //0
				DslSyntaxRules.PRE_MODIFIER_VALUE, //1
				DslSyntaxRules.BLOCK_START, //2
				manyExpressionRule, //3
				DslSyntaxRules.BLOCK_END, //4
				DslSyntaxRules.POST_MODIFIER_VALUE); //5
		return PegRules.choice(//"single or multiple")
				blockExpressionRule, //0
				manyExpressionRule //1
		);
	}

	/** {@inheritDoc} */
	@Override
	protected DslMultiExpression handle(final PegChoice parsing) {
		final String preMultiExpression;
		final String postMultiExpression;
		//---
		final List<PegChoice> many;
		switch (parsing.getChoiceIndex()) {
			case 0:
				final List<?> blockExpression = (List<?>) parsing.getValue();
				preMultiExpression = ((Optional<String>) blockExpression.get(0)).orElse("") + blockExpression.get(1);
				many = (List<PegChoice>) blockExpression.get(3);
				postMultiExpression = (String) blockExpression.get(5);
				break;
			case 1:
				preMultiExpression = "";
				many = (List<PegChoice>) parsing.getValue();
				postMultiExpression = "";
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getChoiceIndex() + " not implemented");
		}

		final List<DslExpression> expressionDefinitions = new ArrayList<>();
		final List<DslMultiExpression> multiExpressionDefinitions = new ArrayList<>();

		//On récupère le produit de la règle many
		for (final PegChoice item : many) {
			switch (item.getChoiceIndex()) {
				case 0:
					expressionDefinitions.add((DslExpression) item.getValue());
					break;
				case 1:
					multiExpressionDefinitions.add((DslMultiExpression) item.getValue());
					break;
				default:
					throw new IllegalArgumentException("case " + item.getChoiceIndex() + " not implemented");
			}
		}
		final boolean block = parsing.getChoiceIndex() == 0;
		//---
		return new DslMultiExpression(preMultiExpression, block, expressionDefinitions, multiExpressionDefinitions, postMultiExpression);
	}
}
