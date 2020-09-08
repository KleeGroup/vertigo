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

/**
 * This class adapts a non compliant java type to a compliant SQL java type.
 *
 *  Primitive	<=========> Java
 *  PrimitiveDataType <=Adapter=> JavaDataType)
 *
 * By default only a few types are allowed
 * 	- Integer, Long, Double, BigDecimal
 * 	- Boolean
 * 	- String
 *  - LocalDate, Instant
 *  - DataStream
 *
 *  If you want to store another value type like 'mail' you have to define a specific adapter.
 *  This Adapter MUST handle null value.
 *
 * @author pchretien
 * @param <J> JavaDataType (ex : Mail, Point)
 * @param <B> PrimitiveDataType (ex : String, Integer)
 */
public interface BasicTypeAdapter<J, B> {
	/**
	 * String -> Mail
	 * @param primitiveValue the stored value
	 * @param javaType ex : Mail, Point...
	 * @return the value transformed in POJO
	 */
	J toJava(B basicValue, Class<J> javaType);

	/**
	 * Mail -> String
	 * @param javaValue the pojo nullable value
	 * @return the value transformed in a basicType ex : String, Integer....
	 */
	B toBasic(J javaValue);

	/**
	 * ex : String, Integer....
	 * @return the BasicType
	 */
	BasicType getBasicType();

}
