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
package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;
import io.vertigo.lang.Assertion;

public class TestDefinitionprovider implements SimpleDefinitionProvider {

	@Inject
	public TestDefinitionprovider(@ParamValue("testParam") final String testParam) {
		Assertion.checkArgNotEmpty(testParam);

	}

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new SampleDefinition());
	}

	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		// we do nothing
	}

}
