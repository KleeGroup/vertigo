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
package io.vertigo.dynamo.domain.util;

import java.util.stream.Collectors;

import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.Fragment;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * The DtObjectUtil class is a set of utils about the DtObject.
 *
 * @author pchretien
 */
public final class DtObjectUtil {
	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);

	private DtObjectUtil() {
		//private constructor.
	}

	/**
	 * Creates a new instance of 'DtObject' from a 'DtDefinition'.
	 *
	 * @param dtDefinition the definition to use for creation
	 * @return the new instance
	 */
	public static DtObject createDtObject(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		//La création des DtObject n'est pas sécurisée
		return ClassUtil.newInstance(dtDefinition.getClassCanonicalName(), DtObject.class);
	}

	/**
	 * Creates a new entity from a 'DtDefinition'.
	 *
	 * @param dtDefinition the definition to use for creation
	 * @return the new instance
	 */
	public static Entity createEntity(final DtDefinition dtDefinition) {
		return Entity.class.cast(createDtObject(dtDefinition));
	}

	/**
	 * Returns the 'id' of a 'DtObject'.
	 * @param entity the entity
	 * @return the id of the specified 'DtObject'
	 */
	public static Object getId(final Entity entity) {
		Assertion.checkNotNull(entity);
		//-----
		final DtDefinition dtDefinition = findDtDefinition(entity);
		final DtField idField = dtDefinition.getIdField().get();
		return idField.getDataAccessor().getValue(entity);
	}

	/**
	 * Récupération d'une UID de DTO.
	 * On récupère l'URI d'un DTO référencé par une association.
	 * Il est nécessaire que l'association soit simple.
	 * Si l'association est multiple on ne récupère pas une UID mais une DtListURI, c'est à dire le pointeur vers une liste.
	 *
	 *  On recherche une UID correspondant à une association.
	 *  Exemple : Une Commande possède un bénéficiaire.
	 *  Dans cetexemple on recherche l'UID du bénéficiaire à partir de l'objet commande.
	 * @param <E>
	
	 * @param associationDefinitionName Nom de la définition d'une association
	 * @param dto  Object
	 * @param dtoTargetClass Class of entity of this association
	 * @return dto du DTO relié via l'association au dto passé en paramètre (Nullable)
	 */
	public static <E extends Entity> UID<E> createUID(final DtObject dto, final String associationDefinitionName, final Class<E> dtoTargetClass) {
		Assertion.checkNotNull(associationDefinitionName);
		Assertion.checkNotNull(dto);
		Assertion.checkNotNull(dtoTargetClass);
		//-----
		final AssociationSimpleDefinition associationSimpleDefinition = Home.getApp().getDefinitionSpace().resolve(associationDefinitionName, AssociationSimpleDefinition.class);
		// 1. On recherche le nom du champ portant l'objet référencé (Exemple : personne)
		final DtDefinition dtDefinition = associationSimpleDefinition.getPrimaryAssociationNode().getDtDefinition();

		// 2. On calcule le nom de la fk.
		final DtField fkField = associationSimpleDefinition.getFKField();

		// 3. On calcule l'URI de la clé étrangère
		final Object id = fkField.getDataAccessor().getValue(dto);
		if (id == null) {
			return null;
		}
		return UID.of(dtDefinition, id);
	}

	/**
	 * Récupération d'une UID de Collection à partir d'un dto
	 * @param entity the entity
	 * @param associationDefinitionName Nom de l'association
	 * @param roleName Nom du role
	 * @return UID de la collection référencée.
	 */
	public static DtListURIForSimpleAssociation createDtListURIForSimpleAssociation(final Entity entity, final String associationDefinitionName, final String roleName) {
		Assertion.checkNotNull(associationDefinitionName);
		Assertion.checkNotNull(roleName);
		Assertion.checkNotNull(entity);
		//-----
		final AssociationSimpleDefinition associationDefinition = Home.getApp().getDefinitionSpace().resolve(associationDefinitionName, AssociationSimpleDefinition.class);
		return new DtListURIForSimpleAssociation(associationDefinition, entity.getUID(), roleName);
	}

	/**
	 * Récupération d'une UID de Collection à partir d'un dto
	 * @param entity the entity
	 * @param associationDefinitionName Nom de l'association
	 * @param roleName Nom du role
	 * @return UID de la collection référencée.
	 */
	public static DtListURIForNNAssociation createDtListURIForNNAssociation(final Entity entity, final String associationDefinitionName, final String roleName) {
		Assertion.checkNotNull(associationDefinitionName);
		Assertion.checkNotNull(roleName);
		Assertion.checkNotNull(entity);
		//-----
		final AssociationNNDefinition associationDefinition = Home.getApp().getDefinitionSpace().resolve(associationDefinitionName, AssociationNNDefinition.class);
		return new DtListURIForNNAssociation(associationDefinition, entity.getUID(), roleName);
	}

	/**
	 * Creates an UID of entity from an existing fragment.
	 * @param fragment fragment
	 * @return related entity UID
	 */
	public static <E extends Entity, F extends Fragment<E>> UID<E> createEntityUID(final F fragment) {
		Assertion.checkNotNull(fragment);
		//-----
		final DtDefinition dtDefinition = findDtDefinition(fragment);
		final DtDefinition entityDtDefinition = dtDefinition.getFragment().get();
		final DtField idField = entityDtDefinition.getIdField().get();
		final Object idValue = idField.getDataAccessor().getValue(fragment);
		return UID.of(entityDtDefinition, idValue);
	}

	/**
	 * Représentation sous forme text d'un dtObject.
	 * @param dto dtObject
	 * @return Représentation sous forme text du dtObject.
	 */
	public static String toString(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		return findDtDefinition(dto).getFields()
				.stream()
				.filter(dtField -> dtField.getType() != DtField.FieldType.COMPUTED)
				.map(dtField -> dtField.getName() + '=' + dtField.getDataAccessor().getValue(dto))
				.collect(Collectors.joining(", ", findDtDefinition(dto).getName() + '(', ")"));
	}

	/**
	 * Finds the definition to which the specified 'DtObject' is mapped.
	 * @param dto DtObject
	 * @return the id
	 */
	public static DtDefinition findDtDefinition(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		return findDtDefinition(dto.getClass());
	}

	/**
	 * Finds the definition from a type of 'DtObject'
	 * @param dtObjectClass  the type of the 'DtObject'
	 * @return the id
	 */
	public static DtDefinition findDtDefinition(final Class<? extends DtObject> dtObjectClass) {
		Assertion.checkNotNull(dtObjectClass);
		//-----
		final String name = DT_DEFINITION_PREFIX + dtObjectClass.getSimpleName();
		return Home.getApp().getDefinitionSpace().resolve(name, DtDefinition.class);
	}
}
