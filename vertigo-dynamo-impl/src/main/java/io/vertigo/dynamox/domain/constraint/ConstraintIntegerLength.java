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
import io.vertigo.lang.Assertion;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un Integer comprenant au maximum le nombre de chiffres précisé à la construction (nombres de digits)</li>
 * <li>soit null</li>
 * </ul><br>
 * On rappelle que le maximum d'un type Integer est compris entre 1O^9 et 10^10 <br>
 * On conseille donc d'utiliser 10^9 comme structure de stockage max en BDD : donc number(9) <br>
 * Si vous souhaitez flirter avec les 10^10 alors n'utilisez pas de contraintes.
 *
 * @author pchretien
 */
public final class ConstraintIntegerLength extends AbstractConstraintLength<Integer> {
	/**
	 * Borne maximale au sens strict de Integer (= 10 puissance maxLength)
	 */
	private final int maxValue;

	/**
	 * Borne minimale au sens strict de Integer (= - maxValue)
	 */
	private final int minValue;

	/**
	 * Constructeur nécessaire pour le ksp.
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le Integer.
	 */
	public ConstraintIntegerLength(final String args) {
		super(args);
		//-----
		Assertion.checkArgument(getMaxLength() < 10, "Longueur max doit être strictement inférieure à 10");
		//-----
		int tmpMaxValue = 1;
		tmpMaxValue = 1;
		for (int i = 0; i < getMaxLength(); i++) {
			tmpMaxValue *= 10;
		}
		maxValue = tmpMaxValue;
		minValue = -tmpMaxValue;
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final Integer value) {
		if (value == null) {
			return true;
		}
		final int i = value;
		return i > minValue && i < maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return MessageText.of(Resources.DYNAMO_CONSTRAINT_INTEGERLENGTH_EXCEEDED, minValue, maxValue);
	}
}
