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
package io.vertigo.dynamo.task;

import java.util.List;

import io.vertigo.dynamo.task.model.TaskEngine;

/**
 * This class defines the engine with a single input and an output.
 * The input is composed of a list of integers
 *
 * This class is a parameterized function : List<Integer> -> Integer
 * The parameter is defined by the "request" and represents the operation to execute.
 *
 * @author pchretien
 */
public final class TaskEngineMock2 extends TaskEngine {
	/** list<Integer>. */
	public static final String ATTR_IN_INTEGERS = "attrInIntegers";

	private List<Integer> getValues() {
		return getValue(ATTR_IN_INTEGERS);
	}

	private void setOutput(final Integer result) {
		setResult(result);
	}

	/** {@inheritDoc} */
	@Override
	public void execute() {
		int output;
		switch (getTaskDefinition().getRequest()) {
			case "+":
				output = 0;
				for (final int value : getValues()) {
					output += value;
				}
				break;
			case "*":
				output = 1;
				for (final int value : getValues()) {
					output *= value;
				}
				break;
			default:
				throw new IllegalArgumentException("Operateur non reconnu.");
		}
		setOutput(output);
	}
}
