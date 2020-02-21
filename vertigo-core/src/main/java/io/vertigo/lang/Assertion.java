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

import java.util.Objects;
import java.util.function.BooleanSupplier;

import io.vertigo.util.StringUtil;

/**
 * Assertions have been introduced by  B.Meyer with a language called Eiffel.
 *
 * An assertion allows you to design by contract.
 * Each time an assetion fails, an specific exception is thrown.
 * - checkNotNull     throws NullPointerException
 * - checkArgument    throws IllegalArgumentException
 * - checkArgNotEmpty throws IllegalArgumentException
 * - checkState       throws IllegalStateException
 *
 * Assertion can define a message and args.
 * "hello {0}, an error occured on '{1}'", "foo", "bar"
 *  hello foo, an error occured on 'bar'
 *
 * You can use ' inside the message.
 *
 *
 * @author fconstantin
 */
public final class Assertion {
	private Assertion() {
		//private constructor
	}

	/**
	 * Check if an object is not null.
	 * If not a generic exception is thrown.
	 * @param o Object object  that must be not null
	 */
	public static void checkNotNull(final Object o) {
		Objects.requireNonNull(o);
	}

	/**
	 * Check if an object is not null.
	 * If not an exception with a contextual message is thrown.
	 *
	 * @param o Object object that must be not null
	 * @param msg Error message
	 * @param params params of the message
	 */
	public static void checkNotNull(final Object o, final String msg, final Object... params) {
		//Attention si o est un Boolean : il peut s'agir du resultat d'un test (boolean) qui a été autoboxé en Boolean
		Objects.requireNonNull(o, () -> StringUtil.format(msg, params));
	}

	/**
	 * Check if a test is valid.
	 * If not an exception with a contextual message is thrown.
	 *
	 * @param test If the assertion succeeds
	 * @param msg Error message
	 * @param params params of the message
	 */
	public static void checkArgument(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
	}

	/**
	 * Check if a string is not empty.
	 * If not an generic exception is thrown.
	 *
	 * @param str String that must be not empty
	 */
	public static void checkArgNotEmpty(final String str) {
		checkNotNull(str);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException("String must not be empty");
		}
	}

	/**
	 * Check if a string is not empty.
	 * @param str String that must be not empty
	 * @param msg Error message
	 * @param params params of the message
	 */
	public static void checkArgNotEmpty(final String str, final String msg, final Object... params) {
		checkNotNull(str, msg, params);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
	}

	/**
	 * Check if a state is valid.
	 * This assertion should be used inside a processing to check a step.
	 *
	 * @param test If the assertion succeeds
	 * @param msg Error message
	 * @param params params of the message
	 */
	public static void checkState(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
	}

	/**
	 * @param ifCondition condition of this assertion
	 * @return Assertion to check if condition is true
	 */
	public static ConditionalAssertion when(final boolean ifCondition) {
		return (test, msg, params) -> {
			if (ifCondition) {
				Assertion.checkState(test.getAsBoolean(), msg, params);
			}
		};
	}

	/**
	 * Function to assert when a condition if fulfilled.
	 * @author npiedeloup
	 */
	@FunctionalInterface
	public interface ConditionalAssertion {
		/**
		 * Assert something if test return null
		 * @param test BooleanSupplier Check if a state is valid.
		 * @param msg Message Error message
		 * @param params params of the message
		 */
		void check(final BooleanSupplier test, final String msg, final Object... params);
	}
}
