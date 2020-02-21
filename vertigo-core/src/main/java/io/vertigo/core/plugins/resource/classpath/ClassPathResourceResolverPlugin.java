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
package io.vertigo.core.plugins.resource.classpath;

import java.net.URL;
import java.util.Optional;

import io.vertigo.core.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;

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
		Assertion.checkNotNull(resource);
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
