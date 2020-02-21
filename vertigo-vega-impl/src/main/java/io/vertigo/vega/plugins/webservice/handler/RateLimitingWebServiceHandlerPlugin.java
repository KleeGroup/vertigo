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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.exception.TooManyRequestException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Request;
import spark.Response;

/**
 * Rate limit handler.
 * @author npiedeloup
 */
public final class RateLimitingWebServiceHandlerPlugin implements WebServiceHandlerPlugin, SimpleDefinitionProvider {
	private static final long DEFAULT_LIMIT_VALUE = 150; //the rate limit ceiling value
	private static final int DEFAULT_WINDOW_SECONDS = 5 * 60; //the time windows use to limit calls rate
	private static final String RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit"; //the rate limit ceiling for that given request
	private static final String RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining"; //the number of requests left for the M minute window
	private static final String RATE_LIMIT_RESET = "X-Rate-Limit-Reset"; //the remaining seconds before the rate limit resets

	private final VSecurityManager securityManager;
	private final int windowSeconds;
	private final long limitValue;

	/**
	 * Hit counter by userKey.
	 */
	private final ConcurrentMap<String, AtomicLong> hitsCounter = new ConcurrentHashMap<>();
	/**
	 * Last window start time.
	 */
	private long lastRateLimitResetTime = System.currentTimeMillis();

	/**
	 * Constructor.
	 * @param windowSeconds the time windows use to limit calls rate
	 * @param limitValue the rate limit ceiling value
	 * @param securityManager Security Manager
	 * @param daemonManager Manager des daemons
	 */
	@Inject
	public RateLimitingWebServiceHandlerPlugin(
			final VSecurityManager securityManager,
			final DaemonManager daemonManager,
			@ParamValue("windowSeconds") final Optional<Integer> windowSeconds,
			@ParamValue("limitValue") final Optional<Long> limitValue) {
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(limitValue);
		Assertion.checkNotNull(windowSeconds);
		//-----
		this.securityManager = securityManager;
		this.limitValue = limitValue.orElse(DEFAULT_LIMIT_VALUE);
		this.windowSeconds = windowSeconds.orElse(DEFAULT_WINDOW_SECONDS);
		//RateLimitingWebServiceHandlerPlugin::resetRateLimitWindow
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new DaemonDefinition("DmnRateLimitWindowReset", () -> () -> resetRateLimitWindow(), windowSeconds));
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		Assertion.checkNotNull(request);
		Assertion.checkNotNull(response);
		Assertion.checkNotNull(routeContext);
		Assertion.checkNotNull(chain);
		//-----
		final String userKey = obtainUserKey(request, securityManager.getCurrentUserSession());
		response.header(RATE_LIMIT_LIMIT, String.valueOf(limitValue));
		response.header(RATE_LIMIT_RESET, String.valueOf(windowSeconds - (System.currentTimeMillis() - lastRateLimitResetTime) / 1000));

		final long hits = touch(userKey);
		if (hits > limitValue) {
			throw new TooManyRequestException("Rate limit exceeded");
		}
		response.header(RATE_LIMIT_REMAINING, String.valueOf(limitValue - hits));
		return chain.handle(request, response, routeContext);
	}

	private static String obtainUserKey(final Request request, final Optional<UserSession> userSessionOpt) {
		return userSessionOpt
				.map(userSession -> userSession.getSessionUUID().toString())
				.orElseGet(() -> request.ip() + ":" + request.headers("user-agent"));
	}

	private long touch(final String userKey) {
		final AtomicLong value = new AtomicLong(0);
		final AtomicLong oldValue = hitsCounter.putIfAbsent(userKey, value);
		return (oldValue != null ? oldValue : value).incrementAndGet();
	}

	/**
	 * Reset current limitWindow.
	 */
	void resetRateLimitWindow() {
		hitsCounter.clear();
		lastRateLimitResetTime = System.currentTimeMillis();
	}

}
