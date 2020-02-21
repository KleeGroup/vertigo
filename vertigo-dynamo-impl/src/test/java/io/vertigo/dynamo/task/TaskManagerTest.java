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
import static io.vertigo.dynamo.task.TaskEngineMock2.ATTR_IN_INTEGERS;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;

/**
 * This class tests the usage of a task from its registration to its execution.
 * @author dchallas
 */
public final class TaskManagerTest extends AbstractTestCaseJU5 {

	@Inject
	private TaskManager taskManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DynamoFeatures()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/task/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.task.data.DtDefinitions")
								.build())
						.addDefinitionProvider(TaskDefinitionProvider.class)
						.build())
				.build();
	}

	private TaskDefinition getTaskDefinition(final String taskDefinitionName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(taskDefinitionName, TaskDefinition.class);
	}

	/**
	 * Checks if the task-definition is registered.
	 */
	@Test
	public void testRegistry() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);
		Assertions.assertNotNull(taskDefinition);
	}

	/**
	 * Checks when the task-definition is not registered (an exception must be thrown).
	 */
	@Test
	public void testRegistryWithNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
			//L'appel à la résolution doit remonter une assertion
			final TaskDefinition taskDefinition = definitionSpace.resolve(null, TaskDefinition.class);
			nop(taskDefinition);
		});
	}

	/***
	 * Checks the use case of an addition  with several inputs and an output
	 */
	@Test
	public void testExecuteAdd() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);
		Assertions.assertEquals(Integer.valueOf(10), executeTask(taskDefinition, 5, 2, 3));
	}

	/***
	 * Checks the use case of an multiplication with several inputs and an output
	 */
	@Test
	public void testExecuteMulti() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION);
		Assertions.assertEquals(Integer.valueOf(30), executeTask(taskDefinition, 5, 2, 3));
	}

	/**
	 * Checks that an exception is thrown
	 * when a null is given to a required task
	 */
	@Test
	public void testExecuteNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION);
			//on vérifie que le passage d'un paramètre null déclenche une assertion
			executeTask(taskDefinition, null, 2, 3);
		});
	}

	/**
	 * Checks that an exception is thrown
	 * when a task is executed twice
	 */
	@Test
	public void testExecuteAddAdd() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);

		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, 1)
				.addValue(ATTR_IN_INT_2, 8)
				.addValue(ATTR_IN_INT_3, 7)
				.build();

		final Integer result1 = taskManager
				.execute(task)
				.getResult();

		Assertions.assertEquals(Integer.valueOf(16), result1);

		final Integer result2 = taskManager
				.execute(task)
				.getResult();

		Assertions.assertEquals(Integer.valueOf(16), result2);
	}

	/**
	 * @param value1 value 1
	 * @param value2 value 2
	 * @param value3 value 3
	 * @return the addition of all these values.
	 */
	private Integer executeTask(final TaskDefinition taskDefinition, final Integer value1, final Integer value2, final Integer value3) {
		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, value1)
				.addValue(ATTR_IN_INT_2, value2)
				.addValue(ATTR_IN_INT_3, value3)
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/***
	 * Checks the use case of an addition  with one input and an output
	 * the input is composed with a List.
	 */
	@Test
	public void testExecuteAdd2() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION_2);
		Assertions.assertEquals(Integer.valueOf(10), executeTask2(taskDefinition, 5, 2, 3));
	}

	/***
	 * Checks the use case of an multiplication with one input and an output
	 * the input is composed with a List.
	 */
	@Test
	public void testExecuteMulti2() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION_2);
		Assertions.assertEquals(Integer.valueOf(30), executeTask2(taskDefinition, 5, 2, 3));
	}

	private Integer executeTask2(final TaskDefinition taskDefinition, final Integer... values) {
		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INTEGERS, Arrays.asList(values))
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

}
