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
package io.vertigo.core.node.component.di;

/**
 * Thrown if an exception occures during a method concerning dependency injection.
 *
 * @author pchretien
 */
final class DIException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param msg messagae
	 */
	DIException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * @param msg message
	 * @param t cause exception
	 */
	DIException(final String msg, final Throwable t) {
		super(msg, t);
	}
}
