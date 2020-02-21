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
package io.vertigo.vega.plugins.webservice.handler;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.token.TokenManager;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import spark.Request;
import spark.Response;

/**
 * Auto paginator and Sort handler.
 * @author npiedeloup
 */
public final class PaginatorAndSortWebServiceHandlerPlugin implements WebServiceHandlerPlugin {
	public static final String LIST_SERVER_TOKEN = "listServerToken";

	private static final int DEFAULT_RESULT_PER_PAGE = 20;

	private final TokenManager tokenManager;
	private final CollectionsManager collectionsManager;

	/**
	 * Constructor.
	 * @param collectionsManager collections manager
	 * @param tokenManager token manager
	 */
	@Inject
	public PaginatorAndSortWebServiceHandlerPlugin(final CollectionsManager collectionsManager, final TokenManager tokenManager) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(tokenManager);
		//-----
		this.collectionsManager = collectionsManager;
		this.tokenManager = tokenManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return webServiceDefinition.isAutoSortAndPagination();
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {

		final WebServiceDefinition webServiceDefinition = routeContext.getWebServiceDefinition();
		//Criteria in body
		//DtListState in query //see at WebServiceDefinitionBuilder withAutoSortAndPagination it defined where DtListState was
		//serverToken in DtListState

		final WebServiceParam uiListWebServiceParams = lookupWebServiceParam(webServiceDefinition, DtListState.class);
		final WebServiceParam serverTokenWebServiceParams = lookupWebServiceParam(webServiceDefinition, LIST_SERVER_TOKEN);

		final DtListState parsedDtListState = (DtListState) routeContext.getParamValue(uiListWebServiceParams);
		final DtListState dtListState = checkAndEnsureDefaultValue(parsedDtListState);

		final Optional<String> listServerToken = (Optional<String>) routeContext.getParamValue(serverTokenWebServiceParams);
		Optional<DtList<?>> fullListOption = Optional.empty();
		if (listServerToken.isPresent()) {
			fullListOption = tokenManager.get(listServerToken.get());
			response.header(LIST_SERVER_TOKEN, listServerToken.get());
		}
		final DtList<?> fullList;
		if (fullListOption.isPresent()) {
			fullList = fullListOption.get();
		} else {
			final Object result = chain.handle(request, response, routeContext);
			Assertion.checkArgument(result instanceof DtList, "sort and pagination only supports DtList");
			fullList = (DtList<?>) result;
			final String fullListServerToken = tokenManager.put(fullList);
			response.header(LIST_SERVER_TOKEN, fullListServerToken);
		}
		response.header("x-total-count", String.valueOf(fullList.size())); //TODO total count should be list meta
		final DtList<?> filteredList = applySortAndPagination(fullList, dtListState);
		filteredList.setMetaData(DtList.TOTAL_COUNT_META, fullList.size());
		return filteredList;
	}

	private static DtListState checkAndEnsureDefaultValue(final DtListState parsedDtListState) {
		if (!parsedDtListState.getMaxRows().isPresent()) {//check if parsedDtListState is just not initalized
			return DtListState.of(DEFAULT_RESULT_PER_PAGE, parsedDtListState.getSkipRows(), parsedDtListState.getSortFieldName().orElse(null), parsedDtListState.isSortDesc().orElse(null));
		}
		return parsedDtListState;
	}

	/**
	 * Lookup for a parameter of the asked type, return the first found.
	 * @param webServiceDefinition WebService definition
	 * @param paramType Type asked
	 * @return first WebServiceParam of this type, throw exception if not found
	 */
	private static WebServiceParam lookupWebServiceParam(final WebServiceDefinition webServiceDefinition, final Class<DtListState> paramType) {
		return webServiceDefinition.getWebServiceParams()
				.stream()
				.filter(webServiceParam -> paramType.equals(webServiceParam.getType()))
				.findFirst()
				.orElseThrow(() -> new NullPointerException("sort and pagination need a DtListState endpointParams. It should have been added by WebServiceParamBuilder."));

	}

	/**
	 * Lookup for a parameter of the asked name, return the first found.
	 * @param webServiceDefinition WebService definition
	 * @param paramType Type asked
	 * @return first WebServiceParam of this name, throw exception if not found
	 */
	private static WebServiceParam lookupWebServiceParam(final WebServiceDefinition webServiceDefinition, final String paramName) {
		return webServiceDefinition.getWebServiceParams()
				.stream()
				.filter(webServiceParam -> paramName.equals(webServiceParam.getName()))
				.findFirst()
				.orElseThrow(() -> new NullPointerException("sort and pagination need a " + paramName + " endpointParams. It should have been added by WebServiceParamBuilder."));

	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final DtListState dtListState) {
		final DtList<D> sortedList;
		if (dtListState.getSortFieldName().isPresent()) {
			sortedList = collectionsManager.sort(unFilteredList, dtListState.getSortFieldName().get(), dtListState.isSortDesc().get());
		} else {
			sortedList = unFilteredList;
		}
		if (dtListState.getSkipRows() >= sortedList.size()) {
			return new DtList<>(unFilteredList.getDefinition());
		} else if (dtListState.getMaxRows().isPresent()) {
			return sortedList
					.stream()
					.skip(dtListState.getSkipRows())
					.limit(dtListState.getMaxRows().get())
					.collect(VCollectors.toDtList(sortedList.getDefinition()));
		}
		return sortedList;
	}
}
