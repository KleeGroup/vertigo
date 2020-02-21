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
package io.vertigo.studio.plugins.mda.search.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * Model used by FreeMarker.
 *
 * @author pchretien
 */
public final class SearchDtDefinitionModel {
	private final DtDefinition dtDefinition;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public SearchDtDefinitionModel(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

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

}
