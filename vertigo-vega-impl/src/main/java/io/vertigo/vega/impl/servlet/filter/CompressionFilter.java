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
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de javax.servlet.Filter utilisée pour compresser le flux de réponse si il d�passe un seuil,
 * et pour décompresser le flux d'entrée si nécessaire.
 * @author Amy Roh, Dmitri Valdin (Apache Software Foundation)
 */
public final class CompressionFilter extends AbstractFilter {
	private static final String GZIP = "gzip";

	private int compressionThreshold;

	private String userAgent;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		final FilterConfig filterConfig = getFilterConfig();
		if (filterConfig != null) { //NOPMD
			userAgent = filterConfig.getInitParameter("userAgent");
			final int minThreshold = 128;
			final String str = filterConfig.getInitParameter("compressionThreshold");
			if (str != null) { //NOPMD
				compressionThreshold = Integer.parseInt(str);
				if (compressionThreshold <= 0) {
					compressionThreshold = 0;
				} else if (compressionThreshold < minThreshold) {
					compressionThreshold = minThreshold;
				}
			} else {
				compressionThreshold = 0;
			}
		} else {
			compressionThreshold = 0;
		}
	}

	/**
	 * Retourne le User Agent de la requête HTML.
	 *
	 * @param request La requête HTML
	 * @return User Agent
	 */
	public String getUserAgent(final HttpServletRequest request) {
		return userAgent != null ? request.getHeader("user-agent") : null; //NOPMD
	}

	/**
	 * Détermine si l'User Agent est inactif ou la compression inactive.
	 *
	 * @param reqGzip reqGzip
	 * @param reqUserAgent reqUserAgent
	 * @return boolean
	 */
	public boolean isUserAgentNullOrCompressionNull(final String reqGzip, final String reqUserAgent) {
		return compressionThreshold == 0 || "false".equalsIgnoreCase(reqGzip) || reqUserAgent != null && !reqUserAgent.contains(userAgent);
	}

	/**
	 * La méthode doFilter est appelée par le container chaque fois qu'une paire requête/réponse passe à travers
	 * la chaîne suite à une requête d'un client pour une ressource au bout de la chaîne.
	 * L'instance de FilterChain pass�e dans cette méthode permet au filtre de passer la requête et la réponse
	 * à l'entité suivante dans la chaîne.
	 *
	 * Le flux d'entrée est encapsul� pour décompression si son Content-Encoding est gzip. Le flux de sortie est
	 * encapsul� pour compression si le nombre d'octets �crits (dans un buffer au début) d�passe le paramètre de filtre
	 * compressionThreshold.
	 *
	 * @param req javax.servlet.ServletRequest
	 * @param res javax.servlet.ServletResponse
	 * @param chain javax.servlet.FilterChain
	 * @throws java.io.IOException   Si une erreur d'entrée/sortie survient
	 * @throws javax.servlet.ServletException   Si une erreur de servlet survient
	 **/
	@Override
	public void doMyFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
			chain.doFilter(req, res);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		// Is request stream compressed ?
		if (GZIP.equalsIgnoreCase(request.getHeader("Content-Encoding"))) {
			request = new CompressionServletRequestWrapper(request);
		}

		// Are we allowed to compress response stream ?
		final String reqUserAgent = getUserAgent(request);
		String reqGzip = request.getParameter(GZIP);
		if (reqGzip == null) {
			reqGzip = request.getHeader(GZIP);
		}
		if (isUserAgentNullOrCompressionNull(reqGzip, reqUserAgent)) {
			chain.doFilter(request, response);
			return;
		}

		boolean supportCompression = false;
		String name;
		final Enumeration<String> en = request.getHeaders("Accept-Encoding");
		while (en.hasMoreElements()) {
			name = en.nextElement();
			if (name.contains(GZIP)) {
				supportCompression = true;
				break;
			}
		}

		if (supportCompression) {
			// compress response stream
			try (final CompressionServletResponseWrapper wrappedResponse = new CompressionServletResponseWrapper(response, compressionThreshold)) {
				chain.doFilter(request, wrappedResponse);
			}
		} else {
			chain.doFilter(request, response);
		}
	}
}
