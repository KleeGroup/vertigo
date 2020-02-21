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
package io.vertigo.commons.peg;

import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.lang.Assertion;

/**
 * Enlève les blancs et les commentaires
 * Cette règle ne plante jamais -sauf si un blanc obligatoire n'est pas présent-
 * mais permet de faire avancer l'index.
 * @author pchretien
 */
final class PegWhiteSpaceRule implements PegRule<Dummy> {
	private final PegRule<String> rule;
	private final String expression;

	/**
	 * Constructor.
	 * @param blanks Caractères "blancs" et commentaires.
	 */
	PegWhiteSpaceRule(final String blanks) {
		super();
		Assertion.checkNotNull(blanks);
		//-----
		rule = PegRules.word(true, blanks, PegWordRule.Mode.ACCEPT, "_");
		expression = rule.getExpression();
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression; // _ by convention
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<Dummy> parse(final String text, final int start) throws PegNoMatchFoundException {
		int lastIndex;
		int index = start;
		index = rule
				.parse(text, index)
				.getIndex();

		//Suppression des commentaires  /*xxxxxxxxxxxxxxx*/
		while (text.length() > index + 2 && "/*".equals(text.substring(index, index + 2))) {
			index += 2;
			lastIndex = index;
			index = text.indexOf("*/", index);
			if (index < 0) {
				throw new PegNoMatchFoundException(text, lastIndex, null, "Fermeture des commentaires */ non trouvée");
			}
			index += 2;
			//On supprime les blancs
			index = rule.parse(text, index).getIndex();
		}
		return new PegResult<>(index, Dummy.INSTANCE);
	}
}
