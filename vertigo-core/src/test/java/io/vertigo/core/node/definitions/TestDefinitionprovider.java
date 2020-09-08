/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.definitions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.config.DefinitionResourceConfig;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.node.definitions.DefinitionSpaceTest.SampleDefinition;
import io.vertigo.core.param.ParamValue;

public class TestDefinitionprovider implements SimpleDefinitionProvider {

	@Inject
	public TestDefinitionprovider(@ParamValue("testParam") final String testParam) {
		Assertion.check().isNotBlank(testParam);
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
