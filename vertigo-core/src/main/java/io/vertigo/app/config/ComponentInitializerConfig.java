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

import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.lang.Assertion;

/**
 * Configuration of an initializer.
 * @author pchretien
 *
 */
public final class ComponentInitializerConfig {
	private final Class<? extends ComponentInitializer> componentInitializerClass;

	/**
	 * @param componentInitializerClass Class de l'initialiseur du composant
	 */
	ComponentInitializerConfig(final Class<? extends ComponentInitializer> componentInitializerClass) {
		Assertion.checkNotNull(componentInitializerClass);
		//-----
		this.componentInitializerClass = componentInitializerClass;
	}

	/**
	 * @return Classe d'initialisation du composant. (Nullable)
	 */
	public Class<? extends ComponentInitializer> getInitializerClass() {
		return componentInitializerClass;
	}
}
