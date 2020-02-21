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
package io.vertigo.dynamo.plugins.environment.dsl.dynamic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntityField;
import io.vertigo.lang.Assertion;

/**
 * Classe permettant de créer dynamiquement une structure grammaticale.
 * Cette Classe est utilisée pour parcourir dynamiquement les modèles.
 * Rappelons qu'une structure est elle-même composée de sous structure grammaticales.
 *
 * @author  pchretien
 */
public final class DslDefinition {
	/** Type. */
	private final DslEntity entity;

	/** Name of the package. */
	private final String packageName;

	/**name of this definition.*/
	private final String name;

	/** Map  (fieldName, propertyValue)  */
	private final Map<DslEntityField, Object> propertyValueByFieldName;

	/**
	 * Links.
	 * Map (fieldName, definitions identified by its name)
	 */
	private final Map<DslEntityField, List<String>> definitionLinkNamesByFieldName;

	/**
	 * Children.
	 * Map (fieldName, definitions
	 */
	private final Map<DslEntityField, List<DslDefinition>> childDefinitionsByFieldName;

	DslDefinition(
			final DslEntity entity,
			final String packageName,
			final String name,
			final Map<DslEntityField, Object> propertyValueByFieldName,
			final Map<DslEntityField, List<String>> definitionLinkNamesByFieldName,
			final Map<DslEntityField, List<DslDefinition>> childDefinitionsByFieldName) {
		Assertion.checkNotNull(entity);
		//packageName can be null
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(propertyValueByFieldName);
		Assertion.checkNotNull(definitionLinkNamesByFieldName);
		Assertion.checkNotNull(childDefinitionsByFieldName);
		//---
		this.entity = entity;
		this.packageName = packageName;
		this.name = name;
		this.propertyValueByFieldName = propertyValueByFieldName;
		this.definitionLinkNamesByFieldName = definitionLinkNamesByFieldName;
		this.childDefinitionsByFieldName = childDefinitionsByFieldName;
	}

	/**
	 * Static method factory for TaskBuilder
	 * @param name the name of the dslDefinition
	 * @param entity Entité
	 * @return TaskBuilder
	 */
	public static DslDefinitionBuilder builder(final String name, final DslEntity entity) {
		return new DslDefinitionBuilder(name, entity);
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Entité
	 */
	public DslEntity getEntity() {
		return entity;
	}

	/**
	 * @return Nom de la Définition
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retourne la valeur d'une (méta) propriété liée au domaine, champ, dtDéfinition...
	 * null si cette propriété n'existe pas
	 * @param fieldName Name of the field
	 * @return valeur de la propriété
	 */
	public Object getPropertyValue(final String fieldName) {
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isProperty(), "expected a property on {0}", fieldName);
		// On ne vérifie rien sur le type retourné par le getter.
		// le type a été validé lors du put.
		//-----
		// Conformémément au contrat, on retourne null si pas de propriété trouvée
		return propertyValueByFieldName.get(dslEntityField);
	}

	/**
	 * Set des propriétés gérées.
	 * @return Collection
	 */
	public Set<String> getPropertyNames() {
		return propertyValueByFieldName.keySet()
				.stream()
				.map(DslEntityField::getName)
				.collect(Collectors.toSet());
	}

	/**
	 * Permet de récupérer la liste des définitions d'un champ.
	 *
	 * @param fieldName Nom du champ.
	 * @return List
	 */
	public List<String> getDefinitionLinkNames(final String fieldName) {
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isEntityLink(), "expected a link on {0}", fieldName);
		//---
		return definitionLinkNamesByFieldName.get(dslEntityField);
	}

	/**
	 * Uniquement si il y a une et une seule référence pour ce champ.
	 * @param fieldName Nom du champ.
	 * @return Clé de la définition
	 */
	public String getDefinitionLinkName(final String fieldName) {
		final List<String> list = getDefinitionLinkNames(fieldName);
		final String definitionName = list.get(0);
		//-----
		// On vérifie qu'il y a une définition pour le champ demandé
		Assertion.checkNotNull(definitionName);
		return definitionName;
	}

	/**
	 * Permet de récupérer la collection de tous les champs qui pointent vers des définitions utilisées par référence.
	 * @return Collection de tous les champs utilisant des définitions référencées.
	 */
	public Set<DslEntityField> getAllDefinitionLinkFields() {
		return definitionLinkNamesByFieldName.keySet();
	}

	/**
	 * Récupération de la liste des definitions dont est composée la définition principale.
	 * @param fieldName String
	 * @return List
	 */
	public List<DslDefinition> getChildDefinitions(final String fieldName) {
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isEntity(), "expected an entity on {0}", fieldName);
		//---
		return childDefinitionsByFieldName.get(dslEntityField);
	}

	/**
	 * @return Collection des listes de définitions composites.
	 */
	public List<DslDefinition> getAllChildDefinitions() {
		return childDefinitionsByFieldName.values()
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}
}
