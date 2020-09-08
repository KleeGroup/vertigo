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
package io.vertigo.core.util;

import java.util.Collections;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.loader.ComponentSpaceLoader;

public final class InjectorUtil {

	private InjectorUtil() {
		// util
	}

	/**
	 * Creates an new object instance of the given class and inject dependencies using the current App ComponentSpace as container.
	 * This created object is not registered in the ComponantSpace.
	 * Therefore the clazz cannot implement the interface Activeable because the lifecycle of this component is not handled by Vertigo.
	 * @param clazz the clazz of the object your want to create with it's member injected.
	 * @return the newly created object.
	 */
	public static <T> T newInstance(final Class<T> clazz) {
		Assertion.check()
				.isNotNull(clazz)
				.isFalse(clazz.isAssignableFrom(Activeable.class), " {0} is an Activeable component and must be registred in the NodeConfig for creation at the application startup", clazz);
		// ---
		return ComponentSpaceLoader.createInstance(clazz, Node.getNode().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

	/**
	 * Inject dependencies in the instance using the current App ComponentSpace as container.
	 * @param instance the object your want to get it's member injected.
	 */
	public static void injectMembers(final Object instance) {
		Assertion.check()
				.isNotNull(instance);
		//-----
		ComponentSpaceLoader.injectMembers(instance, Node.getNode().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

}
