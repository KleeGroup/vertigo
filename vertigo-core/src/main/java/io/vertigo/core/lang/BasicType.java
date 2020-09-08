/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Basic types.
 * This class defines ALL the basic types, used by Vertigo.
 * This set is limited to only a few types.
 * @author  pchretien
 */
public enum BasicType {
	/** Integer. */
	Integer(Integer.class),
	/** Double. */
	Double(Double.class),
	/** Boolean. */
	Boolean(Boolean.class),
	/** String. */
	String(String.class),
	/** LocalDate. */
	LocalDate(LocalDate.class),
	/** Instant. */
	Instant(Instant.class),
	/** BigDecimal. */
	BigDecimal(java.math.BigDecimal.class),
	/** Long. */
	Long(Long.class),
	/** DataStream. */
	DataStream(DataStream.class);

	/**
	 * The java class wrapped by this basic type.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructor.
	 * @param javaClass the java class
	 */
	BasicType(final Class<?> javaClass) {
		Assertion.check().isNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	/**
	 * Teste si la valeur passée en paramétre est est conforme au type.
	 * Lance une exception avec message adequat si pb.
	 * @param value Valeur é tester
	 */
	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Value " + value + " doesn't match :" + this);
		}
	}

	/**
	 * @return if the dataType talks about a date
	 */
	public boolean isAboutDate() {
		return this == BasicType.LocalDate
				|| this == BasicType.Instant;
	}

	/**
	 * @return if the basic type is a number
	 */
	public boolean isNumber() {
		return this == BasicType.Double
				|| this == BasicType.BigDecimal
				|| this == BasicType.Long
				|| this == BasicType.Integer;
	}

	/**
	 * @return the native java class wrapped by this dataType
	 */
	public Class getJavaClass() {
		return javaClass;
	}

	/**
	 * Finds the basic type bound to a class.
	 * @param type a candidate type
	 * @return Optional Basic Type of this Class
	 */
	public static Optional<BasicType> of(final Class type) {
		Assertion.check().isNotNull(type);
		//---
		BasicType basicType;
		if (Integer.class.equals(type) || int.class.equals(type)) {
			basicType = BasicType.Integer;
		} else if (Double.class.equals(type) || double.class.equals(type)) {
			basicType = BasicType.Double;
		} else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			basicType = BasicType.Boolean;
		} else if (String.class.equals(type)) {
			basicType = BasicType.String;
		} else if (LocalDate.class.equals(type)) {
			basicType = BasicType.LocalDate;
		} else if (Instant.class.equals(type)) {
			basicType = BasicType.Instant;
		} else if (java.math.BigDecimal.class.equals(type)) {
			basicType = BasicType.BigDecimal;
		} else if (Long.class.equals(type) || long.class.equals(type)) {
			basicType = BasicType.Long;
		} else if (DataStream.class.equals(type)) {
			basicType = BasicType.DataStream;
		} else {
			//not a well known basicType
			basicType = null;
		}
		return Optional.ofNullable(basicType);
	}
}
