/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.environment.registries.task;

import io.vertigo.core.Home;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.ClassUtil;

/**
 * @author pchretien
 */
public final class TaskDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {
	/**
	 * Constructeur.
	 */
	public TaskDynamicRegistryPlugin() {
		super(TaskGrammar.GRAMMAR);
	}

	/** {@inheritDoc} */
	@Override
	public Option<Definition> createDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdefinition) {
		if (TaskGrammar.TASK_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			//Seuls les taches sont gérées.
			final Definition definition = createTaskDefinition(xdefinition);
			return Option.some(definition);
		}
		return Option.none();
	}

	private static Class<? extends TaskEngine> getTaskEngineClass(final DynamicDefinition xtaskDefinition) {
		final String taskEngineClassName = getPropertyValueAsString(xtaskDefinition, KspProperty.CLASS_NAME);
		return ClassUtil.classForName(taskEngineClassName, TaskEngine.class);
	}

	private static TaskDefinition createTaskDefinition(final DynamicDefinition xtaskDefinition) {
		final String taskDefinitionName = xtaskDefinition.getName();
		final String request = getPropertyValueAsString(xtaskDefinition, KspProperty.REQUEST);
		Assertion.checkNotNull(taskDefinitionName);
		final Class<? extends TaskEngine> taskEngineClass = getTaskEngineClass(xtaskDefinition);
		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(taskEngineClass)
				.withRequest(request)
				.withPackageName(xtaskDefinition.getPackageName());
		for (final DynamicDefinition xtaskAttribute : xtaskDefinition.getChildDefinitions(TaskGrammar.TASK_ATTRIBUTE)) {
			final String attributeName = xtaskAttribute.getName();
			Assertion.checkNotNull(attributeName);
			final String domainName = xtaskAttribute.getDefinitionName("domain");
			final Domain domain = Home.getDefinitionSpace().resolve(domainName, Domain.class);
			//-----
			final Boolean notNull = getPropertyValueAsBoolean(xtaskAttribute, KspProperty.NOT_NULL);
			if (isInValue(getPropertyValueAsString(xtaskAttribute, KspProperty.IN_OUT))) {
				taskDefinitionBuilder.addInAttribute(attributeName, domain, notNull.booleanValue());
			} else {
				taskDefinitionBuilder.withOutAttribute(attributeName, domain, notNull.booleanValue());
			}
		}
		return taskDefinitionBuilder.build();
	}

	private static boolean isInValue(final String sText) {
		if ("in".equals(sText)) {
			return true;
		} else if ("out".equals(sText)) {
			return false;
		}
		throw new IllegalArgumentException("les seuls types autorises sont 'in' ou 'out' et non > " + sText);
	}
}
