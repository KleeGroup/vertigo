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
package io.vertigo.core.resource;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Standard implementation for the resourceManager.
 * The strategy to access resources is defined by plugins.
 *
 * So, you can extend the capabilities and define your own plugin to be able to access your own resources wherever they are.
 *
 * @author pchretien
 */
public final class ResourceManagerImpl implements ResourceManager {
	private final List<ResourceResolverPlugin> resourceResolverPlugins;

	/**
	 * Constructor.
	 * @param resourceResolverPlugins List of plugins  which resolve the resources.
	 */
	@Inject
	public ResourceManagerImpl(final List<ResourceResolverPlugin> resourceResolverPlugins) {
		Assertion.checkNotNull(resourceResolverPlugins);
		//-----
		this.resourceResolverPlugins = resourceResolverPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public URL resolve(final String resource) {
		for (final ResourceResolverPlugin resourceResolver : resourceResolverPlugins) {
			final Optional<URL> url = resourceResolver.resolve(resource);
			if (url.isPresent()) {
				return url.get();
			}
		}
		/* We have not found any resolver for this resource */
		throw new VSystemException("Resource '{0}' not found", resource);
	}
}
