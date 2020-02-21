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
package io.vertigo.core.component.loader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.core.component.Container;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;

/**
 * This container contains params initialized with String.
 * Method getUnusedKeys allows to identify 'phantom' params.
 *
 * @author pchretien
 */
final class ComponentParamsContainer implements Container {
	private final Optional<ParamManager> paramManagerOpt;
	private final Map<String, String> params;
	private final Set<String> unusedKeys;

	ComponentParamsContainer(final Optional<ParamManager> paramManagerOpt, final Map<String, String> params) {
		Assertion.checkNotNull(paramManagerOpt);
		Assertion.checkNotNull(params);
		//-----
		this.paramManagerOpt = paramManagerOpt;
		this.params = params;
		unusedKeys = new HashSet<>(params.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkNotNull(id);
		//-----
		return params.containsKey(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkState(params.containsKey(id), "param '{0}' of type '{1}' has not be registered.", id, clazz.getSimpleName());
		//-----
		unusedKeys.remove(id);
		return getParam(id).getValue(clazz);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(params.keySet());
	}

	/**
	 * Récupération d'un paramètre typé par son nom.
	 * @param paramName Nom du paramètre
	 * @return Valeur sous forme texte du paramètre
	 */
	private Param getParam(final String paramName) {
		Assertion.checkNotNull(paramName);
		//-----
		final String paramValue = params.get(paramName);
		if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return paramManagerOpt.orElseThrow(() -> new IllegalArgumentException("config is not allowed here"))
					.getParam(property);
		}
		return Param.of(paramName, paramValue);
	}

	/*
	 * @return Keys that are not used, allows to identify phantom keys during injection.
	 */
	Set<String> getUnusedKeys() {
		return unusedKeys;
	}
}
