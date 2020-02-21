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
package io.vertigo.database.sql.mapper;

/**
 * This class adapts a non compliant SQL java type to a compliant SQL java type.
 *
 *  Sql			<=========> Java
 *  SqlDataType <=Adapter=> JavaDataType)
 *
 * By default only a few types are allowed
 * 	- Integer, Long, Double, BigDecimal
 * 	- Boolean
 * 	- String
 *  - LocalDate, Instant
 *  - DataStream
 *
 *  If you want to store another value type like 'mail' you have to define a specific adapter.
 *
 * @author pchretien
 * @param <J> JavaDataType (ex : Mail, Point)
 * @param <S> SqlDataType (ex : String, Integer)
 */
public interface SqlAdapter<J, S> {
	/**
	 * String -> Mail
	 * @param sqlValue the stored value
	 * @return the value transformed in POJO
	 */
	J toJava(S sqlValue);

	/**
	 * Mail -> String
	 * @param javaValue the pojo value
	 * @return the value transformed in native sql type
	 */
	S toSql(J javaValue);

	/**
	 * ex : Mail, Point...
	 * @return the JavaDataTyp
	 */
	Class<J> getJavaDataType();

	/**
	 * ex : String, Integer....
	 * @return the DataSqlType
	 */
	Class<S> getSqlDataType();
}
