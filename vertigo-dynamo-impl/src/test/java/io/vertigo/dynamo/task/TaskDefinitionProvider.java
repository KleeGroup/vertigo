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

import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_1;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_2;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_3;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_OUT;
import static io.vertigo.dynamo.task.TaskEngineMock2.ATTR_IN_INTEGERS;

import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.util.ListBuilder;

public final class TaskDefinitionProvider implements SimpleDefinitionProvider {
	public static String TK_MULTIPLICATION = "TkMultiplication";
	public static String TK_ADDITION = "TkAddition";

	public static String TK_MULTIPLICATION_2 = "TkMultiplication2";
	public static String TK_ADDITION_2 = "TkAddition2";

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return new ListBuilder<Definition>()
				.add(buildTaskDefinition(definitionSpace, TK_MULTIPLICATION, "*"))
				.add(buildTaskDefinition(definitionSpace, TK_ADDITION, "+"))
				.add(buildTaskDefinition2(definitionSpace, TK_MULTIPLICATION_2, "*"))
				.add(buildTaskDefinition2(definitionSpace, TK_ADDITION_2, "+"))
				.build();

	}

	private TaskDefinition buildTaskDefinition(
			final DefinitionSpace definitionSpace,
			final String taskDefinitionName,
			final String params) {
		final Domain doInteger = definitionSpace.resolve("DoInteger", Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineMock.class)
				.withRequest(params)
				.withPackageName(TaskEngineMock.class.getPackage().getName())
				.addInRequired(ATTR_IN_INT_1, doInteger)
				.addInRequired(ATTR_IN_INT_2, doInteger)
				.addInRequired(ATTR_IN_INT_3, doInteger)
				.withOutRequired(ATTR_OUT, doInteger)
				.build();
	}

	private TaskDefinition buildTaskDefinition2(
			final DefinitionSpace definitionSpace,
			final String taskDefinitionName,
			final String params) {
		final Domain doIntegers = definitionSpace.resolve("DoIntegers", Domain.class);
		final Domain doInteger = definitionSpace.resolve("DoInteger", Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineMock2.class)
				.withRequest(params)
				.withPackageName(TaskEngineMock.class.getPackage().getName())
				.addInRequired(ATTR_IN_INTEGERS, doIntegers)
				.withOutRequired(ATTR_OUT, doInteger)
				.build();
	}
}
