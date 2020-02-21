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
package io.vertigo.vega.plugins.webservice.webserver.sparkjava;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

/**
 * VegaSparkFilter for SparkFilter (mandatory).
 * @author npiedeloup
 */
public final class VegaSparkFilter extends SparkFilter {
	private static final SparkApplication[] EMPTY_CONF = new SparkApplication[0];

	/** {@inheritDoc} */
	@Override
	protected SparkApplication getApplication(final FilterConfig filterConfig) {
		return () -> {
			/*no specific initialization*/
		};
	}

	/** {@inheritDoc} */
	@Override
	protected SparkApplication[] getApplications(final FilterConfig filterConfig) {
		return EMPTY_CONF;
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		//Spark override getRequestURI and getPathInfo from request and send it to next Filters and servlets
		//We change the filterChain to revert these modifications when calling filter chain
		final FilterChain wrappedFilterChain = new FilterChain() {

			/** {@inheritDoc} */
			@Override
			public void doFilter(final ServletRequest chainRequest, final ServletResponse chainResponse)
					throws IOException, ServletException {
				chain.doFilter(request, chainResponse);
			}
		};
		super.doFilter(request, response, wrappedFilterChain);
	}

}
