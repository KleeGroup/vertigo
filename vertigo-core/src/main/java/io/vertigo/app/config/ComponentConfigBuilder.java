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
package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.component.Component;
import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class allows to configure a component step by step.
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfigBuilder implements Builder<ComponentConfig> {
	private String myId;
	private final boolean proxy;
	private Class<? extends Component> myApiClass;
	private Class<? extends Component> myImplClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor of a component config
	 * @param proxy if the component is a proxy
	 */
	ComponentConfigBuilder(final boolean proxy) {
		this.proxy = proxy;
	}

	/**
	 * @param implClass the impl class of the component
	 * @return this builder
	 */
	public ComponentConfigBuilder withImpl(final Class<? extends Component> implClass) {
		Assertion.checkNotNull(implClass);
		//-----
		myImplClass = implClass;
		return this;
	}

	/**
	 * @param apiClass the apiClass for the component
	 * @return this builder
	 */
	public ComponentConfigBuilder withApi(final Class<? extends Component> apiClass) {
		Assertion.checkNotNull(apiClass);
		//-----
		myApiClass = apiClass;
		return this;
	}

	/**
	 * Adds a list of params to this component config.
	 * @param params the list of params
	 * @return this builder
	 */
	public ComponentConfigBuilder addParams(final List<Param> params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(params);
		return this;
	}

	/**
	 * Adds a list of params to this component config.
	 * @param params the list of params
	 * @return this builder
	 */
	public ComponentConfigBuilder addParams(final Param[] params) {
		Assertion.checkNotNull(params);
		//-----
		return addParams(Arrays.asList(params));
	}

	/**
	 * Adds a param to this component config.
	 * @param param the param
	 * @return this builder
	 */
	public ComponentConfigBuilder addParam(final Param param) {
		Assertion.checkNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	/**
	 * Specifies the id to be used (otherwise an id will be chosen by convention) see build method.
	 * @param id the id to use
	 * @return this builder
	 */
	public ComponentConfigBuilder withId(final String id) {
		Assertion.checkArgNotEmpty(id);
		//---
		myId = id;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		final Optional<Class<? extends Component>> apiClassOpt = Optional.ofNullable(myApiClass);
		final Optional<Class<? extends Component>> implClassOpt = Optional.ofNullable(myImplClass);
		if (myId == null) {
			if (proxy) {
				//if proxy then apiClass is required
				myId = DIAnnotationUtil.buildId(apiClassOpt.get());
			} else {
				//if no proxy then implClass is required
				//By convention the component id is the simpleName of the api or the impl
				myId = DIAnnotationUtil.buildId(apiClassOpt.orElseGet(implClassOpt::get));
			}
		}
		return new ComponentConfig(myId, proxy, apiClassOpt, implClassOpt, myParams);
	}
}
