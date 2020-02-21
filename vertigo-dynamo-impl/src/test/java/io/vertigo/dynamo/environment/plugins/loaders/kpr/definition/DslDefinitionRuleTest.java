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
package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslDynamicDefinitionRule;

public class DslDefinitionRuleTest {
	private final DslDefinitionRepository dslDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	@Test
	public void test1() throws PegNoMatchFoundException {
		final DslDefinition dslDefinition = new DslDynamicDefinitionRule("create", dslDefinitionRepository.getGrammar())
				.parse("create Formatter FmtTest { args : \"UPPER\" }", 0)
				.getValue();

		Assertions.assertNotNull(dslDefinition);
	}

	//Exemple de test sur la déclaration d'un Domain
	//	create Domain DO_CODE_POSTAL (
	//			dataType : String;
	//			formatter : FMT_DEFAULT;
	//			constraint : {CK_CODE_POSTAL}
	//		)
	@Test
	public void test2() throws PegNoMatchFoundException {
		final DslDefinition dslDefinition = new DslDynamicDefinitionRule("create", dslDefinitionRepository.getGrammar())
				.parse("create Domain DoCodePostal { dataType : String ,  formatter:FmtDefault, constraint : [ CkCodePostal ]   } ", 0)
				.getValue();
		Assertions.assertNotNull(dslDefinition);
	}

	@Test
	public void testTemplate() throws PegNoMatchFoundException {
		new DslDynamicDefinitionRule("alter", dslDefinitionRepository.getGrammar())
				.parse("alter Formatter FmtDefault {args : \"UPPER\"}", 0);
	}
}
