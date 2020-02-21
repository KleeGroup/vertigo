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
package io.vertigo.dynamo.domain.metamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertigo.lang.Assertion;

/**
 * Gestion de la flexibilité structurelle du modèle.
 * Permet d'ajouter des propriétés sur les concepts structurels.
 *
 * @author pchretien
 */
public final class Properties {
	private final Map<Property<?>, Object> properties;

	Properties(final Map<Property<?>, Object> properties) {
		Assertion.checkNotNull(properties);
		//-----
		if (properties.isEmpty()) {
			this.properties = Collections.emptyMap();
		} else {
			this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
		}
	}

	/**
	 * Static method factory for PropertiesBuilder
	 * @return PropertiesBuilder
	 */
	public static PropertiesBuilder builder() {
		return new PropertiesBuilder();
	}

	/**
	 * Set des propriétés gérées.
	 * @return Collection
	 */
	public Set<Property<?>> getProperties() {
		return properties.keySet();
	}

	/**
	 * Retourne la valeur d'une (méta) propriété liée au domaine, champ, dtDéfinition...
	 * null si cette propriété n'existe pas
	 * @param property Propriété
	 * @return valeur de la propriété
	 */
	public <T> T getValue(final Property<T> property) {
		Assertion.checkNotNull(property);
		//On ne vérifie rien sur le type retourné par le getter.
		//le type a été validé lors du put.
		//-----
		//Conformémément au contrat, on retourne null si pas de propriété trouvée
		return property.getType().cast(properties.get(property));
	}
}
