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
package io.vertigo.dynamo.domain.metamodel.association;

import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.lang.Assertion;

/**
 * URI d'une liste définie par une association.
 *
 * @author pchretien
 */
public abstract class DtListURIForAssociation<A extends AssociationDefinition> extends DtListURI {
	private static final long serialVersionUID = 5933412183954919000L;

	private final String roleName;
	private final UID source;

	/**
	 * Constructor.
	 * @param associationDefinition Définition de l'association
	 * @param source URI (Clé primaire) du dtObject source
	 * @param roleName Nom du rôle
	 */
	protected DtListURIForAssociation(final A associationDefinition, final UID source, final String roleName) {
		super(AssociationUtil.getAssociationNode(associationDefinition, roleName).getDtDefinition());
		Assertion.checkNotNull(associationDefinition);
		Assertion.checkNotNull(source);
		Assertion.checkNotNull(roleName);
		//-----
		this.roleName = roleName;

		/**
		 * Noeud correspondant au role
		 */
		final AssociationNode target = AssociationUtil.getAssociationNode(associationDefinition, roleName);

		//On vérifie la cardinalité de la cible
		Assertion.checkArgument(target.isMultiple(), "le noeud cible doit être multiple");

		this.source = source;
	}

	/**
	 * @return Clé identifiant la ressource parmi les ressources du même type.
	 * Exemple :
	 */
	public UID getSource() {
		return source;
	}

	/**
	 * @return String Nom du rôle représentant la collection dans l'association
	 */
	public String getRoleName() {
		return roleName;
	}
}
