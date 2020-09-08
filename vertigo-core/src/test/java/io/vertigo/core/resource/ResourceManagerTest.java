/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.local.LocalResourceResolverPlugin;
import io.vertigo.core.plugins.resource.url.URLResourceResolverPlugin;

/**
 * @author pchretien
 */
public final class ResourceManagerTest extends AbstractTestCaseJU5 {
	@Inject
	private ResourceManager resourceManager;
	final String locales = "fr_FR";

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales(locales)
						.addPlugin(LocalResourceResolverPlugin.class)
						.addPlugin(URLResourceResolverPlugin.class)
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.build();
	}

	@Test
	public void testNullResource() {
		Assertions.assertThrows(NullPointerException.class,
				() -> resourceManager.resolve(null));

	}

	@Test
	public void testEmptyResource() {
		Assertions.assertThrows(RuntimeException.class,
				() -> resourceManager.resolve("nothing"));
	}

	@Test
	public void testClassPathResourceSelector() {
		final String expected = "io/vertigo/core/resource/hello.properties";
		final URL url = resourceManager.resolve(expected);
		assertTrue(url.getPath().contains(expected));
	}

	@Test
	public void testLocalResourceSelector() {
		final String filePath = "src/test/java/io/vertigo/core/resource/hello.properties";
		//final String espected = "file:" + filePath;
		final URL url = resourceManager.resolve(filePath);
		assertTrue(url.getPath().contains(filePath));
	}

	@Test
	public void testURLResourceSelector() {
		final String expectedPath = "/vertigo-io/vertigo";
		final String expected = "https://github.com" + expectedPath;
		final URL url = resourceManager.resolve(expected);
		assertEquals(expectedPath, url.getPath());
	}
}
