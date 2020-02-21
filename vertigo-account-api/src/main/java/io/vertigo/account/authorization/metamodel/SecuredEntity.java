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
package io.vertigo.account.authorization.metamodel;

import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;

/**
 * Une SecuredEntity est une entité sécurisé.
 * Secured entity definition defined how an entity is secured.
 * - entity definition
 * - fields used for security purpose
 * - security dimension
 * - allowed operations
 *
 *
 * @author jgarnier, npiedeloup
 */
@DefinitionPrefix("Sec")
public final class SecuredEntity implements Definition {
	private final String name;
	private final DtDefinition entityDefinition;
	private final List<DtField> securityFields;
	private final List<SecurityDimension> advancedDimensions;
	private final List<Authorization> operations;

	/**
	 * Constructs an instance of SecurityEntity.
	 *
	 * @param entityDefinition Entity sécurisé.
	 * @param securityFields fields simple de sécurité.
	 * @param advancedDimensions axes avancés de sécurité.
	 * @param operations opérations attribuées.
	 */
	public SecuredEntity(
			final DtDefinition entityDefinition,
			final List<DtField> securityFields,
			final List<SecurityDimension> advancedDimensions,
			final List<Authorization> operations) {
		Assertion.checkNotNull(entityDefinition);
		Assertion.checkNotNull(securityFields);
		Assertion.checkNotNull(advancedDimensions);
		Assertion.checkNotNull(operations);
		//---
		name = "Sec" + entityDefinition.getName();
		this.entityDefinition = entityDefinition;
		this.securityFields = securityFields;
		this.advancedDimensions = advancedDimensions;
		this.operations = operations;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the value of entity.
	 */
	public DtDefinition getEntity() {
		return entityDefinition;
	}

	/**
	 * @return the list of security fieldNames.
	 */
	public List<DtField> getSecurityFields() {
		return securityFields;
	}

	/**
	 * @return the value of axes.
	 */
	public List<SecurityDimension> getSecurityDimensions() {
		return advancedDimensions;
	}

	/**
	 * @return the value of operations.
	 */
	public List<Authorization> getOperations() {
		return operations;
	}
}
