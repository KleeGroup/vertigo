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
import java.util.Optional;

import io.vertigo.core.component.Plugin;

/**
 * Résout une ressource en fournissant son URL.
 * @author prahmoune
 */
public interface ResourceResolverPlugin extends Plugin {
	/**
	 * Retourne une URL à partir de sa représentation 'chaîne de caractères'
	 * @param resource Url de la ressource(chaîne de caractères)
	 * @return URL associée à la ressource
	 */
	Optional<URL> resolve(String resource);
}
