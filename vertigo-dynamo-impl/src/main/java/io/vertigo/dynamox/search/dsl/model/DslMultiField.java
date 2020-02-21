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
package io.vertigo.dynamox.search.dsl.model;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

/**
 * Multi fields definition.
 * (preBody)\[(fields)+,\](postBody)
 * @author npiedeloup
 */
public final class DslMultiField {
	private final String preBody;
	private final List<DslField> fields;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fields List of Index's fields
	 * @param postBody String after body
	 */
	public DslMultiField(final String preBody, final List<DslField> fields, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(fields);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fields = fields;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return fields
				.stream()
				.map(DslField::toString)
				.collect(Collectors.joining(",", preBody + '[', ']' + postBody));
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return fields
	 */
	public List<DslField> getFields() {
		return fields;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}
}
