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
package io.vertigo.studio.plugins.mda.domain.java.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.model.DtMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtStaticMasterData;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.Fragment;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.lang.Assertion;

/**
 * Model used by FreeMarker.
 *
 * @author pchretien
 */
public final class DtDefinitionModel {
	private final DtDefinition dtDefinition;
	private final List<DtFieldModel> dtFieldModels = new ArrayList<>();
	private final List<DtFieldModel> dtAllFieldModels = new ArrayList<>();
	private final List<DtFieldModel> dtComputedFieldModels = new ArrayList<>();
	private final List<AssociationModel> associationModels = new ArrayList<>();

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public DtDefinitionModel(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

		for (final DtField dtField : dtDefinition.getFields()) {
			final DtFieldModel dtFieldModel = new DtFieldModel(dtDefinition, dtField);
			dtAllFieldModels.add(dtFieldModel);
			if (FieldType.COMPUTED == dtField.getType()) {
				dtComputedFieldModels.add(dtFieldModel);
			} else {
				dtFieldModels.add(dtFieldModel);
			}
		}

		addTemplateAssociationNodes(Home.getApp().getDefinitionSpace().getAll(AssociationSimpleDefinition.class));
		addTemplateAssociationNodes(Home.getApp().getDefinitionSpace().getAll(AssociationNNDefinition.class));
	}

	/**
	 * Enregistre toutes les templates d'associations où la DtDéfinition est concernée.
	 */
	private void addTemplateAssociationNodes(final Collection<? extends AssociationDefinition> associationDefinitions) {
		for (final AssociationDefinition associationDefinition : associationDefinitions) {
			if (associationDefinition.getAssociationNodeA().getDtDefinition().getName().equals(dtDefinition.getName())) {
				associationModels.add(new AssociationModel(associationDefinition, associationDefinition.getAssociationNodeB()));
			}
			if (associationDefinition.getAssociationNodeB().getDtDefinition().getName().equals(dtDefinition.getName())) {
				associationModels.add(new AssociationModel(associationDefinition, associationDefinition.getAssociationNodeA()));
			}
		}
	}

	/**
	 * @return DT définition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/**
	 * @return Nom canonique (i.e. avec le package) de la classe d'implémentation du DtObject
	 */
	public String getClassCanonicalName() {
		return dtDefinition.getClassCanonicalName();
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * Retourne le nom local de la definition (const case, sans prefix)
	 * @return Simple Nom (i.e. sans le package) de la definition du DtObject
	 */
	public String getLocalName() {
		return dtDefinition.getLocalName();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return dtDefinition.getPackageName();
	}

	public String getStereotypePackageName() {
		return getStereotypeClass().getCanonicalName();
	}

	public boolean isEntity() {
		return Entity.class.isAssignableFrom(getStereotypeClass());
	}

	public boolean isFragment() {
		return Fragment.class.isAssignableFrom(getStereotypeClass());
	}

	public String getEntityClassSimpleName() {
		return dtDefinition.getFragment().get().getClassSimpleName();
	}

	/**
	 * @return Nom simple de l'nterface associé au Sterotype de l'objet (DtObject, DtMasterData ou KeyConcept)
	 */
	public String getStereotypeInterfaceName() {
		if (dtDefinition.getStereotype() == DtStereotype.Fragment) {
			return getStereotypeClass().getSimpleName() + "<" + getEntityClassSimpleName() + ">";
		}
		return getStereotypeClass().getSimpleName();
	}

	private Class getStereotypeClass() {
		switch (dtDefinition.getStereotype()) {
			case Entity:
				return Entity.class;
			case ValueObject:
				return DtObject.class;
			case MasterData:
				return DtMasterData.class;
			case StaticMasterData:
				return DtStaticMasterData.class;
			case KeyConcept:
				return KeyConcept.class;
			case Fragment:
				return Fragment.class;
			default:
				throw new IllegalArgumentException("Stereotype " + dtDefinition.getStereotype().name() + " non géré");
		}
	}

	/**
	 * @return Liste de champs
	 */
	public List<DtFieldModel> getFields() {
		return dtFieldModels;
	}

	/**
	 * @return Liste de tous les champs
	 */
	public List<DtFieldModel> getAllFields() {
		return dtAllFieldModels;
	}

	/**
	 * @return Liste des champs calculés
	 */
	public List<DtFieldModel> getDtComputedFields() {
		return dtComputedFieldModels;
	}

	/**
	 * @return Liste des associations
	 */
	public List<AssociationModel> getAssociations() {
		return associationModels;
	}

	public boolean containsAccessor() {
		return dtDefinition.getStereotype() != DtStereotype.Fragment &&
				dtDefinition.getFields()
						.stream()
						.anyMatch(field -> field.getType() == FieldType.FOREIGN_KEY && field.getFkDtDefinition().getStereotype() != DtStereotype.StaticMasterData);

	}

	public boolean containsEnumAccessor() {
		return dtDefinition.getStereotype() != DtStereotype.Fragment &&
				dtDefinition.getFields()
						.stream()
						.anyMatch(field -> field.getType() == FieldType.FOREIGN_KEY && field.getFkDtDefinition().getStereotype() == DtStereotype.StaticMasterData);

	}

	public boolean containsListAccessor() {
		return associationModels
				.stream()
				//only multiples
				.filter(AssociationModel::isMultiple)
				//simple navigable ou nn
				.anyMatch(associationModel -> (associationModel.isSimple() && associationModel.isNavigable()) || !associationModel.isSimple());

	}
}
