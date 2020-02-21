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
import java.util.Locale;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * Parsing rule for boolean operator.
 * || or OR && and AND
 * @author npiedeloup
 */
final class DslBooleanOperatorRule extends AbstractRule<String, List<Object>> {

	DslBooleanOperatorRule() {
		super(createMainRule(), "boolOperator");
	}

	private static PegRule<List<Object>> createMainRule() {
		return PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				PegRules.choice(//"single or multiple") //1
						PegRules.term("AND"), //0
						PegRules.term("and"), //1
						PegRules.term("And"), //2
						PegRules.term("&&"), //3
						PegRules.term("OR"), //4
						PegRules.term("Or"), //5
						PegRules.term("or"), //6
						PegRules.term("||")), //7
				DslSyntaxRules.SPACES); //2
	}

	/** {@inheritDoc} */
	@Override
	protected String handle(final List<Object> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String operator = (String) ((PegChoice) parsing.get(1)).getValue();
		final String postSpaces = (String) parsing.get(2);
		return DslUtil.concat(preSpaces, operator.toUpperCase(Locale.ENGLISH), postSpaces); //toUpperCase car ES n'interprete pas correctement en lowercase
	}
}
