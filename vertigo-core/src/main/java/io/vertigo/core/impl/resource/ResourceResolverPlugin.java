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
package io.vertigo.core.impl.resource;

import java.net.URL;
import java.util.Optional;

import io.vertigo.core.node.component.Plugin;

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
