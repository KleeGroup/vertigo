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
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.lang.Assertion;

/**
 * @deprecated
 * Filter qui permet de gérer la compatibilité avec IE8, en ajoutant le header X-UA-Compatible dans les responses.
 * @author npiedeloup
 */
@Deprecated
public class IE8CompatablityFixServlet implements Filter {
	private static final List<String> ACCEPTED_MODES = Arrays.asList("edge", "9", "8", "7", "5", "EmulateIE7");
	private String mode;

	/** {@inheritDoc} */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		mode = filterConfig.getInitParameter("mode");
		Assertion.checkState(ACCEPTED_MODES.contains(mode), "Mode de compatibilité IE non géré {0} (modes ok :{1})", mode, ACCEPTED_MODES);
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		((HttpServletResponse) response).setHeader("X-UA-Compatible", "IE=" + mode);
		chain.doFilter(request, response);
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		//rien
	}

}
