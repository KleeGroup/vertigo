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
 * A resource is defined by
 * - a type
 * - a path
 * A resource can be a file, a blob or a simple java class.
 * A resource is used to configure a module.
 *
 * @author pchretien
 */
public final class DefinitionResourceConfig {
	private final String type;
	private final String path;

	DefinitionResourceConfig(final String type, final String path) {
		Assertion.check()
				.isNotBlank(type)
				.isNotBlank(path);
		//-----
		this.type = type;
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "{ type: " + type + ", path: " + path + " }";
	}
}
