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
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.PROPERTY_VALUE;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.QUOTATION_MARK;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * règle de déclaration d'une affectation de propriété.
 * Dans la mesure ou l'on récupère un couple propriété, valeur on apparente
 * cela à une Entry (voir api Map).
 *
 * La propriété doit exister.
 * Syntaxe : nomPropriété: "valeur";
 * Exemple : maxLength:"10";
 *
 * @author pchretien
 */
public final class DslPropertyDeclarationRule extends AbstractRule<DslPropertyEntry, List<Object>> {
	private final Map<String, String> entityProperties;

	/**
	 * <propertyName> : "<propertyvalue>";
	 */
	public DslPropertyDeclarationRule(final Set<String> entityPropertyNames) {
		super(createMainRule(entityPropertyNames));
		Assertion.checkNotNull(entityPropertyNames);
		//-----
		entityProperties = new HashMap<>();
		for (final String entityPropertyName : entityPropertyNames) {
			final String propertyName = StringUtil.constToLowerCamelCase(entityPropertyName);
			entityProperties.put(propertyName, entityPropertyName);
		}
	}

	private static PegRule<List<Object>> createMainRule(final Set<String> entityPropertyNames) {
		final List<PegRule<?>> propertyNamesRules = new ArrayList<>();
		for (final String entityPropertyName : entityPropertyNames) {
			propertyNamesRules.add(PegRules.term(StringUtil.constToLowerCamelCase(entityPropertyName)));
		}

		return PegRules.sequence(
				PegRules.choice(propertyNamesRules),
				SPACES,
				PAIR_SEPARATOR,
				SPACES,
				QUOTATION_MARK,
				PROPERTY_VALUE, //5
				QUOTATION_MARK,
				SPACES,
				PegRules.optional(DslSyntaxRules.OBJECT_SEPARATOR));
	}

	@Override
	protected DslPropertyEntry handle(final List<Object> parsing) {
		final String propertyName = (String) ((PegChoice) parsing.get(0)).getValue();
		final String propertyValue = (String) parsing.get(5);
		return new DslPropertyEntry(entityProperties.get(propertyName), propertyValue);
	}
}
