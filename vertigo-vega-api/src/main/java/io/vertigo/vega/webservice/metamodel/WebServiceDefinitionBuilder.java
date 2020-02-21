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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition.Verb;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;

/**
 * WebServiceDefinition Builder.
 *
 * @author npiedeloup
 */
public final class WebServiceDefinitionBuilder implements Builder<WebServiceDefinition> {
	private static final int NAME_MAX_SIZE = 40;
	private final Method myMethod;
	private Verb myVerb;
	private String myPathPrefix;
	private String myPath;
	private boolean myNeedSession = true;
	private boolean mySessionInvalidate;
	private boolean myNeedAuthentication = true;
	private final Set<String> myIncludedFields = new LinkedHashSet<>();
	private final Set<String> myExcludedFields = new LinkedHashSet<>();
	private boolean myAccessTokenPublish;
	private boolean myAccessTokenMandatory;
	private boolean myAccessTokenConsume;
	private boolean myServerSideSave;
	private boolean myAutoSortAndPagination;
	private String myDoc = "";
	private boolean myCorsProtected = true; //true by default
	private boolean myFileAttachment = true; //true by default
	private final List<WebServiceParam> myWebServiceParams = new ArrayList<>();

	/**
	 * Constructeur.
	 * @param method Method to bind to this webService
	 */
	WebServiceDefinitionBuilder(final Method method) {
		Assertion.checkNotNull(method);
		//-----
		myMethod = method;
	}

	/** {@inheritDoc} */
	@Override
	public WebServiceDefinition build() {
		final String usedPath = myPathPrefix != null ? myPathPrefix + myPath : myPath;
		final String sortPath = sortPath(myVerb, usedPath);
		final String acceptedType = computeAcceptedType();
		return new WebServiceDefinition(
				"Ws" + myVerb + normalizePath(usedPath),
				myVerb,
				usedPath,
				sortPath,
				acceptedType,
				myMethod,
				myNeedSession,
				mySessionInvalidate,
				myNeedAuthentication,
				myAccessTokenPublish,
				myAccessTokenMandatory,
				myAccessTokenConsume,
				myServerSideSave,
				myAutoSortAndPagination,
				myIncludedFields,
				myExcludedFields,
				myWebServiceParams,
				myDoc,
				myCorsProtected,
				myFileAttachment);
	}

	private static String normalizePath(final String servicePath) {
		final String argsRemovedPath = servicePath
				.replaceAll("_|\\(\\)", "") //remove unsignificative path elements (can't have different routes by _ or ()
				.replaceAll("\\{.*?\\}|\\*", "_");//.*? : reluctant quantifier; remove params which works like wildcards

		//On rend le path plus lisible et compatible DefinitionName
		final String normalizedConstString = argsRemovedPath.toUpperCase(Locale.ROOT)
				.replaceAll("/", "_")
				.replaceAll("([0-9]+)([^0-9])", "_$1_$2")
				.replaceAll("([0-9]+)", "_$1")
				.replaceAll("_+", "_");
		final String normalizedString = StringUtil.constToUpperCamelCase(normalizedConstString);
		final String hashcodeAsHex = "$x" + Integer.toHexString(argsRemovedPath.hashCode());
		//On limite sa taille pour avec un nom de définition acceptable
		return normalizedString.substring(0, Math.min(NAME_MAX_SIZE, normalizedString.length())) + hashcodeAsHex;
	}

	private static String sortPath(final Verb myVerb, final String servicePath) {
		//On calcule la taille du path sans le nom des paramètres, c'est util pour trier les routes dans l'ordre d'interception.
		final String argsRemovedPath = servicePath.replaceAll("_|\\(\\)", "").replaceAll("\\{.*?\\}|\\*", "_");//.*? : reluctant quantifier;
		return myVerb + argsRemovedPath.toUpperCase(Locale.ROOT);
	}

	/**
	 * @param pathPrefix Path prefix
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withPathPrefix(final String pathPrefix) {
		Assertion.checkArgNotEmpty(pathPrefix, "Route pathPrefix must be specified on {0}.{1}", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		Assertion.checkArgument(pathPrefix.startsWith("/"), "Route pathPrefix must starts with / (on {0}.{1})", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		//-----
		myPathPrefix = pathPrefix;
		return this;
	}

	/**
	 * @param verb Verb
	 * @param path Path
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder with(final Verb verb, final String path) {
		Assertion.checkState(myVerb == null, "A verb is already specified on {0}.{1} ({2})", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName(), myVerb);
		Assertion.when(StringUtil.isEmpty(myPathPrefix))
				.check(() -> !StringUtil.isEmpty(path), "Route path must be specified on {0}.{1} (at least you should defined a pathPrefix)", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		Assertion.when(!StringUtil.isEmpty(path))
				.check(() -> path.startsWith("/"), "Route path must be empty (then use pathPrefix) or starts with / (on {0}.{1})", myMethod.getDeclaringClass().getSimpleName(), myMethod.getName());
		//-----
		myVerb = verb;
		myPath = path;
		return this;
	}

	/**
	 * @return if verb was set
	 */
	public boolean hasVerb() {
		return myVerb != null;
	}

	/**
	 * @param accessTokenConsume accessTokenConsume
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withAccessTokenConsume(final boolean accessTokenConsume) {
		myAccessTokenConsume = accessTokenConsume;
		return this;
	}

	/**
	 * @param needAuthentication needAuthentication
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withNeedAuthentication(final boolean needAuthentication) {
		myNeedAuthentication = needAuthentication;
		return this;
	}

	/**
	 * @param needSession needSession
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withNeedSession(final boolean needSession) {
		myNeedSession = needSession;
		return this;
	}

	/**
	 * @param sessionInvalidate sessionInvalidate
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withSessionInvalidate(final boolean sessionInvalidate) {
		mySessionInvalidate = sessionInvalidate;
		return this;
	}

	/**
	 * @param excludedFields list of excludedFields
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder addExcludedFields(final String... excludedFields) {
		Assertion.checkNotNull(excludedFields);
		//-----
		myExcludedFields.addAll(Arrays.asList(excludedFields));
		return this;
	}

	/**
	 * @param includedFields list of includedFields
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder addIncludedFields(final String... includedFields) {
		Assertion.checkNotNull(includedFields);
		//-----
		myIncludedFields.addAll(Arrays.asList(includedFields));
		return this;
	}

	/**
	 * @param accessTokenPublish accessTokenPublish
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withAccessTokenPublish(final boolean accessTokenPublish) {
		myAccessTokenPublish = accessTokenPublish;
		return this;
	}

	/**
	 * @param accessTokenMandatory accessTokenMandatory
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withAccessTokenMandatory(final boolean accessTokenMandatory) {
		myAccessTokenMandatory = accessTokenMandatory;
		return this;
	}

	/**
	 * @param serverSideSave serverSideSave
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withServerSideSave(final boolean serverSideSave) {
		myServerSideSave = serverSideSave;
		return this;
	}

	/**
	 * @param autoSortAndPagination autoSortAndPagination
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withAutoSortAndPagination(final boolean autoSortAndPagination) {
		myAutoSortAndPagination = autoSortAndPagination;

		//autoSortAndPagination must keep the list serverSide but not the input one, its the full one, so we don't use serverSideSave marker
		//autoSortAndPagination use a Implicit DtListState, this one must be show in API, so we add it to webServiceParams
		//autoSortAndPaginationHandler will use it
		if (autoSortAndPagination) {
			addWebServiceParam(WebServiceParam.builder(String.class)
					.with(WebServiceParamType.Query, "listServerToken") // We declare listServerToken in query without prefix
					.optional()
					.build());
			addWebServiceParam(WebServiceParam.builder(DtListState.class)
					.with(WebServiceParamType.Query, "") // We declare ListState in query without prefix
					.build());
		}
		return this;
	}

	/**
	 * @param doc doc
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withDoc(final String doc) {
		myDoc = doc;
		return this;
	}

	/**
	 * @param corsProtected corsProtected
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withCorsProtected(final boolean corsProtected) {
		myCorsProtected = corsProtected;
		return this;
	}

	/**
	 * @param fileAttachment fileAttachment
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder withFileAttachment(final boolean fileAttachment) {
		myFileAttachment = fileAttachment;
		return this;
	}

	/**
	 * @param webServiceParam webServiceParam
	 * @return this builder
	 */
	public WebServiceDefinitionBuilder addWebServiceParam(final WebServiceParam webServiceParam) {
		myWebServiceParams.add(webServiceParam);
		return this;
	}

	private static String computeAcceptedType() {
		//AcceptedType is from client view : it's return type, not input type
		return "*/*";
	}

}
