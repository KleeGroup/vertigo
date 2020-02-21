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
package io.vertigo.studio.plugins.mda.task.model;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author pchretien
 */
public final class TaskAttributeModel {
	private final TaskAttribute taskAttribute;

	TaskAttributeModel(final TaskAttribute taskAttribute) {
		Assertion.checkNotNull(taskAttribute);
		//-----
		this.taskAttribute = taskAttribute;
	}

	/**
	 * @return Nom de l'attribut.
	 */
	public String getName() {
		return taskAttribute.getName();
	}

	/**
	 * @return Nom de la variable
	 */
	public String getVariableName() {
		return taskAttribute.getName();
	}

	/**
	 * @return Type de la donnée en string
	 */
	public String getDataType() {
		return String.valueOf(DomainUtil.buildJavaType(taskAttribute.getDomain()));
	}

	/**
	 * @return Type java du champ
	 */
	public String getJavaTypeLabel() {
		return DomainUtil.buildJavaTypeLabel(taskAttribute.getDomain());
	}

	/**
	 * @return Si l'attribut est obligatoire.
	 */
	public boolean isRequired() {
		return taskAttribute.isRequired();
	}

	/**
	 * @return Domain.
	 */
	public Domain getDomain() {
		return taskAttribute.getDomain();
	}
}
