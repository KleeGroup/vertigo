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
/**
 *
 */
package io.vertigo.account.authorization.metamodel;

import java.util.Collections;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;

/**
 * Secured data dimension.
 *
 * @author jgarnier, npiedeloup
 */
public final class SecurityDimension {

	private final String name;
	private final SecurityDimensionType type;
	private final List<DtField> fields;
	private final List<String> values;

	/**
	 * Construct an instance of SecurityDimension.
	 *
	 * @param name name.
	 * @param type type.
	 * @param fields Ordered list of fields (multiple for TREE, empty for ENUM).
	 * @param values Ordered list of values (empty for TREE, multiple for ENUM).
	 */
	public SecurityDimension(
			final String name,
			final SecurityDimensionType type,
			final List<DtField> fields,
			final List<String> values) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(fields);
		Assertion.checkNotNull(values);
		Assertion
				.when(SecurityDimensionType.ENUM == type) // == because enum
				.check(() -> fields.isEmpty() && values.size() > 1, "SecurityDimension of type ENUM ({0}) needs the ordered list of values and no field (name is use)", name);
		Assertion
				.when(SecurityDimensionType.TREE == type) // == because enum
				.check(() -> fields.size() > 1 && values.isEmpty(), "SecurityDimension of type TREE ({0}) needs more than on fields and the no values", name);
		//----
		this.name = name;
		this.type = type;
		this.fields = Collections.unmodifiableList(fields);
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Give the name of this dimension.
	 *
	 * @return the name of this dimension.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Give the value of type.
	 *
	 * @return the value of type.
	 */
	public SecurityDimensionType getType() {
		return type;
	}

	/**
	 * Give the ordered list of fields (multiple for TREE, empty for ENUM)
	 *
	 * @return the ordered list of fields.
	 */
	public List<DtField> getFields() {
		return fields;
	}

	/**
	 * Give the ordered list of values (empty for TREE, multiple for ENUM).
	 *
	 * @return the  ordered list of values.
	 */
	public List<String> getValues() {
		return values;
	}
}
