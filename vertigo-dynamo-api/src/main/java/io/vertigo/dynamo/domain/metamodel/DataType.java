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
package io.vertigo.dynamo.domain.metamodel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.DataStream;

/**
 * Primitive types.
 * This class defines the primitive types.
 * @author  pchretien
 */
public enum DataType {
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
	 * The java class wrapped by this dataType.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructor.
	 * @param javaClass the java class
	 */
	DataType(final Class<?> javaClass) {
		Assertion.checkNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	/**
	 * Teste si la valeur passée en paramétre est est conforme au type.
	 * Lance une exception avec message adequat si pb.
	 * @param value Valeur é tester
	 */
	void checkValue(final Object value) {
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
		return this == DataType.LocalDate
				|| this == DataType.Instant;
	}

	/**
	 * @return if the dataType is a number
	 */
	public boolean isNumber() {
		return this == DataType.Double
				|| this == DataType.BigDecimal
				|| this == DataType.Long
				|| this == DataType.Integer;
	}

	/**
	 * @return the native java class wrapped by this dataType
	 */
	public Class<?> getJavaClass() {
		return javaClass;
	}

	/**
	 * Finds the dataType bound to a class.
	 * @param type
	 * @return Optional DataType of this Class
	 */
	public static Optional<DataType> of(final Class type) {
		Assertion.checkNotNull(type);
		//---
		DataType dataType;
		if (Integer.class.equals(type) || int.class.equals(type)) {
			dataType = DataType.Integer;
		} else if (Double.class.equals(type) || double.class.equals(type)) {
			dataType = DataType.Double;
		} else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			dataType = DataType.Boolean;
		} else if (String.class.equals(type)) {
			dataType = DataType.String;
		} else if (LocalDate.class.equals(type)) {
			dataType = DataType.LocalDate;
		} else if (Instant.class.equals(type)) {
			dataType = DataType.Instant;
		} else if (java.math.BigDecimal.class.equals(type)) {
			dataType = DataType.BigDecimal;
		} else if (Long.class.equals(type) || long.class.equals(type)) {
			dataType = DataType.Long;
		} else if (DataStream.class.equals(type)) {
			dataType = DataType.DataStream;
		} else {
			//not a well known dataType
			dataType = null;
		}
		return Optional.ofNullable(dataType);
	}
}
