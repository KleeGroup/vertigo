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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

/**
 * Type d'objets du XMI géré.
 * @author pforhan
 */
public enum EAXmiType {
	/**
	 * Objet Xmi décrivant un Package >>Package.
	 */
	Package("uml:Package"),
	/**
	 * Objet Xmi décrivant une Classe >> DtDefinition.
	 */
	Class("uml:Class"),
	/**
	 * Objet Xmi décrivant un Attibut d'une Classe >> DtField.
	 */
	Attribute("uml:Property"),
	/**
	 * Objet Xmi décrivant une Association >> Association.
	 */
	Association("uml:Association"),
	/**
	 * Tag Xmi pour un élément à traiter.
	 */
	PackageElement("packageElement"),
	/**
	 * Tag Xmi pour un attribut de classe.
	 */
	OwnedAttribute("ownedAttribute"),
	/**
	 * Tag d'extension EA pour un attribut.
	 */
	ClassAttribute("attribute"),
	/**
	 * Tag d'extension EA pour une association.
	 */
	Connector("connector");

	private final String code;

	private EAXmiType(final String code) {
		this.code = code;
	}

	private String getCode() {
		return code;
	}

	static EAXmiType getType(final String name) {
		final EAXmiType type;
		if (Package.getCode().equals(name)) {
			type = Package;
		} else if (Class.getCode().equals(name)) {
			type = Class;
		} else if (Attribute.getCode().equals(name)) {
			type = Attribute;
		} else if (Association.getCode().equals(name)
				|| Connector.getCode().equals(name)) {
			type = Association;
		} else if (ClassAttribute.getCode().equals(name)) {
			type = ClassAttribute;
		} else {
			//rien trouvé
			type = null;
		}
		return type;
	}

	static boolean isNodeByRef(final String name) {
		return Attribute.getCode().equals(name)
				|| Class.getCode().equals(name)
				|| ClassAttribute.getCode().equals(name)
				|| Package.getCode().equals(name)
				|| Connector.getCode().equals(name);
	}

	static boolean isObjet(final String type, final String tagName) {
		return Attribute.getCode().equals(type) && OwnedAttribute.getCode().equals(tagName)
				|| Class.getCode().equals(type)
				|| Package.getCode().equals(type)
				|| Association.getCode().equals(type);
	}

	boolean isAttribute() {
		return this == Attribute;
	}

	boolean isClass() {
		return this == Class
				|| this == Association;
	}
}
