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

import java.util.List;

import io.vertigo.account.authorization.metamodel.rulemodel.RuleUserPropertyValue;
import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * Parsing rule for userProperty.
 * ${(userProperty)}
 * @author npiedeloup
 */
final class DslUserPropertyValueRule extends AbstractRule<RuleUserPropertyValue, List<Object>> {

	DslUserPropertyValueRule() {
		super(createMainRule(), "userProperty");
	}

	private static PegRule<List<Object>> createMainRule() {
		return PegRules.sequence(
				DslSyntaxRules.PRE_USER_PROPERTY_VALUE, //0
				DslSyntaxRules.WORD, //1
				DslSyntaxRules.POST_USER_PROPERTY_VALUE); //2
	}

	/** {@inheritDoc} */
	@Override
	protected RuleUserPropertyValue handle(final List<Object> parsing) {
		final String userProperty = (String) parsing.get(1);
		return new RuleUserPropertyValue(userProperty);
	}

}
