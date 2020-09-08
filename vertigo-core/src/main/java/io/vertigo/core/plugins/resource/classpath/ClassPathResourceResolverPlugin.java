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
package io.vertigo.core.plugins.resource.classpath;

import java.net.URL;
import java.util.Optional;

import io.vertigo.core.impl.resource.ResourceResolverPlugin;
import io.vertigo.core.lang.Assertion;

/**
 * Résolution des URL liées au classPath.
 * Cette résolution est en absolue.
 *
 * @author prahmoune
 */
public final class ClassPathResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	@Override
	public Optional<URL> resolve(final String resource) {
		Assertion.check().isNotNull(resource);
		//-----
		try {
			//le getClassLoader permet de se mettre en absolue (getClass().getRessource serait relatif)
			final URL url = getClassLoader().getResource(resource);
			return Optional.ofNullable(url);
		} catch (final RuntimeException e) { //if Ressource name is invalid it should throw exception
			return Optional.empty();
		}
	}

	private static ClassLoader getClassLoader() {
		//On récupère le classLoader courant (celui qui a créé le thread).
		return Thread.currentThread().getContextClassLoader();
	}
}
