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

import java.lang.reflect.Field;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Métadonnée liée à un champ.
 *
 * @author  pchretien
 *
 */
public final class DtProperty {
	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	public static final Property<Integer> MAX_LENGTH = new Property<>("maxLength", Integer.class);

	/**
	 * Propriété standard : Type des définitions.
	 */
	public static final Property<String> TYPE = new Property<>("type", String.class);

	/**
	 * Proriété Regex de type String.
	 */
	public static final Property<String> REGEX = new Property<>("pattern", String.class);

	/**
	 * Propriété de contrainte : valeur minimum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date min.
	 */
	public static final Property<Double> MIN_VALUE = new Property<>("minValue", Double.class);

	/**
	 * Propriété de contrainte : valeur maximum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date max.
	 */
	public static final Property<Double> MAX_VALUE = new Property<>("maxValue", Double.class);

	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	public static final Property<String> UNIT = new Property<>("unit", String.class);

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	public static final Property<String> INDEX_TYPE = new Property<>("indexType", String.class);

	/**
	 * Propriété standard : Type de données pour la persistence.
	 */
	public static final Property<String> STORE_TYPE = new Property<>("storeType", String.class);

	public static final Property<Boolean> MULTIPLE = new Property<>("multiple", Boolean.class);

	private DtProperty() {
		//private
	}

	/**
	 * @param propertyName Property name
	 * @return Property
	 */
	public static Property<?> valueOf(final String propertyName) {
		try {
			final Field field = DtProperty.class.getDeclaredField(propertyName);
			final Property<?> property = Property.class.cast(field.get(DtProperty.class));
			Assertion.checkNotNull(property);
			return property;
		} catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw WrappedException.wrap(e, "property '{0}' not found on DtProperty", propertyName);
		}
	}
}
