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
package io.vertigo.dynamo.plugins.environment.registries.file;

import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DynamicRegistry;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;

/**
 * @author pchretien
 */
public final class FileDynamicRegistry implements DynamicRegistry {

	@Override
	public DslGrammar getGrammar() {
		return new FileGrammar();
	}

	/** {@inheritDoc} */
	@Override
	public DefinitionSupplier supplyDefinition(final DslDefinition dslDefinition) {
		final DslEntity dslEntity = dslDefinition.getEntity();
		if (FileGrammar.FILE_INFO_DEFINITION_ENTITY.equals(dslEntity)) {
			//Seuls les taches sont gérées.
			return definitionSpace -> createFileDefinition(dslDefinition);
		}
		throw new IllegalStateException("The type of definition" + dslDefinition + " is not managed by me");
	}

	private static FileInfoDefinition createFileDefinition(final DslDefinition xFileDefinition) {
		final String fileDefinitionName = xFileDefinition.getName();
		final String storeName = (String) xFileDefinition.getPropertyValue(KspProperty.DATA_SPACE);

		return new FileInfoDefinition(fileDefinitionName, storeName);
	}

}
