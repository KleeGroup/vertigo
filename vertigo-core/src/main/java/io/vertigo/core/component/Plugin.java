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
package io.vertigo.core.component;

/**
 * A plugin is a particular component.
 * A plugin is not referenced in the ComponentSpace.
 * A plugin is usefull to encapsulate a specific set of operations.
 * A plugin is often used as a strategy pattern to make an operation interchangeable without changing the component.
 *
 * All plugins MUST BE thread safe.
 * Plugins are singletons.
 *
 * As a plugin is a component, it can own component's behaviors such as Activeable.
 *
 * @author pchretien
 */
public interface Plugin extends Component {
	//
}
