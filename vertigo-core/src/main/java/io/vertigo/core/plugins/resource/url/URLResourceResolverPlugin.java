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
package io.vertigo.core.plugins.resource.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import io.vertigo.core.impl.resource.ResourceResolverPlugin;
import io.vertigo.core.lang.Assertion;

/**
 * Résolution des URL par le standard java.net.URL.
 *
 * @author npiedeloup
 */
public final class URLResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	@Override
	public Optional<URL> resolve(final String resource) {
		Assertion.check().isNotNull(resource);
		//-----
		try {
			final URL url = new URL(resource);
			return isUrlAvailable(url) ? Optional.of(url) : Optional.empty();
		} catch (final MalformedURLException e) {
			return Optional.empty();
		}
	}

	private static boolean isUrlAvailable(final URL url) {
		try (InputStream is = url.openStream()) {
			return is.read() > 0;
		} catch (final IOException e) {
			return false;
		}
	}
}
