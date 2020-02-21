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
package io.vertigo.database.sql.statement;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Paramètres créés par l'analyseur et utilisés par le Handler.
 *
 * - IN 	: le séparateur utilisé est #
 *
 * @author pchretien
 */
final class SqlNamedParam {
	private final String betweenCar;
	private final String attributeName;
	private final String fieldName;
	private final Integer rowNumber;

	/**
	 * Constructor.
	 *
	 * @param betweenCar String
	 */
	private SqlNamedParam(final String betweenCar) {
		Assertion.checkArgNotEmpty(betweenCar);
		//---
		this.betweenCar = betweenCar;
		final String[] tokens = betweenCar.split("\\.");
		if (tokens.length == 1) {
			//Simple bound param : CODE
			attributeName = tokens[0];
			fieldName = null;
			rowNumber = null;
		} else if (tokens.length == 2) {
			// we have two cases : a list of primitive or an objet with a field
			if (tokens[1].matches("\\d+")) {
				//list of primivtives
				// example : MOVIE_ID_LIST.0
				attributeName = tokens[0];
				fieldName = null;
				rowNumber = parseDtcRowNumber(tokens[1]);
			} else {
				// a field of a bean is bound
				// example : DTO_MOVIE.TITLE
				attributeName = tokens[0];
				fieldName = tokens[1];
				rowNumber = null;
			}
		} else if (tokens.length == 3) {
			// cas particulier des DTC : il y a qqc entre le premier et le deuxieme point
			// qui doit être un entier >= 0
			// exemple : DTC_PERSONNE.12.Nom
			attributeName = tokens[0];
			fieldName = tokens[2];
			rowNumber = parseDtcRowNumber(tokens[1]);
		} else {
			throw new IllegalStateException();
		}
		Assertion.checkNotNull(attributeName);
	}

	/**
	 * Static builder.
	 * @param betweenCar Separator char
	 * @return new SqlNamedParam
	 */
	public static SqlNamedParam of(final String betweenCar) {
		return new SqlNamedParam(betweenCar);
	}

	private static int parseDtcRowNumber(final String betweenPoints) {
		try {
			final Integer dtcRowNumber = Integer.valueOf(betweenPoints);
			Assertion.checkState(dtcRowNumber != null && dtcRowNumber >= 0, "Paramètre incohérent : {0} doit être positif ou null.", betweenPoints);
			return dtcRowNumber;
		} catch (final NumberFormatException nfe) {
			throw WrappedException.wrap(nfe, "Param {0} must be an integer.", betweenPoints);
		}
	}

	/**
	 * Un paramètre est primitif s'il n'y a pas de champ associé.
	 *
	 * @return S'il s'agit d'un paramètre primitif
	 */
	public boolean isPrimitive() {
		return fieldName == null;
	}

	/**
	 * @return Paramètre de type liste.
	 */
	public boolean isList() {
		return rowNumber != null;
	}

	/**
	 * @return Paramètre de type Objet.(DTO)
	 */
	public boolean isObject() {
		return !isPrimitive();
	}

	/**
	 * @return Nom de l'attribut de la tache (ou paramètre de tache)
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return Numéro de ligne dans le cas d'un paramètre représentant un élément d'une liste
	 */
	public int getRowNumber() {
		Assertion.checkNotNull(rowNumber, "il ne s'agit pas d'une liste");
		//-----
		return rowNumber;
	}

	@Override
	public String toString() {
		return betweenCar;
	}
}
