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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface centralisant les mappings à la BDD.
 *
 * @author pchretien
 */
public interface SqlMapping {
	/**
	 * Affecte les valeurs sur un statement.
	 *
	 * @param statement Statement SQL à affecter
	 * @param index Index de la variable dans le statement
	 * @param dataType Type primitif
	 * @param value Valeur à affecter sur le statement à l'index indiqué
	 * @throws SQLException Exception sql
	 */
	<O> void setValueOnStatement(PreparedStatement statement, int index, Class<O> dataType, O value) throws SQLException;

	/**
	 * Retourne la valeur typée vertigo d'un resultSet.
	 *
	 * @param resultSet ResultSet
	 * @param col Indexe de la colonne
	 * @param dataType Type primitif
	 * @return Valeur typée d'un resultSet
	 * @throws SQLException Exception sql
	 */
	<O> O getValueForResultSet(ResultSet resultSet, int col, Class<O> dataType) throws SQLException;

}
