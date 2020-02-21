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
package io.vertigo.database.impl.sql.vendor.oracle;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Sql dialect for Oracle 12c+
 * @author npiedeloup
 */
class OracleDialect implements SqlDialect {
	/**
	 * Oracle n'autorise pas de sequence de plus de 30 char.
	 */
	private static final int ORACLE_SEQUENCE_NAME_MAX_LENGHT = 30;

	/** {@inheritDoc} */
	@Override
	public String createInsertQuery(
			final String idFieldName,
			final List<String> dataFieldsName,
			final String sequencePrefix,
			final String tableName) {
		Assertion.checkArgNotEmpty(idFieldName);
		Assertion.checkNotNull(dataFieldsName);
		Assertion.checkArgNotEmpty(sequencePrefix);
		Assertion.checkArgNotEmpty(tableName);
		//---
		return new StringBuilder()
				.append("insert into ").append(tableName).append(" (")
				.append(StringUtil.camelToConstCase(idFieldName)).append(", ")
				.append(dataFieldsName
						.stream()
						.map(StringUtil::camelToConstCase)
						.collect(Collectors.joining(", ")))
				.append(") values (")
				.append(getSequenceName(sequencePrefix, tableName)).append(".nextval , ")
				.append(dataFieldsName
						.stream()
						.map(fieldName -> " #dto." + fieldName + '#')
						.collect(Collectors.joining(", ")))
				.append(")")
				.toString();
	}

	/**
	 * Nom de la séquence utilisée lors des inserts
	 * @param dtDefinition Définition du DT mappé
	 * @return String Nom de la sequence
	 */
	private static String getSequenceName(final String sequencePrefix, final String tableName) {
		//oracle n'autorise pas de sequence de plus de 30 char.
		String sequenceName = sequencePrefix + tableName;
		if (sequenceName.length() > ORACLE_SEQUENCE_NAME_MAX_LENGHT) {
			sequenceName = sequenceName.substring(0, ORACLE_SEQUENCE_NAME_MAX_LENGHT);
		}
		return sequenceName;
	}

	/** {@inheritDoc} */
	@Override
	public GenerationMode getGenerationMode() {
		return GenerationMode.GENERATED_COLUMNS;
	}

	/** {@inheritDoc} */
	@Override
	public String getTestQuery() {
		return "SELECT 1 FROM DUAL";
	}
}
