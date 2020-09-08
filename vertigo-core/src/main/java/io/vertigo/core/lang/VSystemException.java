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
package io.vertigo.core.lang;

import io.vertigo.core.util.StringUtil;

/**
 * This class defines a Vertigo system exception.
 *
 * @author fconstantin, pchretien, npiedeloup
 */
public class VSystemException extends RuntimeException {
	private static final long serialVersionUID = 8367574550413643134L;

	/**
	 * Constructor.
	 * @param msg  the message
	 * @param params the params
	 */
	public VSystemException(final String msg, final Object... params) {
		super(StringUtil.format(msg, params));
	}

	/**
	 * Constructor.
	 * @param e the cause exception
	 * @param msg  the message
	 * @param params the params
	 */
	public VSystemException(final Exception e, final String msg, final Object... params) {
		super(StringUtil.format(msg, params), e);
	}
}
