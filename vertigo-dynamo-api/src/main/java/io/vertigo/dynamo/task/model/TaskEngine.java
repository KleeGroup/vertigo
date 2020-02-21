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

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;

/**
 * Moteur précisant le mode d'exécution d'une définition de tache.
 * Attention ce moteur est avec état ; il est donc nécessaire de le recréer avant chaque utilisation.
 *
 * @author fconstantin, pchretien
 * @see io.vertigo.dynamo.task.model.Task
 */
public abstract class TaskEngine {
	private Task input;
	private Object result = Void.TYPE;

	/**
	 * Réalise l'exécution d'une tache.
	 * L'implémentation n'est pas responsable de la gestion de la transaction.
	 * Un rollback de la transaction sera automatiquement exécuté au cas où
	 * une exception survient.
	 * La tache permet d'accéder à la définition des paramètres d'entrée-sortie
	 * ainsi qu'à la chaine de configuration de la tache.
	 */
	protected abstract void execute();

	/**
	 * Exécute le travail.
	 * Le travail s'exécute dans la transaction courante si elle existe.
	 *  - Le moteur n'est pas responsable de de créer une transaction.
	 *  - En revanche si une telle transaction existe elle est utilisée.
	 * @param task Task to process
	 * @return TaskResult contenant les résultats
	 */
	public final TaskResult process(final Task task) {
		Assertion.checkNotNull(task);
		//-----
		input = task;
		execute();
		return new TaskResult(task.getDefinition(), result == Void.TYPE ? null : result);
	}

	/**
	* Getter avec un type générique.
	* Retourne la valeur d'un paramètre (INPUT)
	*
	* @param <J> Type java de l'objet recherché
	* @param attributeName Nom du paramètre
	* @return Valeur
	*/
	protected final <J> J getValue(final String attributeName) {
		return input.getValue(attributeName);
	}

	/**
	 * Setter générique
	 * Affecte la valeur d'un paramètre (OUTPUT)
	 *
	 * @param o Valeur
	 */
	protected final void setResult(final Object o) {
		Assertion.checkArgument(o != Void.TYPE, "you can't  invoke setResult with Void, use null instead");
		Assertion.checkState(result == Void.TYPE, "you can't  invoke setResult more than one time");
		//-----
		result = o;
	}

	/**
	 * Retourne la définition de la tache.
	 * taskDataSet est non visible (Framework).
	 *
	 * @return Définition de la tache
	 */
	protected final TaskDefinition getTaskDefinition() {
		return input.getDefinition();
	}

}
