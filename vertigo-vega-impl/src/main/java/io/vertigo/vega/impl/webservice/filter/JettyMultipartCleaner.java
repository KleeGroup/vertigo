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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.MultiParts;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Filter to configure MultipartConfigElement for Jetty Request.
 * @author npiedeloup
 */
public final class JettyMultipartCleaner implements Filter {
	private static final String JETTY_MULTIPARTS = org.eclipse.jetty.server.Request.__MULTIPARTS;//"org.eclipse.multipartConfig";
	private static final Logger LOG = LogManager.getLogger(JettyMultipartCleaner.class);

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		final MultiParts multiParts = (MultiParts) request.raw()
				.getAttribute(JETTY_MULTIPARTS);
		if (multiParts != null && !multiParts.isEmpty()) {
			try {
				// a multipart request to a servlet will have the parts cleaned up correctly, but
				// the repeated call to deleteParts() here will safely do nothing.
				multiParts.close();
			} catch (final IOException e) {
				LOG.warn("Error while deleting multipart request parts", e);
			}
		}
	}
}
