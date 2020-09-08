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
package io.vertigo.core.node.config;

import io.vertigo.core.lang.Assertion;

/**
 * LogConfile is the unique point used to configure Log.
 * Logas are configured using log4J.
 * So you have to define the place to read the log4j config file.
 *
 * @author pchretien
 */
public final class LogConfig {
	private final String fileName;

	/**
	 * Constructor.
	 * @param fileName the log4J.xml fileName
	 */
	public LogConfig(final String fileName) {
		Assertion.check().isNotBlank(fileName);
		//-----
		this.fileName = fileName;

	}

	/**
	 * @return the log4J.xml fileName
	 */
	public String getFileName() {
		return fileName;
	}
}
