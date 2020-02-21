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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import spark.QueryParamsMap;
import spark.Request;
import spark.Session;
import spark.utils.IOUtils;

/**
 * Wrapping SparkRequest to support UTF-8.
 *
 * @author npiedeloup
 */
final class SparkJavaRequestWrapper extends Request {
	private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SparkJavaRequestWrapper.class);

	private final Request innerRequest;
	private final String defaultContentCharset;
	private String body;

	/**
	 * Creates a new KRequestWrapper object.
	 * @param innerRequest Inner request
	 * @param defaultContentCharset Default content charset (if not in content-type header)
	 */
	SparkJavaRequestWrapper(final Request innerRequest, final String defaultContentCharset) {
		this.innerRequest = innerRequest;
		this.defaultContentCharset = defaultContentCharset;
	}

	/** {@inheritDoc} */
	@Override
	public String params(final String param) {
		return innerRequest.params(param);
	}

	/** {@inheritDoc} */
	@Override
	public String[] splat() {
		return innerRequest.splat();
	}

	/** {@inheritDoc} */
	@Override
	public String requestMethod() {
		return innerRequest.requestMethod();
	}

	/** {@inheritDoc} */
	@Override
	public String scheme() {
		return innerRequest.scheme();
	}

	/** {@inheritDoc} */
	@Override
	public String host() {
		return innerRequest.host();
	}

	/** {@inheritDoc} */
	@Override
	public String userAgent() {
		return innerRequest.userAgent();
	}

	/** {@inheritDoc} */
	@Override
	public int port() {
		return innerRequest.port();
	}

	/** {@inheritDoc} */
	@Override
	public String pathInfo() {
		return innerRequest.pathInfo();
	}

	/** {@inheritDoc} */
	@Override
	public String servletPath() {
		return innerRequest.servletPath();
	}

	/** {@inheritDoc} */
	@Override
	public String contextPath() {
		return innerRequest.contextPath();
	}

	/** {@inheritDoc} */
	@Override
	public String url() {
		return innerRequest.url();
	}

	/** {@inheritDoc} */
	@Override
	public String contentType() {
		return innerRequest.contentType();
	}

	/** {@inheritDoc} */
	@Override
	public String ip() {
		return innerRequest.ip();
	}

	/** {@inheritDoc} */
	@Override
	public String body() {
		if (body == null) {
			try {
				body = ioUtilstoString(raw().getInputStream(), getContentCharset()); //we can't override IOUtils static method
			} catch (final Exception e) {
				LOG.warn("Exception when reading body", e);
			}
		}
		return body;
	}

	private static String ioUtilstoString(final InputStream input, final String contentCharset) throws IOException {
		final StringWriter sw = new StringWriter();
		IOUtils.copy(new InputStreamReader(input, contentCharset), sw); //set charset on InputStreamReader
		return sw.toString();
	}

	private String getContentCharset() {
		final Matcher m = CHARSET_PATTERN.matcher(contentType());
		if (m.find()) {
			return m.group(1).trim().toUpperCase(Locale.ENGLISH);
		}
		return defaultContentCharset;
	}

	/** {@inheritDoc} */
	@Override
	public int contentLength() {
		return innerRequest.contentLength();
	}

	/** {@inheritDoc} */
	@Override
	public String queryParams(final String queryParam) {
		return innerRequest.queryParams(queryParam);
	}

	/** {@inheritDoc} */
	@Override
	public String headers(final String header) {
		return innerRequest.headers(header);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> queryParams() {
		return innerRequest.queryParams();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> headers() {
		return innerRequest.headers();
	}

	/** {@inheritDoc} */
	@Override
	public String queryString() {
		return innerRequest.queryString();
	}

	/** {@inheritDoc} */
	@Override
	public void attribute(final String attribute, final Object value) {
		innerRequest.attribute(attribute, value);
	}

	/** {@inheritDoc} */
	@Override
	public Object attribute(final String attribute) {
		return innerRequest.attribute(attribute);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> attributes() {
		return innerRequest.attributes();
	}

	/** {@inheritDoc} */
	@Override
	public HttpServletRequest raw() {
		return innerRequest.raw();
	}

	/** {@inheritDoc} */
	@Override
	public QueryParamsMap queryMap() {
		return innerRequest.queryMap();
	}

	/** {@inheritDoc} */
	@Override
	public QueryParamsMap queryMap(final String key) {
		return innerRequest.queryMap(key);
	}

	/** {@inheritDoc} */
	@Override
	public Session session() {
		return innerRequest.session();
	}

	/** {@inheritDoc} */
	@Override
	public Session session(final boolean create) {
		return innerRequest.session(create);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> cookies() {
		return innerRequest.cookies();
	}

	/** {@inheritDoc} */
	@Override
	public String cookie(final String name) {
		return innerRequest.cookie(name);
	}

}
