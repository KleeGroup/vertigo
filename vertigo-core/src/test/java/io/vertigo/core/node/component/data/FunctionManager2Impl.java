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
package io.vertigo.core.node.component.data;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;

public final class FunctionManager2Impl implements FunctionManager {
	private final List<FunctionPlugin> functionPlugins;

	@Inject
	public FunctionManager2Impl(final List<FunctionPlugin> functionPlugins) {
		Assertion.check().isNotNull(functionPlugins);
		//-----
		this.functionPlugins = functionPlugins;
	}

	@Override
	public int compute(final String functionName, final int x) {
		return getFunctionPlugin(functionName).compute(x);
	}

	@Override
	public int computeAll(final int x) {
		int result = x;
		for (final FunctionPlugin functionPlugin : functionPlugins) {
			result = functionPlugin.compute(result);
		}
		return result;
	}

	private FunctionPlugin getFunctionPlugin(final String functionName) {
		for (final FunctionPlugin functionPlugin : functionPlugins) {
			if (functionName.equals(functionPlugin.getName())) {
				return functionPlugin;
			}
		}
		throw new IllegalArgumentException("FunctionPlugin '" + functionName + "' not found");
	}
}
