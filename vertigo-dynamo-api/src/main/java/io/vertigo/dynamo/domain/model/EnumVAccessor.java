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
package io.vertigo.dynamo.domain.model;

import java.util.stream.Stream;

import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 *
 * Accessor specialized for Enum access
 * @author mlaroche
 *
 * @param <E> type of the remote entity
 * @param <V> type of the enum valuetype
 */
public final class EnumVAccessor<E extends Entity, V extends Enum<V> & MasterDataEnum<E>> extends AbstractVAccessor<E> {
	private static final long serialVersionUID = 1L;

	private final Class<V> enumClass;

	/**
	 * Constructor.
	 * @param clazz the entity class
	 * @param role the role of the association (case of multiple associations with the same entity)
	 */
	public EnumVAccessor(final Class<E> clazz, final String role, final Class<V> enumClass) {
		super(DtObjectUtil.findDtDefinition(clazz), role);
		//---
		Assertion.checkState(enumClass.isEnum() && MasterDataEnum.class.isAssignableFrom(enumClass), "Enum '{0}' must implement StaticMasterDataEnum", enumClass.getCanonicalName());
		this.enumClass = enumClass;
	}

	/**
	 * Retrieves the values of the remote entity as an Enum value
	 * @return the enum value representing the distant entity
	 */
	public V getEnumValue() {
		final UID<E> entityUri = getUID();
		if (entityUri != null) {
			return Stream.of(enumClass.getEnumConstants())
					.filter(enumValue -> entityUri.equals(enumValue.getEntityUID()))
					.findFirst()
					.orElseThrow(() -> new VSystemException("Unable to find corresponding enum of type '{0}' with uid '{1}'", enumClass.getName(), entityUri));
		}
		return null;

	}

	/**
	 * Set the value of the remote entity as an Enum value
	 * @param enumValue
	 */
	public void setEnumValue(final V enumValue) {
		setUID(enumValue.getEntityUID());
	}

}
