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

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.lang.Assertion;

/**
 * Contrainte sur la valeur minimale d'un nombre.
 * arguments = valeur minimale.
 * @author npiedeloup
 */
public final class ConstraintNumberMinimum implements Constraint<Number, Number> {
	private final double minValue;

	/**
	 * Constructor.
	 * @param args the minimum value
	 */
	public ConstraintNumberMinimum(final String args) {
		Assertion.checkArgument(args != null && args.length() > 0, "Vous devez préciser la valeur minimum comme argument de ConstraintNumberMinimum");
		//-----
		minValue = Double.parseDouble(args);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final Number value) {
		if (value == null) {
			return true;
		}
		return value.doubleValue() >= minValue;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return MessageText.of(Resources.DYNAMO_CONSTRAINT_NUMBER_MINIMUM, minValue);
	}

	/** {@inheritDoc} */
	@Override
	public Property getProperty() {
		return DtProperty.MIN_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public Number getPropertyValue() {
		return minValue;
	}
}
