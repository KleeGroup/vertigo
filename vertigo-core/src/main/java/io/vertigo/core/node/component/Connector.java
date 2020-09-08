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
package io.vertigo.core.node.component;

/**
 * A connector is a particular core-component.
 * A connector bridge to dedicated library or product but with no purpose in mind.
 * A connector is only used by plugins to implement specific functions via the dedicated library.
 *
 * All connectors MUST BE thread safe.
 * Connectors are singletons.
 *
 * As a connector is a core-component, it can own component's behaviors such as Activeable.
 *
 * @author mlaroche
 */
public interface Connector<C> extends CoreComponent {

	String DEFAULT_CONNECTOR_NAME = "main";

	/**
	 * A connector might need to define it's name to be findable by plugin and to differentiate multiple instance of the same connector.
	 * For example, I know that my plugin needs a connection to the "secondary" database.
	 * "main" is the default but connector can have a different strategy, for example via a @ParamValue parameter in it's constuctor
	 * @return the name of the connector
	 */
	default String getName() {
		return DEFAULT_CONNECTOR_NAME;
	}

	C getClient();
}
