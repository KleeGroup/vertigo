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
package io.vertigo.vega.impl.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.WrappedException;

/**
 * Filtre analytics des requetes HTTP. *
 * @author npiedeloup
 */
public final class AnalyticsFilter extends AbstractFilter {
	private AnalyticsManager analyticsManager;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		analyticsManager = Home.getApp().getComponentSpace().resolve(AnalyticsManager.class);
	}

	/** {@inheritDoc} */
	@Override
	public void doMyFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) {
		final String name = ((HttpServletRequest) request).getRequestURL().toString();
		analyticsManager.trace(
				"urls",
				name,
				tracer -> {
					try {
						chain.doFilter(request, response);
					} catch (IOException | ServletException e) {
						throw WrappedException.wrap(e);
					}
				});
	}
}
