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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.ARRAY_END;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.ARRAY_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.ARRAY_START;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.WORD;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * règle de composition d'une déclaration de liste de mots.
 * {mot1, mot2, mot3}
 * @author pchretien
 */
public final class DslWordsRule extends AbstractRule<List<String>, PegChoice> {

	// 	{ }
	private static final PegRule<List<Object>> EMPTY_LIST = PegRules.sequence(//Liste vide
			ARRAY_START,
			SPACES,
			ARRAY_END);

	// , XXXX
	private static final PegRule<List<List<Object>>> MANY_WORDS = PegRules.zeroOrMore(
			PegRules.sequence(//"mot"
					ARRAY_SEPARATOR,
					SPACES,
					WORD //2
			), false);

	//{ XXXXX (,XXXX)+ }
	private static final PegRule<List<Object>> NON_EMPTY_LIST = PegRules.sequence(//"Liste non vide"
			ARRAY_START,
			SPACES,
			WORD, //2
			MANY_WORDS, // 3
			SPACES,
			ARRAY_END);
	static final PegRule<List<String>> WORDS = new DslWordsRule();

	public DslWordsRule() {
		super(createMainRule(), "Words");
	}

	// {} | { XXXXX (,XXXX)+ }
	private static PegRule<PegChoice> createMainRule() {
		return PegRules.choice(//"liste vide ou non"
				EMPTY_LIST, //0
				NON_EMPTY_LIST);//1
	}

	@Override
	protected List<String> handle(final PegChoice parsing) {
		final List<String> words = new ArrayList<>();
		//---
		switch (parsing.getChoiceIndex()) {
			case 0: //liste vide on s'arrète
				break;
			case 1: //liste non vide on continue
				//On récupère le prmier mot qui est obligatoire.
				final List<Object> list = (List<Object>) parsing.getValue();
				words.add((String) list.get(2));
				//On récupère le produit de la règle many
				final List<List<Object>> many = (List<List<Object>>) list.get(3);
				for (final List<Object> row : many) {
					words.add((String) row.get(2));
				}
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getChoiceIndex() + " not implemented");
		}
		return words;
	}
}
