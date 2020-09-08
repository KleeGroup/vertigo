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
package io.vertigo.core.impl.param;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;

/**
 * This class implements the default paramManager.
 *
 * The strategy to access params is defined by a list of plugins.
 *
 * @author pchretien, npiedeloup
 */
public final class ParamManagerImpl implements ParamManager {
	private final List<ParamPlugin> paramPlugins;

	/**
	 * Constructor.
	 * @param paramPlugins the list of plugins
	 */
	@Inject
	public ParamManagerImpl(final List<ParamPlugin> paramPlugins) {
		Assertion.check().isNotNull(paramPlugins);
		//-----
		this.paramPlugins = paramPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public Param getParam(final String paramName) {
		return getOptionalParam(paramName)
				.orElseThrow(() -> new IllegalArgumentException("param '" + paramName + "' not found"));
	}

	@Override
	public Optional<Param> getOptionalParam(final String paramName) {
		Assertion.check().isNotBlank(paramName);
		//-----
		return paramPlugins
				.stream()
				.map(paramPlugin -> paramPlugin.getParam(paramName))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
}
