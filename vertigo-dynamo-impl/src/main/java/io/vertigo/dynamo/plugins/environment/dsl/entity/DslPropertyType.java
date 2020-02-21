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
package io.vertigo.dynamo.plugins.environment.dsl.entity;

import java.util.Locale;

import io.vertigo.lang.Assertion;

/**
 * @author  pchretien
 */
public enum DslPropertyType implements DslEntityFieldType {
	/** Integer. */
	Integer(Integer.class),
	/** Double. */
	Double(Double.class),
	/** Boolean. */
	Boolean(Boolean.class),
	/** String. */
	String(String.class);

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructor.
	 *
	 * @param javaClass Classe java encapsulée
	 */
	DslPropertyType(final Class<?> javaClass) {
		Assertion.checkNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	/**
	 * Check value against this type.
	 * @param value Value to check (nullable)
	 */
	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Valeur " + value + " ne correspond pas au type :" + this);
		}
	}

	/**
	 * Convert a read string value, to this DSL type.
	 * @param stringValue string input
	 * @return Typed object of this string
	 */
	public Object cast(final String stringValue) {
		final String sValue = stringValue == null ? null : stringValue.trim();
		if (sValue == null || sValue.length() == 0) {
			return null;
		}
		switch (this) {
			case Integer:
				return java.lang.Integer.valueOf(sValue);
			case Double:
				return java.lang.Double.valueOf(sValue);
			case String:
				return sValue;
			case Boolean:
				switch (sValue.toLowerCase(Locale.ROOT)) {
					/* only true and false are accepted*/
					case "true":
						return true;
					case "false":
						return false;
					default:
						throw new IllegalArgumentException("unable to cast boolean property from '" + sValue + "', only. 'true' or 'false' are accepted");
				}
			default:
				throw new IllegalArgumentException("unsupported type of property : '" + javaClass + "'");
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isProperty() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEntityLink() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEntity() {
		return false;
	}
}
