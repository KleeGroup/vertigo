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

import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class SqlAssociationSimpleModel {
	private final AssociationSimpleDefinition associationDefinition;

	/**
	 * Constructeur.
	 * @param associationNode Noeud de l'association à générer
	 */
	public SqlAssociationSimpleModel(final AssociationSimpleDefinition associationDefinition) {
		Assertion.checkNotNull(associationDefinition);
		//-----
		this.associationDefinition = associationDefinition;
	}

	/**
	 * @return Association name in CONST_CASE
	 */
	public String getName() {
		return StringUtil.camelToConstCase(associationDefinition.getName());
	}

	/**
	 * @return Association foreign table
	 */
	public String getForeignTable() {
		return StringUtil.camelToConstCase(associationDefinition.getForeignAssociationNode().getDtDefinition().getLocalName());
	}

	/**
	 * @return Association primary table
	 */
	public String getPrimaryTable() {
		return StringUtil.camelToConstCase(associationDefinition.getPrimaryAssociationNode().getDtDefinition().getLocalName());
	}

	/**
	 * @return Association FK
	 */
	public String getForeignColumn() {
		return StringUtil.camelToConstCase(associationDefinition.getFKField().getName());
	}

	/**
	 * @return Association PK
	 */
	public String getPrimaryIdColumn() {
		return StringUtil.camelToConstCase(associationDefinition.getPrimaryAssociationNode().getDtDefinition().getIdField().get().getName());
	}
}
