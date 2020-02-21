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
package io.vertigo.vega.impl.webservice.catalog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.app.Home;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.SessionLess;

/**
 * Swagger WebService to list services published.
 * @see "https://github.com/wordnik/swagger-spec/blob/master/versions/2.0.md"
 * @author npiedeloup (22 juil. 2014 11:12:02)
 */
public final class SwaggerWebServices implements WebServices {
	private static final String[][] SUPPORTED_CONTENT_TYPE = {
			{ ".html", "text/html" },
			{ ".css", "text/css" },
			{ ".js", "application/x-javascript" },
			{ ".png", "image/png" },
			{ ".gif", "image/gif" }
	};

	/**
	 * @param request HttpRequest
	 * @return Api representation in Swagger definition
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerApi")
	public Map<String, Object> getSwapperApi(final HttpServletRequest request) {
		//compute contextPath + servletPath - current Ws url
		// Spark override getRequestURI and removed contextPath from it, so we put it manualy
		final String prefixUrl = (request.getContextPath() != null ? request.getContextPath() : "")
				+ (request.getRequestURI().substring(0, request.getRequestURI().indexOf("/swaggerApi")));
		return new SwaggerApiBuilder()
				.withContextPath(prefixUrl)
				.withWebServiceDefinitions(Home.getApp().getDefinitionSpace().getAll(WebServiceDefinition.class))
				.build();
	}

	/**
	 * Redirect to index.html.
	 * @param response HttpResponse
	 * @throws IOException Exception
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi")
	public void getSwapperUi(final HttpServletResponse response) throws IOException {
		response.sendRedirect("./swaggerUi/index.html");
	}

	/**
	 * Redirect to index.html.
	 * @param response HttpResponse
	 * @throws IOException Exception
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/")
	public void getSwapperUiEmpty(final HttpServletResponse response) throws IOException {
		response.sendRedirect("./index.html");
	}

	/**
	 * Return a swagger static resources.
	 * @param resourceUrl Resource name
	 * @param response HttpResponse
	 * @throws IOException Exception
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/{resourceUrl}")
	public void getSwapperUi(@PathParam("resourceUrl") final String resourceUrl, final HttpServletResponse response) throws IOException {
		FileUtil.checkUserFileName(resourceUrl);
		//----
		if (resourceUrl.isEmpty()) {
			response.sendRedirect("./index.html");
		}
		final URL url = SwaggerWebServices.class.getResource("/swagger-site/" + resourceUrl);
		sendFile(url, resolveContentType(resourceUrl), response, resourceUrl);
	}

	/**
	 * Return a swagger static resources.
	 * @param resourcePathUrl Resource path
	 * @param resourceUrl Resource name
	 * @param response HttpResponse
	 * @throws IOException Exception
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/{resourcePathUrl}/{resourceUrl}")
	public void getSwapperUi(@PathParam("resourcePathUrl") final String resourcePathUrl, @PathParam("resourceUrl") final String resourceUrl, final HttpServletResponse response) throws IOException {
		FileUtil.checkUserPath(resourcePathUrl);
		FileUtil.checkUserFileName(resourceUrl);
		//----
		final URL url = SwaggerWebServices.class.getResource("/swagger-site/" + resourcePathUrl + "/" + resourceUrl);
		sendFile(url, resolveContentType(resourceUrl), response, resourceUrl);
	}

	private static void sendFile(final URL url, final String contentType, final HttpServletResponse response, final String resourceName) throws IOException {
		if (url != null) {
			final URLConnection connection = url.openConnection();
			connection.connect();
			response.setContentLength(connection.getContentLength());
			response.setDateHeader("Last-Modified", connection.getLastModified());
			response.setContentType(contentType != null ? contentType : connection.getContentType());
			try (final BufferedInputStream bInput = new BufferedInputStream(connection.getInputStream())) {
				try (final OutputStream output = response.getOutputStream()) {
					copy(bInput, output);
				}
			}
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			//send a content otherwise Jetty change status to 204
			try (final OutputStream output = response.getOutputStream()) {
				output.write((resourceName + " not found").getBytes(StandardCharsets.ISO_8859_1.name()));
			}
		}
	}

	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

	private static String resolveContentType(final String resourceUrl) {
		for (final String[] entry : SUPPORTED_CONTENT_TYPE) {
			if (resourceUrl.endsWith(entry[0])) {
				return entry[1];
			}
		}
		return null;
	}

}
