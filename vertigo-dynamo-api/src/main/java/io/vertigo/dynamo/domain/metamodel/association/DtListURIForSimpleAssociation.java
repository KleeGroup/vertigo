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

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.model.UID;

/**
 * URI for simple 1N relation list.
 * @author npiedeloup
 */
public final class DtListURIForSimpleAssociation extends DtListURIForAssociation<AssociationSimpleDefinition> {
	private static final long serialVersionUID = -6235569695625996356L;
	private final DefinitionReference<AssociationSimpleDefinition> associationSimpleDefinitionRef;

	/**
	 * @param associationDefinition Association definition
	 * @param source URI source
	 * @param roleName role of this association
	 */
	public DtListURIForSimpleAssociation(final AssociationSimpleDefinition associationDefinition, final UID source, final String roleName) {
		super(associationDefinition, source, roleName);
		associationSimpleDefinitionRef = new DefinitionReference<>(associationDefinition);
	}

	/**
	 * @return Association definition.
	 */
	public AssociationSimpleDefinition getAssociationDefinition() {
		return associationSimpleDefinitionRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public String buildUrn() {
		return getAssociationDefinition().getName() + D2A_SEPARATOR + getRoleName() + D2A_SEPARATOR + getSource().urn();
	}
}
