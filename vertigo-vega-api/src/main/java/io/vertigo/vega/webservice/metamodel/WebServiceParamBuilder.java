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
package io.vertigo.vega.webservice.metamodel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.vega.webservice.WebServiceTypeUtil;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import io.vertigo.vega.webservice.validation.DtObjectValidator;

/**
 * WebServiceParam Builder.
 *
 * @author npiedeloup
 */
public final class WebServiceParamBuilder implements Builder<WebServiceParam> {
	private final Type myParamType;
	private boolean optional;
	private WebServiceParamType myWebServiceParamType = WebServiceParamType.Body; // default;
	private String myWebServiceParamName = "[1]"; //default body Name
	private final List<Class<? extends DtObjectValidator>> myValidatorClasses = new ArrayList<>();
	private final Set<String> myIncludedFields = new HashSet<>();
	private final Set<String> myExcludedFields = new HashSet<>();
	private boolean myNeedServerSideToken;
	private boolean myConsumeServerSideToken;

	/**
	 * Constructor.
	 * @param paramType param type
	 */
	WebServiceParamBuilder(final Type paramType) {
		Assertion.checkNotNull(paramType);
		//-----
		optional = WebServiceTypeUtil.isAssignableFrom(Optional.class, paramType);
		if (optional) {
			//si option, le type du paramètre est le sub type
			final Type[] typeArguments = ((ParameterizedType) paramType).getActualTypeArguments();
			myParamType = typeArguments[0]; //on sait qu'il n'y a qu'un paramètre à Option<>
		} else {
			myParamType = paramType;
		}
	}

	/**
	 * @param restParamType paramType
	 * @param restParamName paramName
	 * @return Builder
	 */
	public WebServiceParamBuilder with(final WebServiceParamType restParamType, final String restParamName) {
		Assertion.checkNotNull(restParamType);
		Assertion.checkNotNull(restParamName); //empty names were check on WebServiceParam constructor
		//-----
		myWebServiceParamType = restParamType;
		myWebServiceParamName = restParamName;
		return this;
	}

	/**
	 * @param validatorClasses List of validator to check
	 * @return Builder
	 */
	public WebServiceParamBuilder addValidatorClasses(final Class<? extends DtObjectValidator>... validatorClasses) {
		Assertion.checkNotNull(validatorClasses);
		//-----
		myValidatorClasses.addAll(Arrays.asList(validatorClasses));
		return this;
	}

	/**
	 * @param excludedFields List of exluded fields
	 * @return Builder
	 */
	public WebServiceParamBuilder addExcludedFields(final String... excludedFields) {
		Assertion.checkNotNull(excludedFields);
		//-----
		myExcludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	/**
	 * @param includedFields list of included fields (empty means all fields included)
	 * @return Builder
	 */
	public WebServiceParamBuilder addIncludedFields(final String... includedFields) {
		Assertion.checkNotNull(includedFields);
		//-----
		myIncludedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	/**
	 * If serverSide token is needed and used
	 * @return Builder
	 */
	public WebServiceParamBuilder needServerSideToken() {
		myNeedServerSideToken = true;
		return this;
	}

	/**
	 * If serverSide token is consume
	 * @return Builder
	 */
	public WebServiceParamBuilder consumeServerSideToken() {
		myConsumeServerSideToken = true;
		return this;
	}

	/**
	 * Force this WebServiceParam as optional
	 * @return Builder
	 */
	public WebServiceParamBuilder optional() {
		optional = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public WebServiceParam build() {
		return new WebServiceParam(
				myWebServiceParamType,
				myWebServiceParamName,
				myParamType,
				optional,
				myIncludedFields,
				myExcludedFields,
				myNeedServerSideToken,
				myConsumeServerSideToken,
				myValidatorClasses);
	}
}
