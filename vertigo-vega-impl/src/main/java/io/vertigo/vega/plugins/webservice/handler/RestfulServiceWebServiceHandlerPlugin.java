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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.lang.VUserException;
import io.vertigo.util.ClassUtil;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.validation.ValidationUserException;
import spark.Request;
import spark.Response;

/**
 * RestfulServiceHandler : call service method.
 * @author npiedeloup
 */
public final class RestfulServiceWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		final WebServiceDefinition webServiceDefinition = routeContext.getWebServiceDefinition();
		final Object[] serviceArgs = makeArgs(routeContext);
		final Method method = webServiceDefinition.getMethod();
		final WebServices webServices = (WebServices) Home.getApp().getComponentSpace().resolve(method.getDeclaringClass());

		if (method.getName().startsWith("create")) {
			//by convention, if method starts with 'create', an http 201 status code is returned (if ok)
			response.status(HttpServletResponse.SC_CREATED);
		}
		try {
			return ClassUtil.invoke(webServices, method, serviceArgs);
		} catch (final RuntimeException e) {
			//If throwed exception was ValidationUserException, VUserException, SessionException, VSecurityException, RuntimeException
			//we re throw it
			final Throwable cause = e.getCause();
			if (cause instanceof InvocationTargetException) {
				final Throwable targetException = ((InvocationTargetException) cause).getTargetException();
				if (targetException instanceof ValidationUserException) {
					throw (ValidationUserException) targetException;
				} else if (targetException instanceof VUserException) {
					throw (VUserException) targetException;
				} else if (targetException instanceof SessionException) {
					throw (SessionException) targetException;
				} else if (targetException instanceof VSecurityException) {
					throw (VSecurityException) targetException;
				} else if (targetException instanceof RuntimeException) {
					throw (RuntimeException) targetException;
				}
			}
			throw e;
		}
	}

	private static Object[] makeArgs(final WebServiceCallContext routeContext) {
		final WebServiceDefinition webServiceDefinition = routeContext.getWebServiceDefinition();
		if (webServiceDefinition.isAutoSortAndPagination()) {
			final Object[] serviceArgs = new Object[webServiceDefinition.getWebServiceParams().size() - 2]; //when using AutoSortAndPagination, there is 2 automanaged params
			int i = 0;
			for (final WebServiceParam webServiceParam : webServiceDefinition.getWebServiceParams()) {
				if (!webServiceParam.getType().isAssignableFrom(DtListState.class)
						&& !webServiceParam.getName().equals(PaginatorAndSortWebServiceHandlerPlugin.LIST_SERVER_TOKEN)) {
					serviceArgs[i] = routeContext.getParamValue(webServiceParam);
					i++;
				}
			}
			return serviceArgs;
		}
		return webServiceDefinition.getWebServiceParams()
				.stream()
				.map(webServiceParam -> routeContext.getParamValue(webServiceParam))
				.toArray();
	}
}
