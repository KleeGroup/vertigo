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

import java.util.Objects;
import java.util.function.Supplier;

import io.vertigo.core.util.StringUtil;

/**
 * Assertions help us to build better code, with more robustness.
 *
 * Assertions have been introduced by  B.Meyer in a language called Eiffel.
 *
 * An assertion allows you to design by contract.
 * Each time an assertion fails, an specific exception is thrown.
 *
 * The following assertions help you to test if
 * - an object is noT null => isNotNull
 * - a string is not blank (and not null) =>isNotBlank
 * - an expression is true or false =>isTrue or isFalse
 *
 * You can have a condition before running an assertion => when
 * That's usefull when you want to test a pattern of an object that can be null.
 *
 * This assertion should be written in a fluent style to group all the assertions
 * into a single block of code.
 *
 * Assertion can define a message and args.
 * "hello {0}, an error occured on '{1}'", "foo", "bar"
 *  returns
 *  hello foo, an error occured on 'bar'
 *
 * You can use the simple quote ' inside the message.
 *
 * @author pchretien, fconstantin
 */
public final class Assertion {
	private static final Assertion INSTANCE = new Assertion();

	private Assertion() {
		super();
	}

	public static Assertion check() {
		return INSTANCE;
	}

	/**
	 * Evaluates an assertion when a condition is fulfilled.
	 *
	 * @param condition the condition to check the assertion
	 * @param assertionSupplier the assertion to check when the condition is fulfilled
	 * @return the current assertion
	 */
	public Assertion when(final boolean condition, final Supplier<Assertion> assertionSupplier) {
		if (condition) {
			isValid(assertionSupplier);
		}
		return INSTANCE;
	}

	/**
	 * Checks if an object is not null.
	 * Throws the famous NullPointerException if not.
	 *
	 * @param o the object
	 * @return the current assertion
	 */
	public Assertion isNotNull(final Object o) {
		Objects.requireNonNull(o);
		return this;
	}

	/**
	 * Checks if an object is not null.
	 * Throws the famous NullPointerException with a pretty message if not.
	 *
	 * @param o the object
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNotNull(final Object o, final String msg, final Object... params) {
		//Attention si o est un Boolean : il peut s'agir du resultat d'un test (boolean) qui a été autoboxé en Boolean
		Objects.requireNonNull(o, () -> StringUtil.format(msg, params));
		return this;
	}

	/**
	 * Checks if an object is null.
	 * Throws an IllegalStateException if not
	 * @param o the object
	 * @return the current assertion
	 */
	public Assertion isNull(final Object o) {
		return isNull(o, "this object must be null");
	}

	/**
	 * Checks if an object is null.
	 * Throws an illegalStateException with a pretty message if not.
	 *
	 * @param o the object
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNull(final Object o, final String msg, final Object... params) {
		if (o != null) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if a boolean expression is True
	 * Throws an IllegalStateException with a pretty message if not.
	 *
	 * @param test the boolean expression
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isTrue(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if a boolean expression is False
	 * Throws an IllegalStateException with a pretty message if not.
	 *
	 * @param test the boolean expression
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isFalse(final boolean test, final String msg, final Object... params) {
		return isTrue(!test, msg, params);
	}

	/**
	 * Checks if a string is not blank (and not empty).
	 * Throws an IllegalArgumentException if not.
	 *
	 * @param str the string
	 * @return the current assertion
	 */
	public Assertion isNotBlank(final String str) {
		return isNotBlank(str, "String must not be empty");
	}

	/**
	 * Checks if a string is not blank (and not empty).
	 * Throws an IllegalArgumentException with a pretty message if not.
	 *
	 * @param str the string
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNotBlank(final String str, final String msg, final Object... params) {
		isNotNull(str, msg, params);
		if (StringUtil.isBlank(str)) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if an assertion supplied is valid.
	 * Throws the Exception thrown by the assertion if not.
	 *
	 * @param assertionSupplier the assertion supplied
	 * @return the current assertion
	 */
	public Assertion isValid(final Supplier<Assertion> assertionSupplier) {
		assertionSupplier.get();
		return this;
	}
}
