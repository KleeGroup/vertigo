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

import javax.inject.Inject;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.lang.Assertion;

/**
 * This manager allows you to script a text.
 * Some expressions can be used inside the text.
 * These expressions will be parsed and evaluated.
 *
 * @author pchretien
 */
public final class ScriptManagerImpl implements ScriptManager {
	/**
	 * the plugin used to evaluate an expression.
	 */
	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	/**
	 * Constructor.
	 * @param expressionEvaluatorPlugin the plugin used to evaluate an expression
	 */
	@Inject
	public ScriptManagerImpl(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		//-----
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void parse(final String script, final ScriptParserHandler scriptHandler, final ScriptSeparator separator) {
		new ScriptParser(separator).parse(script, scriptHandler);
	}

	/** {@inheritDoc} */
	@Override
	public String evaluateScript(final String script, final SeparatorType separatorType, final List<ExpressionParameter> parameters) {
		return new ScriptEvaluator(expressionEvaluatorPlugin, separatorType, parameters).evaluate(script);
	}

	/** {@inheritDoc} */
	@Override
	public <J> J evaluateExpression(final String expression, final List<ExpressionParameter> parameters, final Class<J> type) {
		return expressionEvaluatorPlugin.evaluate("return " + expression + ";", parameters, type);
	}

}
