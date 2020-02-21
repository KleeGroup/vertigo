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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.vertigo.core.component.aop.Aspect;

/**
 * Create proxy-reference from component's instance.
 * Proxy reference implements aspects (AOP).
 *
 * @author pchretien
 */
public interface AopPlugin extends Plugin {

	/**
	 * Create a proxy-reference.
	 *
	 * @param instance Component's instance
	 * @param joinPoints List of joinPoints
	 * @return  Proxy-Reference
	 */
	<C extends Component> C wrap(final C instance, Map<Method, List<Aspect>> joinPoints);

	/**
	 * Unwrap the proxy
	 * @param component the component to unwrap
	 * @return the underlying object
	 */
	<C extends Component> C unwrap(final C component);
}
