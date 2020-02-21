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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslDefinitionBodyRule;
import io.vertigo.lang.VSystemException;

public class DslDefinitionBodyRuleTest {
	private final DslDefinitionRepository dslDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	private static DslEntity find(final List<DslEntity> entities, final String entityName) {
		return entities.stream()
				.filter(entity -> entity.getName().equals(entityName))
				.findFirst()
				.orElseThrow(() -> new VSystemException("not found {0}", entityName));
	}

	@Test
	public void test1() throws PegNoMatchFoundException {
		final List<DslEntity> entities = dslDefinitionRepository.getGrammar().getEntities();

		final DslEntity entity = find(entities, "Formatter");

		final DslDefinitionBody definitionBody = new DslDefinitionBodyRule(entity)
				.parse("{ args : \"UPPER\" }", 0)
				.getValue();

		Assertions.assertEquals(1, definitionBody.getPropertyEntries().size());
	}

	//Exemple de test sur la déclaration d'un Domain
	//	create Domain DO_CODE_POSTAL (
	//			dataType : String;
	//			formatter : FMT_DEFAULT;
	//			constraint : {CK_CODE_POSTAL}
	//		)
	@Test
	public void test2() throws PegNoMatchFoundException {
		final List<DslEntity> entities = dslDefinitionRepository.getGrammar().getEntities();
		final DslEntity entity = find(entities, "Domain");

		final DslDefinitionBody definitionBody = new DslDefinitionBodyRule(entity)
				.parse("{ dataType : String ,  formatter : FmtDefault,  constraint : [ CkCodePostal ]    } ", 0)
				.getValue();

		Assertions.assertNotNull(definitionBody);
	}

	@Test
	public void testError() {
		final List<DslEntity> entities = dslDefinitionRepository.getGrammar().getEntities();
		final DslEntity entity = find(entities, "Domain");
		final String testValue = "{ dataType : String ,  formatter : FmtDefault,  constraint : [ CkCodePostal ] , maxLengh:\"true\"   } ";
		try {
			new DslDefinitionBodyRule(entity)
					.parse(testValue, 0);
			Assertions.fail();
		} catch (final PegNoMatchFoundException e) {
			//System.out.println(e.getFullMessage());
			Assertions.assertEquals(testValue.indexOf("maxLengh") + "maxLengh".length() - 1, e.getIndex());
		}
	}
}
