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
package io.vertigo.core.plugins.resource.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import io.vertigo.core.impl.resource.ResourceResolverPlugin;
import io.vertigo.core.lang.Assertion;

/**
 * Résolution des URL liées à l'emplacement local.
 *
 * @author prahmoune
 */
public final class LocalResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	@Override
	public Optional<URL> resolve(final String resource) {
		Assertion.check().isNotNull(resource);
		//-----
		final File file = new File(resource);
		if (file.exists() && file.canRead()) {
			try {
				return Optional.of(file.toURI().toURL());
			} catch (final MalformedURLException e) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}
}
