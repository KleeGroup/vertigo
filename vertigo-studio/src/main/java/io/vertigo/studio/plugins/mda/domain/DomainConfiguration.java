/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.domain;

import static io.vertigo.studio.impl.mda.PropertiesUtil.getPropertyNotNull;
import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du DomainGenerator.
 *
 * @author dchallas
 */
final class DomainConfiguration extends AbstractConfiguration {
	private final String domainDictionaryClassName;

	/**
	 * Constructeur.
	 * @param properties propriétes
	 */
	DomainConfiguration(final Properties properties) {
		super(properties);
		//---------------------------------------------------------------------
		domainDictionaryClassName = getPropertyNotNull(properties, "domain.dictionaryClassName", "domain.dictionaryClassName doit être renseigné et préciser le nom de la class Dictionaire des DtDefinitions");
	}

	String getDomainPackage() {
		return getProjectPackageName() + ".domain";
	}

	String getDomainDictionaryClassName() {
		return domainDictionaryClassName;
	}
}
