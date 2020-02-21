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
package io.vertigo.vega.webservice;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.restassured.RestAssured;
import io.vertigo.vega.webservice.data.MyNodeConfig;

public final class WebServiceManagerServletTest extends AbstractWebServiceManagerTest {
	static {
		//RestAsssured init
		RestAssured.port = MyNodeConfig.WS_PORT;
	}

	private static Server server;

	@BeforeAll
	public static void setUp() throws Exception {
		startServer();
	}

	@AfterAll
	public static void tearDown() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	private static ClassLoader getUrlClassLoader() {
		return new URLClassLoader(new URL[0], WebServiceManagerServletTest.class.getClassLoader());
	}

	private static void startServer() throws IOException, Exception {
		server = new Server(MyNodeConfig.WS_PORT);
		final WebAppContext context = new WebAppContext(WebServiceManagerServletTest.class.getClassLoader().getResource("io/vertigo/vega/testWebApp/").getFile(), "/");
		System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
		context.setAttribute("jacoco.exclClassLoaders", "*");
		context.setAttribute("javax.servlet.context.tempdir", getScratchDir());
		context.addBean(new ServletContainerInitializersStarter(context), true);
		context.setClassLoader(getUrlClassLoader());
		context.setClassLoader(new WebAppClassLoader(WebServiceManagerServletTest.class.getClassLoader(), context));

		server.setHandler(context);
		server.start();
	}

	private static File getScratchDir() throws IOException {
		final File tempDir = new File(System.getProperty("java.io.tmpdir"));
		final File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

		if (!scratchDir.exists()) {
			if (!scratchDir.mkdirs()) {
				throw new IOException("Unable to create scratch directory: " + scratchDir);
			}
		}
		return scratchDir;
	}
}
