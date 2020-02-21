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
package io.vertigo.dynamox.domain.constraint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;

/**
 * Exemple de contrainte utilisant une expression régulière.
 *
 * @author  pchretien
 */
public final class ConstraintRegex implements Constraint<String, String> {
	private final Pattern pattern;

	/**
	 * @param regex Expression régulière
	 */
	public ConstraintRegex(final String regex) {
		pattern = Pattern.compile(regex);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final String value) {
		if (value == null) {
			return true;
		}
		final String input = value;
		final Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return MessageText.of(Resources.DYNAMO_CONSTRAINT_REGEXP, pattern.pattern());
	}

	/** {@inheritDoc} */
	@Override
	public Property getProperty() {
		return DtProperty.REGEX;
	}

	/** {@inheritDoc} */
	@Override
	public String getPropertyValue() {
		return pattern.pattern();
	}

	/**
	 * @return Expression régulière testée par la contrainte
	 */
	public String getRegex() {
		//regex ==>
		return pattern.pattern();
	}
}
