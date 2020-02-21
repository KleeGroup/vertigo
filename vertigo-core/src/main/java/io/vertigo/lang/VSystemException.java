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
package io.vertigo.lang;

import io.vertigo.util.StringUtil;

/**
 * This class defines a Vertigo system exception.
 *
 * @author fconstantin, pchretien, npiedeloup
 */
public class VSystemException extends RuntimeException {
	private static final long serialVersionUID = -2256807194400285743L;

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
