/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;

/**
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 * @param <B> the type of the parent builder
 */
public final class ComponentConfigBuilder<B extends Builder> implements Builder<ComponentConfig> {
	//Par convention l'id du composant manager est le simpleName de la classe de l'api ou de l'impl.
	private final B parentConfigBuilder;
	private final Optional<Class<? extends Component>> apiClass;
	private final Class<? extends Component> implClass;
	private final Map<String, String> myParams = new HashMap<>();

	ComponentConfigBuilder(final B parentConfigBuilder, final Optional<Class<? extends Component>> apiClass, final Class<? extends Component> implClass) {
		Assertion.checkNotNull(parentConfigBuilder);
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		//-----
		this.parentConfigBuilder = parentConfigBuilder;
		this.apiClass = apiClass;
		this.implClass = implClass;
	}

	/**
	 * Add a param to this component config.
	 * @param paramName Name of the param
	 * @param paramValue Value of the param
	 * @return this builder
	 */
	public ComponentConfigBuilder<B> addParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName);
		//paramValue can be null
		//-----
		if (paramValue != null) {
			myParams.put(paramName, paramValue);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		final String id = apiClass.isPresent() ? DIAnnotationUtil.buildId(apiClass.get()) : DIAnnotationUtil.buildId(implClass);
		return new ComponentConfig(id, apiClass, implClass, myParams);
	}

	/**
	 * Close this component config and returns to the parent config.
	 * @return the builder of the parent config
	 */
	public B endComponent() {
		return parentConfigBuilder;
	}
}
