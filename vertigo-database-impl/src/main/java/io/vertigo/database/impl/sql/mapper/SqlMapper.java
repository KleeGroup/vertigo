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
package io.vertigo.database.impl.sql.mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.database.sql.mapper.SqlAdapter;
import io.vertigo.lang.Assertion;

public final class SqlMapper {
	private static final SqlAdapter<Object, Object> IDENTITY = new SqlAdapter<Object, Object>() {

		@Override
		public Object toJava(final Object sqlValue) {
			return sqlValue;
		}

		@Override
		public Object toSql(final Object javaValue) {
			return javaValue;
		}

		@Override
		public Class getJavaDataType() {
			throw new UnsupportedOperationException("Can't use getJavaDataType");
		}

		@Override
		public Class getSqlDataType() {
			throw new UnsupportedOperationException("Can't use getSqlDataType");
		}

	};
	private final Map<Class, SqlAdapter> adaptersByJavaType;

	public SqlMapper(final List<SqlAdapter> adapters) {
		Assertion.checkNotNull(adapters);
		//---
		adaptersByJavaType = adapters
				.stream()
				.collect(Collectors.toMap(SqlAdapter::getJavaDataType, Function.identity()));
	}

	public Class getSqlType(final Class javaType) {
		return adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).getSqlDataType() : javaType;
	}

	// mail from ResultSet contains a String, dataType=Mail
	public <J> J toJava(final Class<J> javaType, final Object sqlValue) {
		return (J) adaptersByJavaType.getOrDefault(javaType, IDENTITY).toJava(sqlValue);
	}

	// javaValue =Mail
	//-> String
	public Object toSql(final Class javaType, final Object javaValue) {
		return adaptersByJavaType.getOrDefault(javaType, IDENTITY).toSql(javaValue);
	}

}
