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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.PAIR_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.WORD;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslWordsRule.WORDS;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.lang.Assertion;

/**
 * Règle de déclaration d'une champ référenéant une listes de clés.
 * @author pchretien
 */
public final class DslDefinitionEntryRule extends AbstractRule<DslDefinitionEntry, List<Object>> {

	/**
	 * Constructor.
	 * @param fieldNames List of field names
	 */
	public DslDefinitionEntryRule(final List<String> fieldNames) {
		super(createMainRule(fieldNames));
	}

	private static PegRule<List<Object>> createMainRule(final List<String> fieldNames) {
		Assertion.checkNotNull(fieldNames);
		//-----
		final List<PegRule<?>> fieldNamesRules = fieldNames.stream()
				.map(PegRules::term)
				.collect(Collectors.toList());
		//-----
		return PegRules.sequence(//"DefinitionKey"
				PegRules.choice(fieldNamesRules), //0
				SPACES,
				PAIR_SEPARATOR,
				SPACES,
				PegRules.choice(WORD, WORDS), //4
				SPACES,
				PegRules.optional(DslSyntaxRules.OBJECT_SEPARATOR));
	}

	@Override
	protected DslDefinitionEntry handle(final List<Object> parsing) {
		final String fieldName = (String) ((PegChoice) parsing.get(0)).getValue();
		final List<String> definitionKeys;

		final PegChoice definitionChoice = (PegChoice) parsing.get(4);
		switch (definitionChoice.getChoiceIndex()) {
			case 1:
				//Déclaration d'une liste de définitions identifiée par leurs clés
				definitionKeys = (List<String>) definitionChoice.getValue();
				break;
			case 0:
				//Déclaration d'une définition identifiée par sa clé
				final String value = (String) definitionChoice.getValue();
				definitionKeys = java.util.Collections.singletonList(value);
				break;
			default:
				throw new IllegalStateException();
		}
		return new DslDefinitionEntry(fieldName, definitionKeys);
	}
}
