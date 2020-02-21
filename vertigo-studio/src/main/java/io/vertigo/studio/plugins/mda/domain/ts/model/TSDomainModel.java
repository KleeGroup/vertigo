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
package io.vertigo.studio.plugins.mda.domain.ts.model;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.lang.Assertion;

/**
 * Model used to define a Domain.
 *
 * @author npiedeloup
 */
public final class TSDomainModel {
	private final Domain domain;

	/***
	 * Constructor.
	 * @param domain Domain
	 */
	TSDomainModel(final Domain domain) {
		Assertion.checkNotNull(domain);
		//-----
		this.domain = domain;
	}

	/**
	 * @return Type javascript du champ with cardinality
	 */
	public String getTypescriptType() {
		return buildTypescriptType(domain, true);
	}

	/**
	 * @return Name of the domain
	 */
	public String getDomainName() {
		return domain.getName();
	}

	/**
	 * @return Local name of the domain
	 */
	public String getDomainDefinitionName() {
		return domain.getDtDefinition().getLocalName();
	}

	/**
	 * @return Simple TS type
	 */
	public String getDomainTypeName() {
		return buildTypescriptType(domain, false);
	}

	/**
	 * @return True si le type est une primitive.
	 */
	public boolean isPrimitive() {
		return domain.getScope().isPrimitive();
	}

	/**
	 * Returns the javascript type.
	 * @param  domain DtDomain
	 * @return String
	 */
	private static String buildTypescriptType(final Domain domain, final boolean withArray) {
		if (domain.getScope().isPrimitive()) {
			final DataType dataType = domain.getDataType();
			if (dataType.isNumber()) {
				return "number";
			} else if (dataType == DataType.Boolean) {
				return "boolean";
			}
			return "string";
		}
		return domain.getJavaClass().getSimpleName() + ((domain.isMultiple() && withArray) ? "[]" : "");
	}
}
