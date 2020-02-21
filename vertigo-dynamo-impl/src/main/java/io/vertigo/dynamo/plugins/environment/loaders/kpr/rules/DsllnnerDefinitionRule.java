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

import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionBuilder;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;
import io.vertigo.lang.Assertion;

final class DslInnerDefinitionRule extends AbstractRule<DslDefinitionEntry, List<Object>> {
	private final String entityName;
	private final DslEntity entity;

	DslInnerDefinitionRule(final String entityName, final DslEntity entity) {
		super(createMainRule(entityName, entity), entityName);
		this.entityName = entityName;
		this.entity = entity;
	}

	private static PegRule<List<Object>> createMainRule(final String entityName, final DslEntity entity) {
		Assertion.checkArgNotEmpty(entityName);
		Assertion.checkNotNull(entity);
		//-----
		final DslDefinitionBodyRule definitionBodyRule = new DslDefinitionBodyRule(entity);
		return PegRules.sequence(//"InnerDefinition"
				PegRules.term(entityName),
				DslSyntaxRules.SPACES,
				DslSyntaxRules.WORD, //2
				DslSyntaxRules.SPACES,
				definitionBodyRule, //4
				DslSyntaxRules.SPACES,
				PegRules.optional(DslSyntaxRules.OBJECT_SEPARATOR));
	}

	@Override
	protected DslDefinitionEntry handle(final List<Object> parsing) {
		//Dans le cas des sous définition :: field [PRD_XXX]

		final String definitionName = (String) parsing.get(2);
		final DslDefinitionBody definitionBody = (DslDefinitionBody) parsing.get(4);

		final DslDefinitionBuilder dslDefinitionBuilder = DslDefinition.builder(definitionName, entity);
		populateDefinition(definitionBody, dslDefinitionBuilder);

		//---
		return new DslDefinitionEntry(entityName, dslDefinitionBuilder.build());
	}

	/**
	 * Peuple la définition à partir des éléments trouvés.
	 */
	private static void populateDefinition(final DslDefinitionBody definitionBody, final DslDefinitionBuilder dslDefinitionBuilder) {
		for (final DslDefinitionEntry fieldDefinitionEntry : definitionBody.getDefinitionEntries()) {
			//-----
			// 1.On vérifie que le champ existe pour la metaDefinition
			// et qu'elle n'est pas déjà enregistrée sur l'objet.
			//-----
			if (fieldDefinitionEntry.containsDefinition()) {
				// On ajoute la définition par sa valeur.
				dslDefinitionBuilder.addChildDefinition(fieldDefinitionEntry.getFieldName(), fieldDefinitionEntry.getDefinition());
			} else {
				// On ajoute les définitions par leur clé.
				dslDefinitionBuilder.addAllDefinitionLinks(fieldDefinitionEntry.getFieldName(), fieldDefinitionEntry.getDefinitionNames());
			}
		}
		for (final DslPropertyEntry dslPropertyEntry : definitionBody.getPropertyEntries()) {
			//			// On vérifie que la propriété est enregistrée sur la metaDefinition
			//			Assertion.precondition(definition.getEntity().getPropertySet().contains(fieldPropertyEntry.getProperty()), "Propriété {0} non enregistré sur {1}",
			//					fieldPropertyEntry.getProperty(), definition.getEntity().getName());
			//-----
			final Object value = readProperty(dslDefinitionBuilder.getEntity(), dslPropertyEntry);
			dslDefinitionBuilder.addPropertyValue(dslPropertyEntry.getPropertyName(), value);
		}
	}

	/**
	 * Retourne la valeur typée en fonction de son expression sous forme de String
	 * L'expression est celle utilisée dans le fichier xml/ksp.
	 * Cette méthode n'a pas besoin d'être optimisée elle est appelée au démarrage uniquement.
	 * @return J Valeur typée de la propriété
	 */
	private static Object readProperty(final DslEntity entity, final DslPropertyEntry dslPropertyEntry) {
		Assertion.checkNotNull(entity);
		Assertion.checkNotNull(dslPropertyEntry);
		//-----
		return entity.getPropertyType(dslPropertyEntry.getPropertyName()).cast(dslPropertyEntry.getPropertyValueAsString());
	}

}
