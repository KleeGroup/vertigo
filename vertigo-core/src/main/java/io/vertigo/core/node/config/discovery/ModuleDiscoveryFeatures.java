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
package io.vertigo.core.node.config.discovery;

import io.vertigo.core.node.config.Features;

/**
 * An abstract Feature with no configuration for discovering and registering components in a package tree.
 * Usage :
 *  - Extends this class
 *  - Provide a module name
 *  - Provide the package prefix to scan for components
 *  - Register this feature in your app's configuration (YAML or Java)
 * @author mlaroche
 *
 */
public abstract class ModuleDiscoveryFeatures<F> extends Features<F> {

	protected ModuleDiscoveryFeatures(final String name) {
		super(name);
	}

	protected abstract String getPackageRoot();

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//DAO + PAO + Services + WebServices
		ComponentDiscovery.registerComponents(getPackageRoot(), getModuleConfigBuilder());
	}

}
