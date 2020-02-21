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
package io.vertigo.commons.script;

import java.util.List;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.core.component.Manager;

/**
 * Manages scripts.
 * A script is composed of expressions.
 * A script (and experssions) can be parameterized.
 *
 * @author pchretien
 */
public interface ScriptManager extends Manager {
	/**
	 * Parses the script with a specific handler.
	 * the grammar is defined by simple tags balises (separators).
	 *
	 * @param script the script to analyse
	 * @param scriptHandler the hander
	 * @param separator the allowed separators in the grammar.
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler, final ScriptSeparator separator);

	/**
	 * Evaluates the script, transforms a script into a text.
	 * @param script the script
	 * @param separatorType the type of separator
	 * @param parameters the parameters
	 * @return the evaluated script as a simplet text
	 */
	String evaluateScript(final String script, final SeparatorType separatorType, final List<ExpressionParameter> parameters);

	/**
	 * Evaluates an expression.
	 *  Examples of expressions written in java
	 *  - name
	 *  - birthDate
	 *  - age>20
	 *  - salary>5000 && age <30
	 *  - name + surName
	 * @param expression the expression
	 * @param parameters the parameters
	 * @param type the returned type
	 * @return the evaluated expression
	 * @Param <J> the returned type by the evaluation
	 */
	<J> J evaluateExpression(final String expression, List<ExpressionParameter> parameters, Class<J> type);
}
