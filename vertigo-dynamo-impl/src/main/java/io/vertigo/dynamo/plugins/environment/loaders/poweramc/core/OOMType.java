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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

/**
 * Type des objets powerAMC.
 * Les correspondances dynamo sont précisées ci-dessous.
 *
 * @author pchretien
 */
enum OOMType {
	/**
	 * Objet OOM décrivant un Package >>Package.
	 */
	Package("o:Package"),
	/**
	 * Objet OOM décrivant une Class >> DtDefinition.
	 */
	Class("o:Class"),
	/**
	 * Objet OOM décrivant un Domain >> Domain.
	 */
	Domain("o:Domain"),
	/**
	 * Objet OOM décrivant un Attibute d'un OOM >> DtField.
	 */
	Attribute("o:Attribute"),
	/**
	 * OOM décrivant un Identifier >> Assignation du caractère PK d'un DtField.
	 */
	Identifier("o:Identifier"),
	/**
	 * OOM décrivant une Association >> Association.
	 */
	Association("o:Association"),
	/**
	 * Référence sur un objet OOM.
	 */
	Shortcut("o:Shortcut");

	private final String code;

	OOMType(final String code) {
		this.code = code;
	}

	private String getCode() {
		return code;
	}

	static OOMType getType(final String name) {
		final OOMType type;
		if (Domain.getCode().equals(name)) {
			type = Domain;
		} else if (Package.getCode().equals(name)) {
			type = Package;
		} else if (Class.getCode().equals(name)) {
			type = Class;
		} else if (Shortcut.getCode().equals(name)) {
			type = Shortcut;
		} else if (Attribute.getCode().equals(name)) {
			type = Attribute;
		} else if (Identifier.getCode().equals(name)) {
			type = Identifier;
		} else if (Association.getCode().equals(name)) {
			type = Association;
		} else {
			//rien trouvé
			type = null;
		}
		return type;
	}

	static boolean isNodeByRef(final String name) {
		return Domain.getCode().equals(name)
				|| Attribute.getCode().equals(name)
				|| Class.getCode().equals(name)
				|| Shortcut.getCode().equals(name)
				|| Identifier.getCode().equals(name);
	}
}
