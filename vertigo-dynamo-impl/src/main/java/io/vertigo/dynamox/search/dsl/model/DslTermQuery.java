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

import java.util.Optional;

import io.vertigo.lang.Assertion;

/**
 * Term query definition.
 * (preBody)#(preTerm)(termField)(postTerm)#?\((escapeMode)\)!\((defaultValue)\)(postBody)
 * @author npiedeloup
 */
public final class DslTermQuery implements DslQuery {

	/**
	 * Reserved mode.
	 * reserved: + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ / AND OR
	 */
	public enum EscapeMode {
		/** no change. */
		none,
		/** escape reserved syntax. */
		escape,
		/** remove reserved syntax. */
		remove
	}

	private final String preBody;
	private final String preTerm;
	private final String termField;
	private final String postTerm;
	private final EscapeMode escapeMode;
	private final Optional<String> defaultValue;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param preTerm String before body
	 * @param termField Term field (criteria's field)
	 * @param postBody String after body
	 * @param escapeMode Reserved escapeMode of criteria
	 * @param defaultValue Optional default value (used if null or empty criteria)
	 * @param postTerm String after body
	 */
	public DslTermQuery(
			final String preBody,
			final String preTerm,
			final String termField,
			final String postTerm,
			final EscapeMode escapeMode,
			final Optional<String> defaultValue,
			final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(preTerm);
		Assertion.checkNotNull(termField);
		Assertion.checkNotNull(postTerm);
		Assertion.checkNotNull(escapeMode);
		Assertion.checkNotNull(defaultValue);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.preTerm = preTerm;
		this.termField = termField;
		this.postTerm = postTerm;
		this.escapeMode = escapeMode;
		this.defaultValue = defaultValue;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + "#" + preTerm + termField + postTerm + "#"
				+ (escapeMode != EscapeMode.none ? "?(" + escapeMode + "Reserved)" : "") + (defaultValue.isPresent() ? "!(" + defaultValue.get() + ")" : "") + postBody;
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return preTerm
	 */
	public String getPreTerm() {
		return preTerm;
	}

	/**
	 * @return termField
	 */
	public String getTermField() {
		return termField;
	}

	/**
	 * @return postTerm
	 */
	public String getPostTerm() {
		return postTerm;
	}

	/**
	 * @return escapeMode
	 */
	public EscapeMode getEscapeMode() {
		return escapeMode;
	}

	/**
	 * @return defaultValue
	 */
	public Optional<String> getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}
}
