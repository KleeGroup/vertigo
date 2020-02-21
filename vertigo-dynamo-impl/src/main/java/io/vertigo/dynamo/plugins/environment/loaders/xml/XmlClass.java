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
package io.vertigo.dynamo.plugins.environment.loaders.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * Classe ou entité.
 * @author pchretien, pforhan
 */
public final class XmlClass {
	private final String code;
	private final String packageName;
	private final String stereotype;
	private final List<XmlAttribute> keyAttributes;
	private final List<XmlAttribute> fieldAttributes;

	/**
	 * Constructor.
	 * @param code Code
	 * @param packageName package name
	 * @param stereotype stereotype
	 * @param keyAttributes Listes des champs identifiants (PK).
	 * @param fieldAttributes Liste des champs non PK.
	 */
	public XmlClass(final String code, final String packageName, final String stereotype, final List<XmlAttribute> keyAttributes, final List<XmlAttribute> fieldAttributes) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkNotNull(keyAttributes);
		Assertion.checkNotNull(fieldAttributes);
		//-----
		this.code = code;
		this.packageName = packageName;
		this.stereotype = stereotype;
		this.keyAttributes = Collections.unmodifiableList(new ArrayList<>(keyAttributes));
		this.fieldAttributes = Collections.unmodifiableList(new ArrayList<>(fieldAttributes));
	}

	/**
	 * @return Code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Stereotype
	 */
	public String getStereotype() {
		return stereotype;
	}

	/**
	 * @return Listes des champs identifiants (PK).
	 */
	public List<XmlAttribute> getKeyAttributes() {
		return keyAttributes;
	}

	/***
	 * @return Liste des champs non PK.
	 */
	public List<XmlAttribute> getFieldAttributes() {
		return fieldAttributes;
	}
}
