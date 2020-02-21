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
package io.vertigo.dynamox.metric.task;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Classe de bouchon pour mettre des données fictives dans les requêtes.
 *
 * @author tchassagnette
 */
final class TaskPopulator {
	private final TaskDefinition taskDefinition;
	private final TaskBuilder taskBuilder;

	/**
	 * Constructeur.
	 * @param taskDefinition Definition de la tache
	 */
	public TaskPopulator(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		this.taskDefinition = taskDefinition;
		taskBuilder = Task.builder(taskDefinition);
	}

	/**
	 * Rempli la task avec les données fictives.
	 * @return Tache préparé à l'exécution
	 */
	public Task populateTask() {
		for (final TaskAttribute attribute : taskDefinition.getInAttributes()) {
			populateTaskAttribute(attribute);
		}
		return taskBuilder.build();
	}

	private void populateTaskAttribute(final TaskAttribute attribute) {
		final String attributeName = attribute.getName();
		final Object value;
		switch (attribute.getDomain().getScope()) {
			case PRIMITIVE:
				final Object item = getDefaultPrimitiveValue(attribute);
				if (attribute.getDomain().isMultiple()) {
					final List list = new ArrayList();
					list.add(item);
					value = list;
				} else {
					value = item;
				}
				break;
			case DATA_OBJECT:
				if (attribute.getDomain().isMultiple()) {
					value = new DtList(attribute.getDomain().getDtDefinition());
				} else {
					value = DtObjectUtil.createDtObject(attribute.getDomain().getDtDefinition());
				}
				break;
			case VALUE_OBJECT:
				final Object valueObject = ClassUtil.newInstance(attribute.getDomain().getJavaClass());
				if (attribute.getDomain().isMultiple()) {
					final List list = new ArrayList();
					list.add(valueObject);
					value = list;
				} else {
					value = valueObject;
				}
				break;
			default:
				throw new IllegalStateException();
		}
		taskBuilder.addValue(attributeName, value);
	}

	private Object getDefaultPrimitiveValue(final TaskAttribute attribute) {
		Object item;
		switch (attribute.getDomain().getDataType()) {
			case Boolean:
				item = Boolean.TRUE;
				break;
			case String:
				item = "Test";
				break;
			case LocalDate:
				item = LocalDate.now();
				break;
			case Instant:
				item = Instant.now();
				break;
			case Double:
				item = Double.valueOf(1);
				break;
			case Integer:
				item = Integer.valueOf(1);
				break;
			case BigDecimal:
				item = BigDecimal.valueOf(1);
				break;
			case Long:
				item = Long.valueOf(1);
				break;
			case DataStream:
			default:
				//we do nothing
				item = null;
		}
		return item;
	}
}
