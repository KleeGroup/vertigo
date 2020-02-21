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

import java.util.NoSuchElementException;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;

/**
 * Model used to define a DtField.
 *
 * @author pchretien
 */
public final class DtFieldModel {
	private final DtDefinition dtDefinition;
	private final DtField dtField;

	/***
	 * Constructeur.
	 * @param dtField Champ à générer
	 */
	DtFieldModel(final DtDefinition dtDefinition, final DtField dtField) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtField);
		//-----
		this.dtDefinition = dtDefinition;
		this.dtField = dtField;
	}

	/**
	 * Nom du champ en camelCase.
	 * En freemarker pour upperCamelCase : myString?cap_first
	 * @return unNom
	 */
	public String getName() {
		return dtField.getName();
	}

	/**
	 * @return DtField
	 */
	public DtField getSource() {
		return dtField;
	}

	public String getResourceKey() {
		return dtField.getResourceKey();
	}

	public String getLabel() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * @return Type du champ (
	 */
	public String getType() {
		return dtField.getType().name();
	}

	/**
	 * @return Type java du champ
	 */
	public String getJavaType() {
		return DomainUtil.buildJavaType(dtField.getDomain());
	}

	/**
	 * @return Type java du champ
	 */
	public String getJavaTypeLabel() {
		return DomainUtil.buildJavaTypeLabel(dtField.getDomain());
	}

	/**
	 * @return Label du champ
	 */
	public String getDisplay() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * @return Si la propriété est requise
	 */
	public boolean isRequired() {
		return dtField.isRequired();
	}

	/**
	 * @return Code java correspondant à l'expression de ce champ calculé
	 */
	public String getJavaCode() {
		return dtField.getComputedExpression().getJavaCode();
	}

	private boolean isChildOfEntity() {
		return !dtDefinition.getFragment().isPresent();
	}

	public boolean isForeignKey() {
		return isChildOfEntity() && dtField.getType() == FieldType.FOREIGN_KEY;
	}

	public AssociationModel getAssociation() {
		Assertion.checkState(isChildOfEntity(), "an association must be declared on an entity");
		//---
		return DomainUtil.getSimpleAssociations()
				.stream()
				.filter(association -> association.getFKField().equals(dtField))
				.map(association -> new AssociationModel(association, association.getPrimaryAssociationNode()))
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
	}

}
