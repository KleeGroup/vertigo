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
package io.vertigo.dynamo.impl.collections.functions.filter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.dynamo.criteria.CriterionLimit;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.DateUtil;

/**
 * Parser des filtres utilisant une syntaxe définie.
 */
public final class DtListPatternFilterUtil {
	private static final String DATE_PATTERN = "dd/MM/yyyy";

	/**
	 * Pattern types : Range or Term.
	 */
	public enum FilterPattern {
		/** range. */
		Range("([a-z][a-zA-Z0-9]*):([\\[\\{\\]])(.*) TO (.*)([\\]\\}\\[])"), //[] : include, ][ or {} : exclude
		/** term. */
		Term("([a-z][a-zA-Z0-9]*):\"(.*)\"");

		private final Pattern pattern;

		FilterPattern(final String patternString) {
			pattern = Pattern.compile(patternString);
		}

		Pattern getPattern() {
			return pattern;
		}
	}

	/**
	 * Constructor.
	 */
	private DtListPatternFilterUtil() {
		//private constructor
	}

	static <D extends DtObject> Predicate<D> createDtListFilterForPattern(final FilterPattern filterPattern, final String[] parsedFilter, final DtDefinition dtDefinition) {
		Assertion.checkNotNull(filterPattern);
		Assertion.checkNotNull(parsedFilter);
		Assertion.checkNotNull(dtDefinition);
		//-----
		//Si on trouve un pattern, on passe sur du code spécifique
		final String fieldName = parsedFilter[1]; //attention parsedFilter[0] = filtre entier
		final DtField dtField = dtDefinition.getField(fieldName);
		final DataType dataType = dtField.getDomain().getDataType();

		switch (filterPattern) {
			case Range:
				return createDtListRangeFilter(parsedFilter, fieldName, dataType);
			case Term:
				return createDtListTermFilter(parsedFilter, fieldName, dataType);
			default:
				throw new VSystemException("La chaine de filtrage: {0} , ne respecte pas la syntaxe {1}.", parsedFilter[0], filterPattern.getPattern().pattern());
		}
	}

	/**
	 * Retourne les éléments parsés du filtre.
	 * index 0 : filtre d'origine.
	 * index 1 : nom du champs (par convention)
	 * ensuite dépend du pattern
	 * @param filterString Filter string to parse
	 * @param parsingPattern Pattern use to parse
	 * @return Resulting String array (Optional)
	 **/
	public static Optional<String[]> parseFilter(final String filterString, final Pattern parsingPattern) {
		Assertion.checkNotNull(filterString);
		Assertion.checkNotNull(parsingPattern);
		//-----
		final Matcher matcher = parsingPattern.matcher(filterString);
		if (!matcher.matches()) {
			return Optional.empty();
		}

		final int nbGroup = matcher.groupCount() + 1;
		final String[] groups = new String[nbGroup];
		for (int i = 0; i < nbGroup; i++) {
			groups[i] = matcher.group(i);
		}
		return Optional.of(groups);
	}

	private static <D extends DtObject> Predicate<D> createDtListTermFilter(final String[] parsedFilter, final String fieldName, final DataType dataType) {
		final Serializable filterValue = convertToValue(parsedFilter[2], dataType, false);
		final Predicate predicate;
		if (filterValue != null) {
			predicate = Criterions.isEqualTo(() -> fieldName, filterValue).toPredicate();
		} else {
			predicate = Criterions.isNotNull(() -> fieldName).toPredicate();
		}
		return predicate;
	}

	private static <D extends DtObject> Predicate<D> createDtListRangeFilter(
			final String[] parsedFilter,
			final String fieldName,
			final DataType dataType) {
		final boolean minIncluded = "[".equals(parsedFilter[2]);
		final Serializable minValue = convertToValue(parsedFilter[3], dataType, true);
		final Serializable maxValue = convertToValue(parsedFilter[4], dataType, true);
		final boolean maxIncluded = "]".equals(parsedFilter[5]);

		final CriterionLimit min = minIncluded ? CriterionLimit.ofIncluded(minValue) : CriterionLimit.ofExcluded(minValue);
		final CriterionLimit max = maxIncluded ? CriterionLimit.ofIncluded(maxValue) : CriterionLimit.ofExcluded(maxValue);
		final Predicate predicate = Criterions.isBetween(() -> fieldName, min, max).toPredicate();
		return predicate;
	}

	private static Serializable convertToValue(final String valueToConvert, final DataType dataType, final boolean acceptJoker) {
		final String stringValue = valueToConvert.trim();
		if (acceptJoker && "*".equals(stringValue) || "".equals(stringValue)) {
			return null;//pas de test
		}
		//--
		return valueOf(dataType, stringValue);
	}

	/** Same as Criterion. */
	private static Serializable valueOf(final DataType dataType, final String stringValue) {
		switch (dataType) {
			case Integer:
				return Integer.valueOf(stringValue);
			case Long:
				return Long.valueOf(stringValue);
			case BigDecimal:
				return new BigDecimal(stringValue);
			case Double:
				return Double.valueOf(stringValue);
			case LocalDate:
				return DateUtil.parseToLocalDate(stringValue, DATE_PATTERN);
			case Instant:
				return DateUtil.parseToInstant(stringValue, DATE_PATTERN);
			case String:
				return stringValue;
			case Boolean:
			case DataStream:
			default:
				throw new IllegalArgumentException("Type de données non comparable : " + dataType.name());
		}
	}
}
