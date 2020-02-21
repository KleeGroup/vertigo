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
package io.vertigo.vega.impl.webservice.filter;

import javax.servlet.MultipartConfigElement;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Filter to configure MultipartConfigElement for Jetty Request.
 * @author npiedeloup
 */
public final class JettyMultipartConfig implements Filter {
	private static final long MAX_PARTS_SIZE = 30 * 1024 * 1024L;
	private static final int MAX_NB_PARTS = 5;
	private static final int MAX_PART_SIZE_IN_MEMORY = 50 * 1024;
	private static final String JETTY_CONFIG_ATTRIBUTE = org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT;//"org.eclipse.multipartConfig";
	private final MultipartConfigElement multipartConfigElement;

	/**
	 * Constructor.
	 * @param tempPath path for uploaded tempfiles
	 */
	public JettyMultipartConfig(final String tempPath) {
		multipartConfigElement = new MultipartConfigElement(tempPath, MAX_PARTS_SIZE, MAX_NB_PARTS * MAX_PARTS_SIZE, MAX_PART_SIZE_IN_MEMORY);
	}

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		request.raw().setAttribute(JETTY_CONFIG_ATTRIBUTE, multipartConfigElement);

	}

}
