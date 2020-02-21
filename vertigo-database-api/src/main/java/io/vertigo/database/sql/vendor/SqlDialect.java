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
package io.vertigo.database.sql.vendor;

import java.util.List;

import io.vertigo.util.StringUtil;

/**
 * The database dialect.
 * Provides all the vendor's specific SQL
 * @author mlaroche
 *
 */
public interface SqlDialect {
	/**
	 * how keys are generated
	 * @author pchretien
	 *
	 */
	public enum GenerationMode {
	GENERATED_KEYS, // H2, SQLServer, PostgreSQL...
	GENERATED_COLUMNS, //Oracle...
	}

	/**
	 * @return The operator for string concatenation.
	 */
	default String getConcatOperator() {
		return " || ";
	}

	/**
	 * Creates the insert request.
	 * @param idFieldName the id field's name
	 * @param dataFieldsName data fields
	 * @param sequencePrefix the prefix to use for sequence
	 * @param tableName the name of the table in which we want to insert
	 * @return the sql request
	 */
	String createInsertQuery(
			final String idFieldName,
			final List<String> dataFieldsName,
			String sequencePrefix,
			String tableName);

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour paginer le resultat {maxRows}, {skipRows}, {sortFieldName} {sortDesc}.
	 * @param query the sql query
	 * @param maxRows max rows (null if none)
	 * @param skipRows skip rows
	 * @param sortFieldName sort FieldName (null if none)
	 * @param sortDesc if sorting desc
	 */
	default void appendListState(final StringBuilder query, final Integer maxRows, final int skipRows, final String sortFieldName, final boolean sortDesc) {
		//syntax SQL:2008
		if (sortFieldName != null) {
			query.append(" order by ").append(StringUtil.camelToConstCase(sortFieldName));
			query.append(sortDesc ? " desc" : "");
		} else if (skipRows > 0 || maxRows != null) {
			query.append(" order by 1"); //order by is mandatory if offset or fetch next was use
		}

		if (skipRows > 0 || maxRows != null) {
			query.append(" offset ").append(skipRows).append(" rows");
		}
		if (maxRows != null) {
			query.append(" fetch next ").append(maxRows).append(" rows only");
		}
	}

	/**
	 * Requête à exécuter pour faire un select for update. Doit pouvoir être surchargé pour tenir compte des
	 * spécificités de la base de données utilisée..
	 * @param tableName nom de la table
	 * @param requestedFields the list of fields to retrieve (the select clause)
	 * @param idFieldName nom de la clé primaire
	 * @return select à exécuter.
	 */
	default String createSelectForUpdateQuery(final String tableName, final String requestedFields, final String idFieldName) {
		return new StringBuilder()
				.append(" select ").append(requestedFields)
				.append(" from ").append(tableName)
				.append(" where ").append(StringUtil.camelToConstCase(idFieldName)).append(" = #").append(idFieldName).append('#')
				.append(" for update ")
				.toString();
	}

	/**
	 * Statement to execute to verify the database connection
	 * @return statement to execute
	 */
	default String getTestQuery() {
		return "SELECT 1";
	}

	/**
	 * @return how keys are generated
	 */
	GenerationMode getGenerationMode();

}
