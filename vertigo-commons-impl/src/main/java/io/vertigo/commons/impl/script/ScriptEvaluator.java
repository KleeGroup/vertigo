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
package io.vertigo.commons.impl.script;

import java.util.List;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.lang.Assertion;

/**
 * Evaluation d'un script
 *
 * @author  pchretien
 */
final class ScriptEvaluator {
	private final SeparatorType separatorType;
	private final List<ExpressionParameter> parameters;
	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	/**
	 * Constructor.
	 * @param separatorType SeparatorType.CLASSIC ou SeparatorType.XML ou SeparatorType.XML_CODE
	 */
	ScriptEvaluator(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin, final SeparatorType separatorType, final List<ExpressionParameter> parameters) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		Assertion.checkNotNull(parameters);
		Assertion.checkNotNull(parameters);
		//-----
		this.separatorType = separatorType;
		this.parameters = parameters;
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
	}

	/**
	 * Evaluation du script.
	 * @param script Script à évaluer
	 * @return Script évalué
	 */
	String evaluate(final String script) {
		Assertion.checkNotNull(script);
		Assertion.checkNotNull(parameters);
		//-----
		final ScriptParserHandlerImpl scriptHandler = new ScriptParserHandlerImpl(expressionEvaluatorPlugin, script, parameters);

		final ScriptParser scriptParser = new ScriptParser(separatorType.getSeparator());
		scriptParser.parse(script, scriptHandler);
		return scriptHandler.eval();
	}

}
