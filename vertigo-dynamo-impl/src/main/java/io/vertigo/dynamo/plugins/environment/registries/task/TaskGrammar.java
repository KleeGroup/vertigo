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
package io.vertigo.dynamo.plugins.environment.registries.task;

import static io.vertigo.dynamo.plugins.environment.KspProperty.CLASS_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.DATA_SPACE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.IN_OUT;
import static io.vertigo.dynamo.plugins.environment.KspProperty.REQUIRED;
import static io.vertigo.dynamo.plugins.environment.KspProperty.REQUEST;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.Boolean;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.String;

import java.util.List;

import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
final class TaskGrammar implements DslGrammar {
	/** Attribute name. */
	public static final String TASK_ATTRIBUTE = "attribute";

	/**Définition de tache.*/
	public static final DslEntity TASK_DEFINITION_ENTITY;

	static {
		final DslEntity taskAttributeDefinitionEntity = DslEntity.builder("Attribute")
				.addRequiredField(REQUIRED, Boolean)
				.addRequiredField(IN_OUT, String)
				.addRequiredField("domain", DomainGrammar.DOMAIN_ENTITY.getLink())
				.build();

		TASK_DEFINITION_ENTITY = DslEntity.builder("Task")
				.addRequiredField(REQUEST, String)
				.addOptionalField(DATA_SPACE, String)
				.addRequiredField(CLASS_NAME, String)
				.addManyFields(TASK_ATTRIBUTE, taskAttributeDefinitionEntity)
				.build();
	}

	@Override
	public List<DslEntity> getEntities() {
		return new ListBuilder<DslEntity>()
				.add(TASK_DEFINITION_ENTITY)
				.unmodifiable()
				.build();
	}
}
