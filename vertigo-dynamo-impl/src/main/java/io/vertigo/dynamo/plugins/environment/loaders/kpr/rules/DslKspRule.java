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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;

import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.lang.Assertion;

/**
 * règle de composition d'un fichier KSP.
 * Permet de parser un texte au format KSP.
 * Un  fichier KSP ne contient que
 *  - des déclarations de définition,
 *  - des déclarations de droits.
 *
 * Tout fichier ksp commence par une entête ou est précisé le nom du pacakage.
 *
 * @author pchretien
 */
public final class DslKspRule extends AbstractRule<Dummy, List<Object>> {
	private final DslDefinitionRepository dynamicModelrepository;

	/**
	 * Constructor.
	 * @param dynamicModelrepository Grammaire
	 */
	public DslKspRule(final DslDefinitionRepository dynamicModelrepository) {
		super(createMainRule(dynamicModelrepository.getGrammar()), "Ksp");
		this.dynamicModelrepository = dynamicModelrepository;
	}

	private static PegRule<List<Object>> createMainRule(final DslGrammar grammar) {
		Assertion.checkNotNull(grammar);
		//-----
		final PegRule<DslDefinition> definitionRule = new DslDynamicDefinitionRule("create", grammar);
		final PegRule<DslDefinition> templateRule = new DslDynamicDefinitionRule("alter", grammar);
		final PegRule<PegChoice> declarationChoiceRule = PegRules.choice(//"definition or template")
				definitionRule, //0
				templateRule //1
		);
		final PegRule<List<PegChoice>> declarationChoicesRule = PegRules.zeroOrMore(declarationChoiceRule, true);
		return PegRules.sequence(
				SPACES,
				new DslPackageDeclarationRule(), //1
				SPACES,
				declarationChoicesRule); //3
	}

	@Override
	protected Dummy handle(final List<Object> parsing) {
		final String packageName = (String) parsing.get(1);
		final List<PegChoice> declarationChoices = (List<PegChoice>) parsing.get(3);

		for (final PegChoice declarationChoice : declarationChoices) {
			//Tant qu'il y a du texte, il doit correspondre
			// - à des définitions qui appartiennent toutes au même package.
			// - à des gestion de droits.
			switch (declarationChoice.getChoiceIndex()) {
				case 0:
					//On positionne le Package
					final DslDefinition oldDynamicDefinition = (DslDefinition) declarationChoice.getValue();
					final DslDefinition newDynamicDefinition = DslDefinition.builder(oldDynamicDefinition.getName(), oldDynamicDefinition.getEntity())
							.withPackageName(packageName)
							.merge(oldDynamicDefinition)
							.build();
					handleDefinitionRule(newDynamicDefinition);
					break;
				case 1:
					handleTemplateRule((DslDefinition) declarationChoice.getValue());
					break;
				default:
					throw new IllegalArgumentException("case " + declarationChoice.getChoiceIndex() + " not implemented");
			}
		}
		return Dummy.INSTANCE;
	}

	private void handleTemplateRule(final DslDefinition dslDefinition) {
		dynamicModelrepository.addPartialDefinition(dslDefinition);
	}

	private void handleDefinitionRule(final DslDefinition dslDefinition) {
		dynamicModelrepository.addDefinition(dslDefinition);
	}
}
