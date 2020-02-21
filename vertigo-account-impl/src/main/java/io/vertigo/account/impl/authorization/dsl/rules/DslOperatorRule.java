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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.account.authorization.metamodel.rulemodel.RuleOperator;
import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * Parsing rule for boolean operator.
 * || or OR && and AND
 * @author npiedeloup
 */
final class DslOperatorRule<O extends RuleOperator> extends AbstractRule<O, List<Object>> {
	private final Map<String, O> operatorIndex;

	DslOperatorRule(final O[] operators, final String ruleName) {
		super(createMainRule(operators), ruleName);
		operatorIndex = (Map<String, O>) getOperatorIndex(operators);
	}

	private static PegRule<List<Object>> createMainRule(final RuleOperator[] operators) {
		final List<PegRule<?>> operatorRules = new ArrayList<>();
		for (final String operator : getOperatorIndex(operators).keySet()) {
			operatorRules.add(PegRules.term(operator));
		}
		return PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				PegRules.choice(operatorRules), //1
				DslSyntaxRules.SPACES); //2
	}

	private static Map<String, RuleOperator> getOperatorIndex(final RuleOperator[] operators) {
		final Map<String, RuleOperator> operatorIndex = new LinkedHashMap<>();
		for (final RuleOperator operator : operators) {
			for (final String authorizedString : operator.authorizedString()) {
				operatorIndex.put(authorizedString, operator);
			}
		}
		return operatorIndex;
	}

	/** {@inheritDoc} */
	@Override
	protected O handle(final List<Object> parsing) {
		final String operatorStr = (String) ((PegChoice) parsing.get(1)).getValue();
		return operatorIndex.get(operatorStr);
	}
}
