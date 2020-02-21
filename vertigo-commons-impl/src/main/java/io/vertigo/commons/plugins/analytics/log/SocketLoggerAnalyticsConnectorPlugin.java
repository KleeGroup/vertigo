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
package io.vertigo.commons.plugins.analytics.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.SerializedLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.health.HealthCheck;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.analytics.process.AProcess;
import io.vertigo.commons.daemon.DaemonScheduled;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;

/**
 * Processes connector which use the log4j SocketAppender.
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {
	private static final Gson GSON = new GsonBuilder().create();
	private static final int DEFAULT_SERVER_PORT = 4562;// DefaultPort of SocketAppender 4650 for log4j and 4562 for log4j2

	private Logger socketProcessLogger;
	private Logger socketHealthLogger;
	private Logger socketMetricLogger;
	private final String hostName;
	private final int port;

	private final String appName;
	private final String localHostName;

	private final ConcurrentLinkedQueue<AProcess> processQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Constructor.
	 * @param appNameOpt the app name
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 */
	@Inject
	public SocketLoggerAnalyticsConnectorPlugin(
			@ParamValue("appName") final Optional<String> appNameOpt,
			@ParamValue("hostName") final Optional<String> hostNameOpt,
			@ParamValue("port") final Optional<Integer> portOpt) {
		Assertion.checkNotNull(appNameOpt);
		Assertion.checkNotNull(hostNameOpt);
		Assertion.checkNotNull(portOpt);
		// ---
		appName = appNameOpt.orElseGet(() -> Home.getApp().getNodeConfig().getAppName());
		hostName = hostNameOpt.orElse("analytica.part.klee.lan.net");
		port = portOpt.orElse(DEFAULT_SERVER_PORT);
		localHostName = retrieveHostName();
	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		Assertion.checkNotNull(process);
		//---
		processQueue.add(process);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final Metric metric) {
		if (socketMetricLogger == null) {
			socketMetricLogger = createLogger("vertigo-analytics-metric", hostName, port);
		}
		sendObject(metric, socketMetricLogger);

	}

	/** {@inheritDoc} */
	@Override
	public void add(final HealthCheck healthCheck) {
		if (socketHealthLogger == null) {
			socketHealthLogger = createLogger("vertigo-analytics-health", hostName, port);
		}
		sendObject(healthCheck, socketHealthLogger);

	}

	private static String retrieveHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			LogManager.getRootLogger().info("Cannot retrieve hostname", e);
			return "UnknownHost";
		}
	}

	private static Logger createLogger(final String loggerName, final String hostName, final int port) {
		// If it doesn't exist we create it with the right appender
		final Logger logger = LogManager.getLogger(loggerName);
		//we create appender
		final SocketAppender appender = SocketAppender.newBuilder()
				.withName("socketAnalytics")
				.withLayout(SerializedLayout.createLayout())
				.withHost(hostName)
				.withPort(port)
				.withReconnectDelayMillis(0)// we make only one try
				.build();
		appender.start();

		final LoggerContext context = LoggerContext.getContext(false); //on ne close pas : car ca stop le context
		final Configuration config = context.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
		loggerConfig.getAppenders().keySet().forEach(appenderName -> loggerConfig.removeAppender(appenderName));
		loggerConfig.addAppender(appender, null, null);

		Configurator.setLevel(loggerName, Level.INFO);
		return logger;
	}

	/**
	 * Daemon to unstack processes to end them
	 */
	@DaemonScheduled(name = "DmnRemoteLogger", periodInSeconds = 1, analytics = false)
	public void pollQueue() {
		while (!processQueue.isEmpty()) {
			final AProcess head = processQueue.poll();
			if (head != null) {
				sendProcess(head);
			}
		}

	}

	private void sendProcess(final AProcess process) {
		if (socketProcessLogger == null) {
			socketProcessLogger = createLogger("vertigo-analytics-process", hostName, port);
		}
		sendObject(process, socketProcessLogger);
	}

	private void sendObject(final Object object, final Logger logger) {

		if (logger.isInfoEnabled()) {
			final JsonObject log = new JsonObject();
			log.addProperty("appName", appName);
			log.addProperty("host", localHostName);
			log.add("event", GSON.toJsonTree(object));
			logger.info(GSON.toJson(log));
		}
	}

}
