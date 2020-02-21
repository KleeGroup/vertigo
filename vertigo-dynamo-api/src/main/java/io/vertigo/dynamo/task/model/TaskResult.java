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
package io.vertigo.dynamo.task.model;

import java.util.Optional;

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;

/**
 * Résultat de l'exécution d'une tache.
 * @author dchallas
 */
public final class TaskResult {
	/**
	 * Définition de la tache.
	 */
	private final Optional<TaskAttribute> outTaskAttributeOptional;

	private final Object result;

	/**
	 * Constructeur.
	 * Le constructeur est protégé, il est nécessaire de passer par le Builder.
	 */
	TaskResult(final TaskDefinition taskDefinition, final Object result) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		outTaskAttributeOptional = taskDefinition.getOutAttributeOption();
		outTaskAttributeOptional.ifPresent(outTaskAttribute -> outTaskAttribute.checkAttribute(result));

		this.result = result;
	}

	/**
	 * Getter générique.
	 * Retourne la valeur d'un paramètre conforme au contrat de l'attribut du service
	 *
	 * @param <V> Type de la valeur
	 * @return Result
	 */
	public <V> V getResult() {
		Assertion.checkArgument(outTaskAttributeOptional.isPresent(), "this task does not provide any result");
		return (V) result;
	}
}
