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
package io.vertigo.dynamox.domain.formatter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;

import io.vertigo.app.Home;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.StringUtil;

/**
 * Gestion des formattages de dates.
 * Args contient plusieurs arguments séparés par des points virgules ';'
 *
 * Le premier argument est obligatoire il représente le format d'affichage d'une date .
 * Les arguments suivants sont facultatifs ils représentent les autres formats de saisie autorisés.
 * Par défaut le premier format de saisie autorisé est le format d'affichage.
 * En effet, pour des raisons ergonomiques il est toujours préférable de pouvoir saisir ce qui est affiché.
 *
 * Exemple 1 d'argument : "dd/MM/yyyy "
 *  On affiche la date au format dd/MM/yyyy
 *  En saisie on autorise dd/MM/yyyy

 * Exemple 2 d'argument : "dd/MM/yyyy ; dd/MM/yy"
 *  On affiche la date au format dd/MM/yyyy
 *  En saisie on autorise dd/MM/yyyy et dd/MM/yy
 *
 * @author pchretien
 */
public final class FormatterDate implements Formatter {
	/**
	 * Format(s) étendu(s) de la date en saisie.
	 * Cette variable n'est créée qu'au besoin.
	 */
	@JsonExclude
	private final List<String> patterns;

	/**
	 * Constructor.
	 */
	public FormatterDate(final String args) {
		// Les arguments ne doivent pas être vides.
		assertArgs(args != null);
		//-----
		final ListBuilder<String> patternsBuilder = new ListBuilder<>();
		for (final String token : args.split(";")) {
			patternsBuilder.add(token.trim());
		}

		//Saisie des dates
		//Le format d'affichage est le premier format de saisie autorisé
		//Autres saisies autorisées (facultatifs)
		patterns = patternsBuilder.unmodifiable().build();
		assertArgs(!patterns.isEmpty());
	}

	private static void assertArgs(final boolean test) {
		Assertion.checkArgument(test, "Les arguments pour la construction de FormatterDate sont invalides :format affichage;{autres formats de saisie}");
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType.isAboutDate(), "this formatter only applies on date formats");
		//-----
		if (objValue == null) {
			return ""; //Affichage d'une date non renseignée;
		}
		switch (dataType) {
			case LocalDate:
				return localDateToString((LocalDate) objValue, patterns.get(0));
			case Instant:
				return instantToString((Instant) objValue, patterns.get(0));
			default:
				throw new IllegalStateException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		Assertion.checkArgument(dataType.isAboutDate(), "Formatter ne s'applique qu'aux dates");
		//-----
		if (StringUtil.isEmpty(strValue)) {
			return null;
		}
		final String sValue = strValue.trim();
		switch (dataType) {
			case LocalDate:
				return applyStringToObject(sValue, FormatterDate::doStringToLocalDate);
			case Instant:
				return applyStringToObject(sValue, FormatterDate::doStringToInstant);
			default:
				throw new IllegalStateException();
		}
	}

	/*
	 *  Cycles through patterns to try and parse given String into a Date | LocalDate | Instant
	 */
	private <T> T applyStringToObject(final String dateString, final BiFunction<String, String, T> fun) throws FormatterException {
		//StringToDate renvoit null si elle n'a pas réussi à convertir la date
		T dateValue = null;
		for (int i = 0; i < patterns.size() && dateValue == null; i++) {
			try {
				dateValue = fun.apply(dateString, patterns.get(i));
			} catch (final Exception e) {
				dateValue = null;
			}
		}
		//A null dateValue means all conversions have failed
		if (dateValue == null) {
			throw new FormatterException(Resources.DYNAMOX_DATE_NOT_FORMATTED);
		}
		return dateValue;
	}

	/*
	 * Converts a String to a LocalDate according to a given pattern
	 */
	private static LocalDate doStringToLocalDate(final String dateString, final String pattern) {
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDate.parse(dateString, dateTimeFormatter);
	}

	/*
	 * Converts a String to a Instant according to a given pattern
	 */
	private static Instant doStringToInstant(final String dateString, final String pattern) {
		return DateTimeFormatter.ofPattern(pattern)
				.withZone(getLocaleManager().getCurrentZoneId())
				.parse(dateString, Instant::from);
	}

	private static String localDateToString(final LocalDate localDate, final String pattern) {
		return DateTimeFormatter.ofPattern(pattern)
				.format(localDate);
	}

	private static String instantToString(final Instant instant, final String pattern) {
		return DateTimeFormatter.ofPattern(pattern)
				.withZone(getLocaleManager().getCurrentZoneId())
				.format(instant);
	}

	private static LocaleManager getLocaleManager() {
		return Home.getApp().getComponentSpace().resolve(LocaleManager.class);
	}
}
