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
package io.vertigo.dynamo.task.metamodel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.lang.Assertion;

/**
 * This class defines a task and its attributes.
 *
 * @author  fconstantin, pchretien
 */
@DefinitionPrefix("Tk")
public final class TaskDefinition implements Definition {
	/** the name of the definition. */
	private final String name;

	/** the package name. */
	private final String packageName;

	/** the dataSpace. */
	private final String dataSpace;

	/** Chaine de configuration du service. */
	private final String request;

	/** Map des (Nom, TaskAttribute) définissant les attributs de tache. */
	private final Map<String, TaskAttribute> inTaskAttributes;

	private final Optional<TaskAttribute> outTaskAttributeOption;

	/**
	 * Moyen de réaliser la tache.
	 */
	private final Class<? extends TaskEngine> taskEngineClass;

	/**
	 * Constructor.
	 * @param taskEngineClass Classe réalisant l'implémentation
	 * @param request Chaine de configuration
	 */
	TaskDefinition(
			final String name,
			final String packageName,
			final String dataSpace,
			final Class<? extends TaskEngine> taskEngineClass,
			final String request,
			final List<TaskAttribute> inTaskAttributes,
			final Optional<TaskAttribute> outTaskAttributeOption) {
		DefinitionUtil.checkName(name, TaskDefinition.class);
		Assertion.checkArgNotEmpty(dataSpace);
		Assertion.checkState(DtDefinition.REGEX_DATA_SPACE.matcher(dataSpace).matches(), "collection {0} must match pattern {1}", dataSpace, DtDefinition.REGEX_DATA_SPACE);
		Assertion.checkNotNull(taskEngineClass, "a taskEngineClass is required");
		Assertion.checkNotNull(request, "a request is required");
		Assertion.checkNotNull(inTaskAttributes);
		Assertion.checkNotNull(outTaskAttributeOption);
		//-----
		this.name = name;
		this.packageName = packageName;
		this.dataSpace = dataSpace;
		this.request = request;
		this.inTaskAttributes = createMap(inTaskAttributes);
		this.outTaskAttributeOption = outTaskAttributeOption;
		this.taskEngineClass = taskEngineClass;
	}

	/**
	 * Static method factory for TaskDefinition
	 * @param taskDefinitionName the name of the taskDefinition (TK_XXX_YYY)
	 * @return TaskDefinition
	 */
	public static TaskDefinitionBuilder builder(final String taskDefinitionName) {
		return new TaskDefinitionBuilder(taskDefinitionName);
	}

	/**
	 * Création  d'une Map non modifiable.
	 * @param taskAttributes Attributs de la tache
	 */
	private static Map<String, TaskAttribute> createMap(final List<TaskAttribute> taskAttributes) {
		final Map<String, TaskAttribute> map = new LinkedHashMap<>();
		for (final TaskAttribute taskAttribute : taskAttributes) {
			Assertion.checkNotNull(taskAttribute);
			Assertion.checkArgument(!map.containsKey(taskAttribute.getName()), "attribut {0} existe déjà", taskAttribute.getName());
			//-----
			map.put(taskAttribute.getName(), taskAttribute);
		}
		return java.util.Collections.unmodifiableMap(map);
	}

	/**
	 * Retourne l'attribut de la tache identifié par son nom.
	 *
	 * @param attributeName Nom de l'attribut recherché.
	 * @return Définition de l'attribut.
	 */
	public TaskAttribute getInAttribute(final String attributeName) {
		Assertion.checkNotNull(attributeName);
		//-----
		final TaskAttribute taskAttribute = inTaskAttributes.get(attributeName);
		Assertion.checkNotNull(taskAttribute, "nom d''attribut :{0} non trouvé pour le service :{1}", attributeName, this);
		return taskAttribute;
	}

	/**
	 * Retourne la classe réalisant l'implémentation de la tache.
	 *
	 * @return Classe réalisant l'implémentation
	 */
	public Class<? extends TaskEngine> getTaskEngineClass() {
		return taskEngineClass;
	}

	/**
	 * Returns the dataSpace to which the taskDefinition belongs.
	 *
	 * @return the dataSpace.
	 */
	public String getDataSpace() {
		return dataSpace;
	}

	/**
	 * Retourne la String de configuration de la tache.
	 * Cette méthode est utilisée par le TaskEngine.
	 *
	 * @return Configuration de la tache.
	 */
	public String getRequest() {
		return request;
	}

	/**
	 * Retourne l' attribut OUT
	 *
	 * @return Attribut OUT
	 */
	public Optional<TaskAttribute> getOutAttributeOption() {
		return outTaskAttributeOption;
	}

	/**
	 * Retourne la liste des attributs IN
	 *
	 * @return Liste des attributs IN
	 */
	public Collection<TaskAttribute> getInAttributes() {
		return inTaskAttributes.values();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
