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
package io.vertigo.dynamo.criteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;

/**
 * Context of a criteria (Handles values of parameters for filtering)
 * @author pchretien
 *
 */
public final class CriteriaCtx {
	private int i;
	private final Map<String, String> fieldValueNames = new HashMap<>();
	private final Map<String, Object> attributeValues = new HashMap<>();
	private final Map<String, DtFieldName> attributeNames = new HashMap<>();

	String attributeName(final DtFieldName dtFieldName, final Object value) {
		final String attributeValueName = dtFieldName.name() + String.valueOf(value);
		String attributeName = fieldValueNames.get(attributeValueName);
		if (attributeName == null) {
			attributeName = dtFieldName.name() + i;
			i++;
			fieldValueNames.put(attributeValueName, attributeName);
			attributeValues.put(attributeName, value);
			attributeNames.put(attributeName, dtFieldName);
		}
		return attributeName;
	}

	/**
	 * Return all attributes' names in this context
	 * @return the set of all attributes' name
	 */
	public Set<String> getAttributeNames() {
		return attributeValues.keySet();
	}

	/**
	 * Return the dtFieldName linked to an attribute name
	 * @param attributeName the name of the attribute
	 * @return the corresponding DtFieldName
	 */
	public DtFieldName getDtFieldName(final String attributeName) {
		return attributeNames.get(attributeName);
	}

	/**
	 * Return the value of an attribute in the context
	 * @param attributeName the name of the attribute
	 * @return the corresponding value
	 */
	public Object getAttributeValue(final String attributeName) {
		return attributeValues.get(attributeName);
	}
}
