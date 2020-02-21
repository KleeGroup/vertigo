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
package io.vertigo.dynamo.impl.task.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.task.proxy.TaskInput;
import io.vertigo.dynamo.task.proxy.TaskOutput;
import io.vertigo.lang.Assertion;

public final class TaskProxyMethod implements ProxyMethod {

	@Override
	public Class<io.vertigo.dynamo.task.proxy.TaskAnnotation> getAnnotationType() {
		return io.vertigo.dynamo.task.proxy.TaskAnnotation.class;
	}

	private static Domain resolveDomain(final String domainName) {
		return Home.getApp().getDefinitionSpace().resolve(domainName, Domain.class);
	}

	private static boolean hasOut(final Method method) {
		return !void.class.equals(method.getReturnType());
	}

	private static boolean isOutOptional(final Method method) {
		return Optional.class.isAssignableFrom(method.getReturnType());
	}

	private static Domain findOutDomain(final Method method) {
		final TaskOutput taskOutput = method.getAnnotation(TaskOutput.class);
		Assertion.checkNotNull(taskOutput, "The return method '{0}' must be annotated with '{1}'", method, TaskOutput.class);
		return resolveDomain(taskOutput.domain());
	}

	private static TaskManager getTaskManager() {
		return Home.getApp().getComponentSpace().resolve(TaskManager.class);
	}

	@Override
	public Object invoke(final Method method, final Object[] args) {
		final TaskDefinition taskDefinition = createTaskDefinition(method);
		final Task task = createTask(taskDefinition, method, args);
		final TaskResult taskResult = getTaskManager().execute(task);
		if (taskDefinition.getOutAttributeOption().isPresent()) {
			return taskResult.getResult();
		}
		return Void.TYPE;
	}

	private static TaskDefinition createTaskDefinition(final Method method) {
		final io.vertigo.dynamo.task.proxy.TaskAnnotation taskAnnotation = method.getAnnotation(io.vertigo.dynamo.task.proxy.TaskAnnotation.class);

		final TaskDefinitionBuilder taskDefinitionBuilder = TaskDefinition.builder(taskAnnotation.name())
				.withEngine(taskAnnotation.taskEngineClass())
				.withRequest(taskAnnotation.request())
				.withDataSpace(taskAnnotation.dataSpace().isEmpty() ? null : taskAnnotation.dataSpace());

		if (hasOut(method)) {
			final Domain outDomain = findOutDomain(method);
			if (isOutOptional(method)) {
				taskDefinitionBuilder.withOutOptional("out", outDomain);

			} else {
				taskDefinitionBuilder.withOutRequired("out", outDomain);
			}
		}
		for (final Parameter parameter : method.getParameters()) {
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);

			//test if the parameter is an optional type
			final boolean optional = Optional.class.isAssignableFrom(parameter.getType());

			if (optional) {
				taskDefinitionBuilder.addInOptional(
						taskAttributeAnnotation.name(),
						resolveDomain(taskAttributeAnnotation.domain()));
			} else {
				taskDefinitionBuilder.addInRequired(
						taskAttributeAnnotation.name(),
						resolveDomain(taskAttributeAnnotation.domain()));
			}
		}

		return taskDefinitionBuilder.build();
	}

	private static Task createTask(final TaskDefinition taskDefinition, final Method method, final Object[] args) {
		final TaskBuilder taskBuilder = Task.builder(taskDefinition);
		for (int i = 0; i < method.getParameters().length; i++) {
			final Parameter parameter = method.getParameters()[i];
			final boolean optional = Optional.class.isAssignableFrom(parameter.getType());
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);

			final Object arg;
			if (optional) {
				arg = ((Optional) args[i]).orElse(null);
			} else {
				arg = args[i];
			}
			taskBuilder.addValue(taskAttributeAnnotation.name(), arg);
		}
		return taskBuilder.build();
	}
}
