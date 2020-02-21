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
package io.vertigo.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import io.vertigo.lang.Assertion;

/**
 * The DateUtil provides usefull methods concerning dates.
 *
 * On distingue deux types de date
 *  - les dates précises au jour
 *  - les dates précises au jour, min, sec (ms)
 *
 * @author npiedeloup, pchretien
 */
public final class DateUtil {
	private DateUtil() {
		super();
	}

	/**
	 * Récupère la date courante (précise au jour).
	 * @return current day.
	 */
	public static LocalDate newDate() {
		return LocalDate.now();
	}

	/**
	 * Récupère l'instant courant (avec heures, minutes, secondes, millisecondes).
	 * @return current day.
	 */
	public static Instant newInstant() {
		return Instant.now();
	}

	/**
	 * Calcule le nombre de jours entre deux dates.
	 *
	 * @param startDate Date de début
	 * @param endDate Date de fin
	 * @return Nombre de jours
	 */
	public static long daysBetween(final LocalDate startDate, final LocalDate endDate) {
		Assertion.checkNotNull(startDate);
		Assertion.checkNotNull(endDate);
		//-----
		return Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
	}

	/**
	 * Compare deux Date.
	 * Cette méthode s'utilise comme firstDate.compareTo(secondDate).
	 * On peut alors utiliser l'opérateur que l'on souhaite entre le résultat du compareTo et 0.
	 * Ex: firstDate <= secondDate   eq.  firstDate.compareTo(secondDate) <= 0
	 *     firstDate > secondDate    eq.  firstDate.compareTo(secondDate) > 0
	 * @param firstDate première date
	 * @param secondDate Deuxiéme date
	 * @return 0 si égale, moins de 0 si firstDate < secondDate, et plus de 0 si firstDate > secondDate
	 */
	public static int compareLocalDate(final LocalDate firstDate, final LocalDate secondDate) {
		Assertion.checkNotNull(firstDate);
		Assertion.checkNotNull(secondDate);
		//-----
		return firstDate.compareTo(secondDate);
	}

	/**
	 * Compare deux dateTime.
	 * Cette méthode s'utilise comme firstDate.compareTo(secondDate).
	 * On peut alors utiliser l'opérateur que l'on souhaite entre le résultat du compareTo et 0.
	 * Ex: firstDate <= secondDate   eq.  firstDate.compareTo(secondDate) <= 0
	 *     firstDate > secondDate    eq.  firstDate.compareTo(secondDate) > 0
	 * @param firstInstant première dateTime
	 * @param secondInstant Deuxiéme dateTime
	 * @return 0 si égale, moins de 0 si firstDate < secondDate, et plus de 0 si firstDate > secondDate
	 */
	public static int compareInstant(final Instant firstInstant, final Instant secondInstant) {
		Assertion.checkNotNull(firstInstant);
		Assertion.checkNotNull(secondInstant);
		//-----
		return firstInstant.compareTo(secondInstant);
	}

	/**
	 * Retourne la date correspondant à l'expression passée en parametre.
	 * Implements parsing of a date expression.
	 * y=year, M=month, w=week
	 * d=day, h=hour, m=minute, s= second
	 * Mind the UpperCase : 'M'onth and 'm'inute !
	 * now+1d
	 * now-6d
	 * now+2w
	 * now-12M
	 * now-2y
	 * "06/12/2003", "dd/MM/yyyy"
	 *
	 * @param dateExpression Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 *
	 * @return date
	 */
	public static LocalDate parseToLocalDate(final String dateExpression, final String datePattern) {
		return DateQueryParserUtil.parseAsLocalDate(dateExpression, datePattern);
	}

	/**
	 * Retourne la date correspondant à l'expression passée en parametre.
	 * Implements parsing of a date expression.
	 * y=year, M=month, w=week
	 * d=day, h=hour, m=minute, s= second
	 * Mind the UpperCase : 'M'onth and 'm'inute !
	 * now+1d
	 * now-6d
	 * now+2w
	 * now-12M
	 * now-2y
	 * "06/12/2003", "dd/MM/yyyy"
	 *
	 * @param dateExpression Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 *
	 * @return Instant
	 */
	public static Instant parseToInstant(final String dateExpression, final String datePattern) {
		return DateQueryParserUtil.parseAsInstant(dateExpression, datePattern);
	}
}
