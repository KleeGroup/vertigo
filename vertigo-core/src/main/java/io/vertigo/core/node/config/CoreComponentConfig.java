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
package io.vertigo.core.node.config;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - proxy or pure component -no proxy-
 *  - a implemenation class (empty if proxy, required if not proxy)
 *  - an optional api class. (required if proxy)
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 */
public final class CoreComponentConfig {
	private enum Type {
		COMPONENT, PLUGIN, AMPLIFIER, CONNECTOR
	}

	private final Type type;
	private final String id;
	private final Optional<Class<? extends CoreComponent>> implClassOpt;
	private final Optional<Class<? extends CoreComponent>> apiClassOpt;
	private final Map<String, String> params;

	static CoreComponentConfig createComponent(final String id, final Optional<Class<? extends Component>> apiClassOpt, final Class<? extends Component> implClass, final List<Param> params) {
		final Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		final Optional<Class<? extends CoreComponent>> myApiClassOpt = Optional.ofNullable(apiClassOpt.orElse(null));
		return new CoreComponentConfig(Type.COMPONENT, id, myApiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createPlugin(final String id, final Class<? extends Plugin> implClass, final List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.empty();
		final Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		return new CoreComponentConfig(Type.PLUGIN, id, apiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createAmplifier(final String id, final Class<? extends Amplifier> apiClass, final List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.of(apiClass);
		final Optional<Class<? extends CoreComponent>> implClassOpt = Optional.empty();
		return new CoreComponentConfig(Type.AMPLIFIER, id, apiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createConnector(final String id, final Class<? extends Connector> implClass, final List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.empty();
		final Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		return new CoreComponentConfig(Type.CONNECTOR, id, apiClassOpt, implClassOpt, params);
	}

	/**
	 * Constructor.
	 * @param apiClassOpt api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	private CoreComponentConfig(
			final Type type,
			final String id,
			final Optional<Class<? extends CoreComponent>> apiClassOpt,
			final Optional<Class<? extends CoreComponent>> implClassOpt,
			final List<Param> params) {
		Assertion.check()
				.isNotNull(type)
				.isNotBlank(id)
				.isNotNull(apiClassOpt)
				.isNotNull(implClassOpt)
				.isNotNull(params);
		//--
		this.type = type;
		switch (type) {
			case AMPLIFIER:
				Assertion.check()
						.isTrue(implClassOpt.isEmpty(), "When an amplifier is declared there is no impl")
						.isTrue(apiClassOpt.isPresent(), "When an amplifier is declared, an api is required")
						.isTrue(Amplifier.class.isAssignableFrom(apiClassOpt.get()), "An amplifier must inherit Amplifier");
				break;
			case COMPONENT:
				Assertion.check()
						.when(apiClassOpt.isPresent(), () -> Assertion.check()
								.isTrue(CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class))
						.isTrue(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
				break;
			case CONNECTOR:
			case PLUGIN:
				Assertion.check()
						.when(apiClassOpt.isPresent(), () -> Assertion.check()
								.isTrue(CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class)
								.isTrue(CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class))
						.isTrue(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
				break;
			default:
				throw new IllegalStateException();
		}
		if (type == Type.AMPLIFIER) {
			Assertion.check()
					.isTrue(implClassOpt.isEmpty(), "When a proxy is declared there is no impl")
					.isTrue(apiClassOpt.isPresent(), "When a proxy is declared, an api is required")
					.isTrue(Amplifier.class.isAssignableFrom(apiClassOpt.get()), "An amplifier must inherit Amplifier");
		} else {
			Assertion.check()
					.isTrue(implClassOpt.isPresent(), "When a classic component -no proxy-  is declared, an impl is required")
					.isTrue(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class))
					.when(apiClassOpt.isPresent(), () -> Assertion.check()
							.isTrue(CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class));
		}
		//-----
		this.id = id;
		this.apiClassOpt = apiClassOpt;
		this.implClassOpt = implClassOpt;

		this.params = params
				.stream()
				.collect(Collectors.toMap(Param::getName, Param::getValue));
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends CoreComponent> getImplClass() {
		return implClassOpt
				.orElseThrow(() -> new NoSuchElementException("No impl class deined for this type of component :" + type));
	}

	/**
	 * @return api of the component
	 */
	public Optional<Class<? extends CoreComponent>> getApiClass() {
		return apiClassOpt;
	}

	/**
	 * @return id of the component
	 */
	public String getId() {
		return id;
	}

	/*
	 * Return type of the Core Component
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return if the component is an amplifier
	 */
	public boolean isAmplifier() {
		return type == Type.AMPLIFIER;
	}

	/**
	 * @return if the component is a plugin
	 */
	public boolean isPlugin() {
		return type == Type.PLUGIN;
	}

	/**
	 * @return if the component is a connector
	 */
	public boolean isConnector() {
		return type == Type.CONNECTOR;
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}
}
