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
package io.vertigo.core.node.config.yaml;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import io.vertigo.core.lang.Assertion;

/**
 * TODO : à refaire?
 * @author pchretien
 */
final class YamlConfigParams {
	private final Properties properties;
	private final Set<String> keys;
	private final Set<String> readKeys = new HashSet<>();

	YamlConfigParams(final Properties properties) {
		Assertion.check().isNotNull(properties);
		//-----
		this.properties = properties;
		keys = new HashSet<>(properties.stringPropertyNames());
	}

	String getParam(final String paramName) {
		Assertion.check()
				.isNotBlank(paramName)
				.isTrue(properties.containsKey(paramName), "property '{0}' not found", paramName);
		//-----
		readKeys.add(paramName);
		return properties.getProperty(paramName);
	}

	Set<String> unreadProperties() {
		keys.removeAll(readKeys);
		return keys;
	}
}
