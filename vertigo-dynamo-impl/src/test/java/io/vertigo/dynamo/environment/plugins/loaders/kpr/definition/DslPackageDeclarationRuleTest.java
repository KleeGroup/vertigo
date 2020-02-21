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
import io.vertigo.commons.peg.PegResult;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslPackageDeclarationRule;

public final class DslPackageDeclarationRuleTest {
	private static final DslPackageDeclarationRule PACKAGE_DECLARATION_RULE = new DslPackageDeclarationRule();

	@Test
	public void testExpression() throws PegNoMatchFoundException {
		final PegResult<String> result = PACKAGE_DECLARATION_RULE
				.parse("package io.vertigo  xxxx", 0);
		Assertions.assertEquals("io.vertigo", result.getValue());
		Assertions.assertEquals("package io.vertigo".length(), result.getIndex());
	}

	@Test
	public void testMalFormedExpression() {
		Assertions.assertThrows(Exception.class, () -> {
			final PegResult<String> result = PACKAGE_DECLARATION_RULE
					.parse("packageio.vertigo", 0);//<-- en exception is excpected here
			Assertions.assertNotNull(result);
		});
	}

	@Test
	public void testMalFormedExpression2() {
		Assertions.assertThrows(Exception.class, () -> {
			final PegResult<String> cursor = PACKAGE_DECLARATION_RULE
					.parse("  packageio.vertigo", 0);//<-- en exception is excpected here
			Assertions.assertNotNull(cursor);
		});
	}
}
