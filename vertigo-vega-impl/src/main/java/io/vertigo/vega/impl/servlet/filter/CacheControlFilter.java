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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de javax.servlet.Filter utilisée pour contrôler la mise en cache dans le navigateur client.
 * <br>Note : Une "limitation de la sécurité" de MSIE 5.5 (bug non présent dans Firefox) n'accepte
 * ni Cache-Control=no-cache, ni Pragma=no-cache en SSL sur les téléchargements de fichiers pdf, doc, xls, xml :
 * remplacer Cache-Control=no-cache par max-age=1.
 * @author Emeric Vernat
 */
public final class CacheControlFilter extends AbstractFilter {

	private static final String FORCE_OVERRIDE = "force-override";
	private boolean forceOverride; //false by default
	private Map<String, String> headers;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		final FilterConfig filterConfig = getFilterConfig();
		if (filterConfig != null) {
			final Map<String, String> tmp = new HashMap<>();
			String name;
			String value;
			for (final Enumeration en = filterConfig.getInitParameterNames(); en.hasMoreElements();) {
				name = (String) en.nextElement();
				if (FORCE_OVERRIDE.equals(name)) {
					forceOverride = Boolean.parseBoolean(filterConfig.getInitParameter(name));
				} else if (!EXCLUDE_PATTERN_PARAM_NAME.equals(name)) {
					value = filterConfig.getInitParameter(name);
					tmp.put(name, value);
				} else {
					//EXCLUDE_PATTERN_PARAM already process by AbstractFilter
				}
			}
			headers = Collections.unmodifiableMap(tmp);
		}
	}

	/**
	 * La méthode doMyFilter est appelée par le container chaque fois qu'une paire requête/réponse passe à travers
	 * la chaîne suite à une requête d'un client pour une ressource au bout de la chaîne.
	 * L'instance de FilterChain passée dans cette méthode permet au filtre de passer la requête et la réponse
	 * à l'entité suivante dans la chaîne.
	 *
	 * Cette implémentation ajoute en headers http les paramètres d'initialisation définit dans la configuration
	 * du filtre (voir configuration de la webapp). Typiquement, cela permet d'ajouter des headers pour contr�ler
	 * la mise en cache sur le navigateur client en fonction du type de contenu demandé. Par exemple :
	 * "Cache-Control: no-cache" ou "Cache-Control: max-age=3600" suivant l'instance de filtre.
	 *
	 * @param req javax.servlet.ServletRequest
	 * @param res javax.servlet.ServletResponse
	 * @param chain javax.servlet.FilterChain
	 * @throws java.io.IOException   Si une erreur d'entrée/sortie survient
	 * @throws javax.servlet.ServletException   Si une erreur de servlet survient
	 */
	@Override
	public void doMyFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse) || headers.isEmpty()
				|| "POST".equals(((HttpServletRequest) req).getMethod())) {
			chain.doFilter(req, res);
			return;
		}

		final HttpServletRequest httpRequest = (HttpServletRequest) req;
		final HttpServletResponse httpResponse = (HttpServletResponse) res;
		final Set<String> alreadySetHeaders = new HashSet<>(Collections.list(httpRequest.getHeaderNames()));
		if (forceOverride || !alreadySetHeaders.contains("Cache-Control")) {
			headers.forEach((k, v) -> httpResponse.setHeader(k, v));
		}
		chain.doFilter(httpRequest, httpResponse);
	}
}
