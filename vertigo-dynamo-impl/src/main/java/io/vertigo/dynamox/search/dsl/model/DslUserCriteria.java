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

import io.vertigo.lang.Assertion;

/**
 * One user criteria.
 * A user query is many DslUserCriteria.
 * @author npiedeloup
 */
public final class DslUserCriteria {
	private final String preMissingPart;
	private final String overridedFieldName;
	private final String overridedPreModifier;
	private final String criteriaWord;
	private final String overridedPostModifier;
	private final String postMissingPart;

	/**
	 * @param preMissingPart Optional pre missing part
	 * @param overridedFieldName Optional overrided fieldName
	 * @param overridedPreModifier Optional overrided word pre modifier
	 * @param criteriaWord Criteria word
	 * @param overridedPostModifier Optional overrided word post modifier
	 * @param postMissingPart Optional post missing part
	 */
	public DslUserCriteria(
			final String preMissingPart,
			final String overridedFieldName,
			final String overridedPreModifier,
			final String criteriaWord,
			final String overridedPostModifier,
			final String postMissingPart) {
		Assertion.checkNotNull(preMissingPart);
		Assertion.checkNotNull(overridedFieldName);
		Assertion.checkNotNull(overridedPreModifier);
		Assertion.checkArgNotEmpty(criteriaWord);
		Assertion.checkNotNull(overridedPostModifier);
		Assertion.checkNotNull(postMissingPart);
		//-----
		this.preMissingPart = preMissingPart;
		this.overridedFieldName = overridedFieldName;
		this.overridedPreModifier = overridedPreModifier;
		this.criteriaWord = criteriaWord;
		this.overridedPostModifier = overridedPostModifier;
		this.postMissingPart = postMissingPart;
	}

	/**
	 * @return preMissingPart
	 */
	public String getPreMissingPart() {
		return preMissingPart;
	}

	/**
	 * @return overridedFieldName
	 */
	public String getOverridedFieldName() {
		return overridedFieldName;
	}

	/**
	 * @return overridedPreModifier
	 */
	public String getOverridedPreModifier() {
		return overridedPreModifier;
	}

	/**
	 * @return criteriaWord
	 */
	public String getCriteriaWord() {
		return criteriaWord;
	}

	/**
	 * @return overridedPostModifier
	 */
	public String getOverridedPostModifier() {
		return overridedPostModifier;
	}

	/**
	 * @return postMissingPart
	 */
	public String getPostMissingPart() {
		return postMissingPart;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(preMissingPart)
				.append(overridedFieldName)
				.append(overridedPreModifier)
				.append(criteriaWord)
				.append(overridedPostModifier)
				.append(postMissingPart)
				.toString();
	}
}
