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
package io.vertigo.account.impl.authorization.dsl.rules;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.account.authorization.metamodel.rulemodel.RuleExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression.BoolOperator;
import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * Parsing rule for query.
 * \(?(expression1|multiExpression1) ((logicalOperator) (expression2|multiExpression2))*\)?
 * @author npiedeloup
 */
final class DslMultiExpressionRule extends AbstractRule<RuleMultiExpression, PegChoice> {
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
		final PegRule<List<Object>> nextExpressionsRule = PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				new DslOperatorRule<>(RuleMultiExpression.BoolOperator.values(), "boolOperator"), //1
				DslSyntaxRules.SPACES, //2
				expressionsRule, //3
				DslSyntaxRules.SPACES //4
		);
		final PegRule<List<List<Object>>> manyNextExpressionsRule = PegRules.zeroOrMore(nextExpressionsRule, false);
		final PegRule<List<Object>> multiExpressionRule = PegRules.named(
				PegRules.sequence(
						DslSyntaxRules.SPACES, //0
						expressionsRule, //1
						DslSyntaxRules.SPACES, //2
						manyNextExpressionsRule, //3
						DslSyntaxRules.SPACES //4
				), "multiExpressionRule");
		final PegRule<List<Object>> blockMultiExpressionRule = PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				DslSyntaxRules.BLOCK_START, //1
				multiExpressionRule, //2
				DslSyntaxRules.BLOCK_END, //3
				DslSyntaxRules.SPACES //4
		);
		if (level == 0) {
			return PegRules.choice(//"block or not")
					PegRules.parseAll(blockMultiExpressionRule), //0
					PegRules.parseAll(multiExpressionRule) //1
			);
		}
		return PegRules.choice(//"block or not")
				blockMultiExpressionRule, //0
				multiExpressionRule //1
		);
	}

	/** {@inheritDoc} */
	@Override
	protected RuleMultiExpression handle(final PegChoice parsing) {
		final List<Object> innerBlock;
		switch (parsing.getChoiceIndex()) {
			case 0:
				final List<?> blockExpression = (List<?>) parsing.getValue();
				innerBlock = (List<Object>) blockExpression.get(2);
				break;
			case 1:
				innerBlock = (List<Object>) parsing.getValue();
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getChoiceIndex() + " not implemented");
		}

		final List<RuleExpression> expressionDefinitions = new ArrayList<>();
		final List<RuleMultiExpression> multiExpressionDefinitions = new ArrayList<>();

		final PegChoice firstExpressionChoice = (PegChoice) innerBlock.get(1); //first (expression1|multiExpression1)
		switch (firstExpressionChoice.getChoiceIndex()) {
			case 0:
				expressionDefinitions.add((RuleExpression) firstExpressionChoice.getValue());
				break;
			case 1:
				multiExpressionDefinitions.add((RuleMultiExpression) firstExpressionChoice.getValue());
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getChoiceIndex() + " not implemented");
		}

		final List<List<Object>> many = (List<List<Object>>) innerBlock.get(3); //manyNextExpressionsRule
		//On récupère le produit de la règle many
		BoolOperator operator = null;
		for (final List<Object> item : many) {
			if (operator != null && operator != item.get(1)) {
				throw new IllegalArgumentException("Can't use different operator in same block, attempt to find " + operator);
			}
			operator = (BoolOperator) item.get(1);
			final PegChoice nextExpressionChoice = (PegChoice) item.get(3); //next (expression2|multiExpression2)
			switch (nextExpressionChoice.getChoiceIndex()) {
				case 0:
					expressionDefinitions.add((RuleExpression) nextExpressionChoice.getValue());
					break;
				case 1:
					multiExpressionDefinitions.add((RuleMultiExpression) nextExpressionChoice.getValue());
					break;
				default:
					throw new IllegalArgumentException("case " + nextExpressionChoice.getChoiceIndex() + " not implemented");
			}
		}
		final boolean block = parsing.getChoiceIndex() == 0;
		//---
		return new RuleMultiExpression(block, operator != null ? operator : BoolOperator.AND, expressionDefinitions, multiExpressionDefinitions);
	}
}
