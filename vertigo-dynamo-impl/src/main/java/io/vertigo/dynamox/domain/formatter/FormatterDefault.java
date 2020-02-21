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

import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.lang.Assertion;

/**
 * Default formatter for boolean, number, date and string.
 * It's possible to override default formatting by registering a specific formatter with a conventional name.
 * FMT_STRING_DEFAULT, FMT_DATE_DEFAULT, FMT_BOOLEAN_DEFAULT, FMT_NUMBER_DEFAULT
 * Must be declared before this default Formatter !!
 * @author pchretien, npiedeloup
 */
public final class FormatterDefault implements Formatter {
	private static final String FMT_STRING_DEFAULT = "FmtStringDefault";
	private static final String FMT_LOCAL_DATE_DEFAULT = "FmtLocalDateDefault";
	private static final String FMT_INSTANT_DEFAULT = "FmtInstantDefault";
	private static final String FMT_BOOLEAN_DEFAULT = "FmtBooleanDefault";
	private static final String FMT_NUMBER_DEFAULT = "FmtNumberDefault";

	private final Formatter booleanFormatter;
	private final Formatter numberformatter;
	private final Formatter localDateFormater;
	private final Formatter instantFormater;
	private final Formatter stringFormatter;

	/**
	 * Constructor.
	 */
	public FormatterDefault(final String args) {
		// Les arguments doivent être vides.
		Assertion.checkArgument(args == null, "Les arguments pour la construction de FormatterDefault sont invalides");
		//-----
		booleanFormatter = obtainFormatterBoolean();
		numberformatter = obtainFormatterNumber();
		localDateFormater = obtainFormatterLocalDate();
		instantFormater = obtainFormatterInstant();
		stringFormatter = obtainFormatterString();
	}

	private static DefinitionSpace getDefinitionSpace() {
		return Home.getApp().getDefinitionSpace();
	}

	/**
	 *
	 * @param dataType Type
	 * @return Formatter simple utilisé.
	 */
	public Formatter getFormatter(final DataType dataType) {
		switch (dataType) {
			case String:
				return stringFormatter;
			case LocalDate:
				return localDateFormater;
			case Instant:
				return instantFormater;
			case Boolean:
				return booleanFormatter;
			case Integer:
			case Long:
			case Double:
			case BigDecimal:
				return numberformatter;
			case DataStream:
			default:
				throw new IllegalArgumentException(dataType + " n'est pas géré par ce formatter");
		}
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		return getFormatter(dataType).valueToString(objValue, dataType);
	}

	/** {@inheritDoc} */
	@Override
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		return getFormatter(dataType).stringToValue(strValue, dataType);
	}

	private static Formatter obtainFormatterBoolean() {
		if (getDefinitionSpace().contains(FMT_BOOLEAN_DEFAULT)) {
			return getDefinitionSpace().resolve(FMT_BOOLEAN_DEFAULT, FormatterDefinition.class);
		}
		return new FormatterBoolean("Oui; Non");
	}

	private static Formatter obtainFormatterNumber() {
		if (getDefinitionSpace().contains(FMT_NUMBER_DEFAULT)) {
			return getDefinitionSpace().resolve(FMT_NUMBER_DEFAULT, FormatterDefinition.class);
		}
		return new FormatterNumber("#,###.##");
	}

	private static Formatter obtainFormatterLocalDate() {
		if (getDefinitionSpace().contains(FMT_LOCAL_DATE_DEFAULT)) {
			return getDefinitionSpace().resolve(FMT_LOCAL_DATE_DEFAULT, FormatterDefinition.class);
		}
		return new FormatterDate("dd/MM/yyyy");
	}

	private static Formatter obtainFormatterInstant() {
		if (getDefinitionSpace().contains(FMT_INSTANT_DEFAULT)) {
			return getDefinitionSpace().resolve(FMT_INSTANT_DEFAULT, FormatterDefinition.class);
		}
		return new FormatterDate("dd/MM/yyyy HH:mm");
	}

	private static Formatter obtainFormatterString() {
		if (getDefinitionSpace().contains(FMT_STRING_DEFAULT)) {
			return getDefinitionSpace().resolve(FMT_STRING_DEFAULT, FormatterDefinition.class);
		}
		//Fonctionnement de base (pas de formatage)
		return new FormatterString(null);
	}

}
