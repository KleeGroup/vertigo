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

/**
 * Contrainte pour gérer la longueur des chaines de caractères.
 *
 * @author  plepaisant
 */
public final class ConstraintStringLength extends AbstractConstraintLength<String> {

	/**
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de caractères maximum authorisés sur la chaine de caractères.
	 */
	public ConstraintStringLength(final String args) {
		super(args);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final String value) {
		if (value == null) {
			return true;
		}
		return value.length() <= getMaxLength();
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return MessageText.of(Resources.DYNAMO_CONSTRAINT_STRINGLENGTH_EXCEEDED, Integer.toString(getMaxLength()));
	}
}
