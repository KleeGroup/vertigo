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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.lang.Assertion;

/**
 * Simule le mécanisme JSP sur un fichier texte quelconque.
 * Remplace les éléments compris entre les séparateurs par une évaluation dynamique.
 * <% %>  : permet d'insérer des blocs java
 * <%= %> : permet d'ajouter des éléments au texte
 *
 * @author  pchretien
 */
final class ScriptParserHandlerImpl implements ScriptParserHandler {
	private static final String TEXT_KEY_PREFIX = "ZZYY";
	private static final String TEXT_KEY_SUFFIX = "YYZZ";

	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	private final String originalScript;

	private final StringBuilder evaluatedScript = new StringBuilder();
	/**
	 * Si il y a au moins un paramètre alors la requête est dynamique.
	 */
	private boolean isDynamic;

	private final List<String> texts = new ArrayList<>();
	private final List<ExpressionParameter> parameters;

	/**
	 * Constructor.
	 * @param parameters Map des paramètres
	 */
	ScriptParserHandlerImpl(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin, final String originalScript, final List<ExpressionParameter> parameters) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		Assertion.checkArgNotEmpty(originalScript);
		Assertion.checkNotNull(parameters);
		//-----
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
		this.originalScript = originalScript;
		this.parameters = parameters;
	}

	/** {@inheritDoc} */
	@Override
	public void onText(final String text) {
		evaluatedScript.append("query.append(\"");
		appendEncodedText(evaluatedScript, text);
		evaluatedScript.append("\");");
	}

	/** {@inheritDoc} */
	@Override
	public void onExpression(final String expression, final ScriptSeparator separator) {
		isDynamic = true; //Si on entre dans cette méthode, on est forcément dynamique
		if (expression.charAt(0) == '=') {
			// cas <%=s START_ATTRIBUTE
			//On ajoute une évaluation de l'attribut
			evaluatedScript.append("query.append(String.valueOf(").append(expression.substring(1)).append("));");
		} else {
			//On ajoute le bloc java
			evaluatedScript.append(expression);
		}
	}

	String eval() {
		if (isDynamic) {
			//On positionne un try catch
			evaluatedScript.insert(0, "try { StringBuilder query = new StringBuilder();");
			//---
			evaluatedScript.append(" return query.toString();");
			evaluatedScript.append("} catch (Exception e) { throw new RuntimeException(e);}");
			//-----
			return scriptEvaluate(evaluatedScript.toString());
		}
		return originalScript;

	}

	private void appendEncodedText(final StringBuilder sb, final String text) {
		final int key = texts.size();
		texts.add(text);
		sb.append(TEXT_KEY_PREFIX).append(key).append(TEXT_KEY_SUFFIX);
	}

	private String decodeText(final String text) {
		final StringBuilder result = new StringBuilder();
		int textStart = 0;
		int textEnd;
		while ((textEnd = text.indexOf(TEXT_KEY_PREFIX, textStart)) >= 0) {
			final int keyStart = textEnd + TEXT_KEY_PREFIX.length();
			final int keyEnd = text.indexOf(TEXT_KEY_SUFFIX, keyStart);
			final String key = text.substring(keyStart, keyEnd);

			result.append(text.substring(textStart, textEnd));
			result.append(texts.get(Integer.parseInt(key)));
			textStart = keyEnd + TEXT_KEY_SUFFIX.length();
		}
		// Dernier morceau
		result.append(text.substring(textStart));

		return result.toString();
	}

	private String scriptEvaluate(final String script) {
		final String res = expressionEvaluatorPlugin.evaluate(script, parameters, String.class);
		return decodeText(res);
	}
}
