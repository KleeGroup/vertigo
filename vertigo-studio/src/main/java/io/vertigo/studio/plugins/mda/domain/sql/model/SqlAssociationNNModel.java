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
package io.vertigo.studio.plugins.mda.domain.sql.model;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class SqlAssociationNNModel {
	private final AssociationNNDefinition associationDefinition;

	/**
	 * Constructeur.
	 * @param associationNode Noeud de l'association à générer
	 */
	public SqlAssociationNNModel(final AssociationNNDefinition associationDefinition) {
		Assertion.checkNotNull(associationDefinition);
		//-----
		this.associationDefinition = associationDefinition;
	}

	/**
	 * @return Association name
	 */
	public String getName() {
		return associationDefinition.getName();
	}

	/**
	 * @return Association NN table
	 */
	public String getTableName() {
		return associationDefinition.getTableName();
	}

	/**
	 * @return Association nodeA table
	 */
	public String getNodeATableName() {
		return StringUtil.camelToConstCase(associationDefinition.getAssociationNodeA().getDtDefinition().getLocalName());
	}

	/**
	 * @return Association nodeA Id column name
	 */
	public String getNodeAPKName() {
		return StringUtil.camelToConstCase(associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName());
	}

	/**
	 * @return Association nodeA Id domain
	 */
	public Domain getNodeAPKDomain() {
		return associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getDomain();
	}

	/**
	 * @return Association nodeB table
	 */
	public String getNodeBTableName() {
		return StringUtil.camelToConstCase(associationDefinition.getAssociationNodeB().getDtDefinition().getLocalName());
	}

	/**
	 * @return Association nodeB Id column name
	 */
	public String getNodeBPKName() {
		return StringUtil.camelToConstCase(associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName());
	}

	/**
	 * @return Association nodeB Id domain
	 */
	public Domain getNodeBPKDomain() {
		return associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getDomain();
	}

}
