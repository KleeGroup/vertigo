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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.util.ClassUtil;
import io.vertigo.vega.engines.webservice.json.UiListDelta;
import io.vertigo.vega.engines.webservice.json.UiListModifiable;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.ExtendedObject;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;
import spark.Request;
import spark.Response;

/**
 * Params handler. Extract and Json convert.
 * @author npiedeloup
 */
public final class ValidatorWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		final WebServiceDefinition webServiceDefinition = routeContext.getWebServiceDefinition();
		final UiMessageStack uiMessageStack = routeContext.getUiMessageStack();
		for (final WebServiceParam webServiceParam : webServiceDefinition.getWebServiceParams()) {
			final Object value = routeContext.getParamValue(webServiceParam);
			validateParam(value, uiMessageStack, webServiceParam, routeContext);
		}
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return chain.handle(request, response, routeContext);
	}

	private static void validateParam(final Object value, final UiMessageStack uiMessageStack, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		final Map<String, DtObject> contextKeyMap = new HashMap<>();
		if (value instanceof UiObject && DtObject.class.isAssignableFrom(webServiceParam.getType())) {
			final UiObject<DtObject> uiObject = (UiObject<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(webServiceParam);
			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtObject updatedDto = uiObject.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			contextKeyMap.put(uiObject.getInputKey(), updatedDto);
			routeContext.registerUpdatedDtObjects(webServiceParam, updatedDto, contextKeyMap);
		} else if (value instanceof UiListDelta && DtListDelta.class.isAssignableFrom(webServiceParam.getType())) {
			final UiListDelta<DtObject> uiListDelta = (UiListDelta<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(webServiceParam);

			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtList<DtObject> dtListCreates = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getCreatesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtList<DtObject> dtListUpdates = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getUpdatesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtList<DtObject> dtListDeletes = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getDeletesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtListDelta<DtObject> dtListDelta = new DtListDelta<>(dtListCreates, dtListUpdates, dtListDeletes);
			routeContext.registerUpdatedDtObjects(webServiceParam, dtListDelta, contextKeyMap);
		} else if (value instanceof UiListModifiable && DtList.class.isAssignableFrom(webServiceParam.getType())) {
			final UiListModifiable<DtObject> uiList = (UiListModifiable<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(webServiceParam);

			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtList<DtObject> dtList = mergeAndCheckInput(uiList.getObjectType(), uiList, dtObjectValidators, uiMessageStack, contextKeyMap);
			routeContext.registerUpdatedDtObjects(webServiceParam, dtList, contextKeyMap);
		} else if (value instanceof ExtendedObject) {
			final ExtendedObject<?> extendedObject = (ExtendedObject) value;
			validateParam(extendedObject.getInnerObject(), uiMessageStack, webServiceParam, routeContext);
			final Object updatedValue = routeContext.getParamValue(webServiceParam);
			final ExtendedObject<?> updatedExtendedObject = new ExtendedObject(updatedValue);
			updatedExtendedObject.putAll(extendedObject);
			routeContext.setParamValue(webServiceParam, updatedExtendedObject);
		} else if (value instanceof Optional && ((Optional) value).isPresent()) {
			validateParam(((Optional) value).get(), uiMessageStack, webServiceParam, routeContext);
		}
	}

	private static List<DtObjectValidator<DtObject>> obtainDtObjectValidators(final WebServiceParam webServiceParam) {
		final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses = webServiceParam.getDtObjectValidatorClasses();
		final List<DtObjectValidator<DtObject>> dtObjectValidators = new ArrayList<>(dtObjectValidatorClasses.size());
		for (final Class<? extends DtObjectValidator> dtObjectValidatorClass : dtObjectValidatorClasses) {
			dtObjectValidators.add(ClassUtil.newInstance(dtObjectValidatorClass));
		}
		return dtObjectValidators;
	}

	private static <D extends DtObject> DtList<D> mergeAndCheckInput(
			final Class<D> objectType,
			final Map<String, UiObject<D>> uiObjectMap,
			final List<DtObjectValidator<D>> dtObjectValidators,
			final UiMessageStack uiMessageStack,
			final Map<String, DtObject> contextKeyMap) {
		final DtList<D> dtList = new DtList<>(objectType);
		for (final Map.Entry<String, UiObject<D>> entry : uiObjectMap.entrySet()) {
			final D dto = entry.getValue().mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
			contextKeyMap.put(entry.getValue().getInputKey(), dto);
		}
		return dtList;
	}

	private static <D extends DtObject> DtList<D> mergeAndCheckInput(
			final Class<DtObject> objectType,
			final UiListModifiable<D> uiList,
			final List<DtObjectValidator<D>> dtObjectValidators,
			final UiMessageStack uiMessageStack,
			final Map<String, DtObject> contextKeyMap) {
		final DtList<D> dtList = new DtList<>(objectType);
		for (final UiObject<D> element : uiList) {
			final D dto = element.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
			contextKeyMap.put(element.getInputKey(), dto);
		}
		return dtList;
	}
}
