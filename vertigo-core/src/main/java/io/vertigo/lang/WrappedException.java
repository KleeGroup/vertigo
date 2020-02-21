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

import java.lang.reflect.InvocationTargetException;

import io.vertigo.util.StringUtil;

/**
 * Encapsulates checked Exception inside a RuntimeException.
 * Inspired by gnu.mapping.WrappedException.
 *
 * @author npiedeloup
 */
public final class WrappedException extends RuntimeException {
	private static final long serialVersionUID = 8595187765435824071L;

	/**
	 * Constructor.
	 * @param message the context message
	 * @param cause the cause exception
	 */
	private WrappedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Coerces argument to a RuntimeException.
	 * Re-throws as a non-checked exception.
	 * This method never returns, in spite of the return type.
	 * This allows the call to be written as: throw WrappedExcepton.rethrow(th) so javac and the verifier can know the code doesn't return.
	 * @param th Cause exception
	 * @return RuntimeException runtime
	 */
	public static RuntimeException wrap(final Throwable th) {
		return wrap(th, th.getMessage());
	}

	/**
	 * Coerces argument to a RuntimeException.
	 * Re-throw as a non-checked exception. This method never returns, in spite of the return type.
	 * This allows the call to be written as: throw WrappedExcepton.rethrow(th) so javac and the verifier can know the code doesn't return.
	 * @param th Cause exception
	 * @param msg Context message
	 * @param params Context message params
	 * @return RuntimeException runtime
	 */
	public static RuntimeException wrap(final Throwable th, final String msg, final Object... params) {
		Assertion.checkArgNotEmpty(msg);
		//---
		final Throwable t;
		if (th instanceof InvocationTargetException) {
			t = ((InvocationTargetException) th).getTargetException();
		} else {
			t = th;
		}

		final String message = msg != null ? StringUtil.format(msg, params) : null;
		if (t instanceof RuntimeException) {
			t.addSuppressed(new VSystemException(message));
			throw (RuntimeException) t;
		}
		if (t instanceof Error) {
			t.addSuppressed(new VSystemException(message));
			throw (Error) t;
		}
		throw new WrappedException(message, t);
	}

	/**
	 * Gets the orginal exception.
	 * @return the orginal exception that has been wrapped
	 */
	public Throwable unwrap() {
		return getCause();
	}
}
