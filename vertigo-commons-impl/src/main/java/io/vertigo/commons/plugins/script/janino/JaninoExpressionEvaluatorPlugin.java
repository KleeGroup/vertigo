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
package io.vertigo.commons.plugins.script.janino;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.janino.ScriptEvaluator;

import io.vertigo.commons.impl.script.ExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Evaluate an expression using Janino.
 *
 * @author  pchretien
 */
public final class JaninoExpressionEvaluatorPlugin implements ExpressionEvaluatorPlugin {
	/** {@inheritDoc} */
	@Override
	public <J> J evaluate(final String expression, final List<ExpressionParameter> parameters, final Class<J> type) {
		Assertion.checkNotNull(expression);
		Assertion.checkNotNull(parameters);
		Assertion.checkNotNull(type);
		//-----
		//0. Init Janino parameters defined by their names, types and values.
		final int size = parameters.size();

		final String[] parameterNames = new String[size];
		final Class<?>[] parameterTypes = new Class[size];
		final Object[] parameterValues = new Object[size];

		int i = 0;
		for (final ExpressionParameter parameter : parameters) {
			parameterNames[i] = parameter.getName();
			parameterTypes[i] = parameter.getType();
			parameterValues[i] = parameter.getValue();
			i++;
		}

		//1. Build the scriptEvaluator
		final IScriptEvaluator scriptEvaluator = buildEvaluator(expression, type, parameterNames, parameterTypes);

		//2.Evaluate the script
		return type.cast(doEvaluate(scriptEvaluator, parameterValues));
	}

	private static Object doEvaluate(final IScriptEvaluator scriptEvaluator, final Object[] parameterValues) {
		try {
			return scriptEvaluator.evaluate(parameterValues);
		} catch (final InvocationTargetException e) {
			//Unpacking the exception
			throw WrappedException.wrap(e.getCause(), "An error occurred during expression's evaluation");
		}
	}

	private static IScriptEvaluator buildEvaluator(final String expression, final Class<?> type, final String[] parameterNames, final Class<?>[] parameterTypes) {
		try {
			return new ScriptEvaluator(expression, type, parameterNames, parameterTypes);
		} catch (final Exception ex) {
			throw WrappedException.wrap(ex, "An error occurred during expression's preprocessing  in \n {0} \n", expression);
		}
	}
}
