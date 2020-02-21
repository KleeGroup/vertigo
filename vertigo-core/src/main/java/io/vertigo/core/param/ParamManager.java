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
package io.vertigo.core.param;

import java.util.Optional;

import io.vertigo.core.component.Manager;

/**
 * Interface du gestionnaire de la configuration applicative.
 *
 * Une configuration possède une liste de paramètres.
 * Un paramètre est
 *  - identifié par un nom.
 *  - camelCase.camelCase et ne contient que des lettres et chiffres; les séparateurs sont des points.
 *
 * Les paramètres sont de trois types :
 * -boolean
 * -String
 * -int
 *
 *
 * Exemple en json :
 *
 * {
 *  server.host : "wiki",
 *  server.port : "5455",
 *  maxUsers  :"10",
 * }
 *
 *
 * getStringValue("server.host") => wiki
 * getStringValue("host") => erreur.
 *
 * @author pchretien, npiedeloup, prahmoune
 */
public interface ParamManager extends Manager {
	/**
	 * Returns the value for a param, defined by its name.
	 * @param paramName Name of the param
	 * @return the value of the param
	 */
	Param getParam(String paramName);

	/**
	 * Returns the optional value for a param which may be present / or not, defined by its name.
	 *
	 * @param paramName
	 * @return the optional value of the param
	 */
	Optional<Param> getOptionalParam(String paramName);
}
