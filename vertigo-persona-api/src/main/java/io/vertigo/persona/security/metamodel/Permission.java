/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.security.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Une permission est l'association d'une op�ration et d'une ressource.
 * 
 * @author prahmoune
 * @version $Id: Permission.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
 */
@Prefix("PRM_")
public final class Permission implements Definition {
	private final String name;
	private final String operation;
	private final String filter;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de la permission
	 * @param operation Operation
	 * @param resource Ressource
	 */
	public Permission(final String name, final String operation, final String filter) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(operation);
		Assertion.checkArgNotEmpty(filter);
		// ---------------------------------------------------------------------
		this.name = name;
		this.operation = operation;
		this.filter = filter;
	}

	/**
	 * @return Filter used to check permission
	 */
	public String getFilter() {
		return filter;
	}

	/**	 
	 * @return Operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @return Nom de la permission
	 */
	public String getName() {
		return name;
	}

}