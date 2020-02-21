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
package io.vertigo.dynamo.domain.constraint;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.dynamox.domain.constraint.ConstraintBigDecimal;
import io.vertigo.dynamox.domain.constraint.ConstraintBigDecimalLength;
import io.vertigo.dynamox.domain.constraint.ConstraintDoubleLength;
import io.vertigo.dynamox.domain.constraint.ConstraintIntegerLength;
import io.vertigo.dynamox.domain.constraint.ConstraintLongLength;
import io.vertigo.dynamox.domain.constraint.ConstraintRegex;
import io.vertigo.dynamox.domain.constraint.ConstraintStringLength;

/**
 * Test des contraintes.
 *
 * @author pchretien
 */
public final class ConstraintTest extends AbstractTestCaseJU5 {
	//private ConstraintNotNull constraintNotNull;

	private ConstraintBigDecimal constraintBigDecimal;
	private ConstraintBigDecimalLength constraintBigDecimalLength;
	private ConstraintDoubleLength constraintDoubleLength;
	private ConstraintIntegerLength constraintIntegerLength;
	private ConstraintLongLength constraintLongLength;

	private ConstraintStringLength constraintStringLength;

	private ConstraintRegex constraintRegex;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.endBoot()
				.build();
	}

	/** {@inheritDoc} */
	@Override
	public void doSetUp() {
		//		constraintNotNull = new ConstraintNotNull();
		//		constraintNotNull.initParameters(null);

		constraintBigDecimal = new ConstraintBigDecimal("5,2");

		constraintBigDecimalLength = new ConstraintBigDecimalLength("3"); //10^3

		constraintDoubleLength = new ConstraintDoubleLength("3"); //10^3

		constraintIntegerLength = new ConstraintIntegerLength("3"); //10^3

		constraintLongLength = new ConstraintLongLength("3"); //10^3

		constraintStringLength = new ConstraintStringLength("3"); //10^3

		// \w signifie WORD [A-Za-z0-9_] et on ajoute le tiret -
		constraintRegex = new ConstraintRegex("[\\w-]*");
	}

	private void testBDTrue(final BigDecimal value) {
		Assertions.assertTrue(constraintBigDecimal.checkConstraint(value));
	}

	private void testBDFalse(final BigDecimal value) {
		Assertions.assertFalse(constraintBigDecimal.checkConstraint(value));
	}

	private void testBDLengthTrue(final BigDecimal value) {
		Assertions.assertTrue(constraintBigDecimalLength.checkConstraint(value));
	}

	private void testBDLengthFalse(final BigDecimal value) {
		Assertions.assertFalse(constraintBigDecimalLength.checkConstraint(value));
	}

	private void testDoubleTrue(final Double value) {
		Assertions.assertTrue(constraintDoubleLength.checkConstraint(value));
	}

	private void testDoubleFalse(final Double value) {
		Assertions.assertFalse(constraintDoubleLength.checkConstraint(value));
	}

	private void testIntegerTrue(final Integer value) {
		Assertions.assertTrue(constraintIntegerLength.checkConstraint(value));
	}

	private void testIntegerFalse(final Integer value) {
		Assertions.assertFalse(constraintIntegerLength.checkConstraint(value));
	}

	private void testLongTrue(final Long value) {
		Assertions.assertTrue(constraintLongLength.checkConstraint(value));
	}

	private void testLongFalse(final Long value) {
		Assertions.assertFalse(constraintLongLength.checkConstraint(value));
	}

	private void testStringTrue(final String value) {
		Assertions.assertTrue(constraintStringLength.checkConstraint(value));
	}

	private void testStringFalse(final String value) {
		Assertions.assertFalse(constraintStringLength.checkConstraint(value));
	}

	/**
	 * Test de ConstraintNotNull.
	 */
	@Test
	public void testConstraintNotNull() {
		//		assertFalse(constraintNotNull.checkConstraint(null));
		//		assertFalse(constraintNotNull.checkConstraint(null, KDataType.Date));
		//		assertTrue(constraintNotNull.checkConstraint(true, KDataType.Date));
		//		assertTrue(constraintNotNull.checkConstraint(123));
		//		assertTrue(constraintNotNull.checkConstraint(new Date(), KDataType.Date));
	}

	/**
	 * Test de constraintBigDecimal.
	 */
	@Test
	public void testConstraintBigDecimal() {
		BigDecimal bd;

		testBDTrue(null);

		bd = new BigDecimal(123);
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal("123.4");
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal("1.23");
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal("12.34");
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal("123.45");
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal(1234);
		testBDFalse(bd);
		testBDFalse(bd.negate());

		bd = new BigDecimal("1234.56");
		testBDFalse(bd);
		testBDFalse(bd.negate());

		bd = new BigDecimal("1.234");
		testBDFalse(bd);
		testBDFalse(bd.negate());

		bd = new BigDecimal("123.234");
		testBDFalse(bd);
		testBDFalse(bd.negate());
	}

	/**
	 * Test de constraintBigDecimalLength.
	 */
	@Test
	public void testConstraintBigDecimalLength() {
		BigDecimal bd;

		testBDLengthTrue(null);

		bd = new BigDecimal(123);
		testBDLengthTrue(bd);
		testBDLengthTrue(bd.negate());

		bd = new BigDecimal(1234);
		testBDLengthFalse(bd);
		testBDLengthFalse(bd.negate());

		bd = new BigDecimal(1000);
		testBDLengthFalse(bd);
		testBDLengthFalse(bd.negate());

		bd = new BigDecimal("999.9999");
		testBDLengthTrue(bd);
		testBDLengthTrue(bd.negate());
	}

	/**
	 * Test de ConstraintDoubleLength.
	 */
	@Test
	public void testConstraintDoubleLength() {
		testDoubleTrue(null);

		testDoubleFalse(Double.NaN);

		testDoubleTrue(123d);
		testDoubleTrue(-123d);

		testDoubleFalse(1234d);
		testDoubleFalse(-1234d);

		testDoubleFalse(1000d);
		testDoubleFalse(-1000d);

		final Double d = 999.9999d;
		testDoubleTrue(d);
		testDoubleTrue(-d);
	}

	/**
	 * Test de ConstraintIntegerLength.
	 */
	@Test
	public void testConstraintIntegerLength() {
		testIntegerTrue(null);

		testIntegerTrue(123);
		testIntegerTrue(-123);

		testIntegerFalse(1234);
		testIntegerFalse(-1234);

		testIntegerFalse(1000);
		testIntegerFalse(-1000);
	}

	/**
	 * Test de ConstraintLongLength.
	 */
	@Test
	public void testConstraintLongLength() {
		testLongTrue(null);

		testLongTrue(123L);
		testLongTrue(-123L);

		testLongFalse(1234L);
		testLongFalse(-1234L);

		testLongFalse(1000L);
		testLongFalse(-1000L);
	}

	/**
	 * Test de ConstraintStringLength.
	 */
	@Test
	public void testConstraintStringLength() {
		testStringTrue(null);

		testStringTrue("abc");
		testStringTrue("123");
		testStringTrue("   ");

		testStringFalse("abcd");
		testStringFalse("abc ");
		testStringFalse(" abc");
		testStringFalse("    ");
	}

	/**
	 * Test de ConstraintRegex.
	 */
	@Test
	public void testConstraintRegex() {
		Assertions.assertTrue(constraintRegex.checkConstraint(null));
		Assertions.assertTrue(constraintRegex.checkConstraint("ABCDEFHIJKLMNOPQRSTUVWXYZ"));
		Assertions.assertTrue(constraintRegex.checkConstraint("abcdefghijklmnopqrstuvwxyz"));
		Assertions.assertTrue(constraintRegex.checkConstraint("0123456789"));
		Assertions.assertTrue(constraintRegex.checkConstraint("_-"));
		Assertions.assertTrue(constraintRegex.checkConstraint("abc0123ABC_-"));

		Assertions.assertFalse(constraintRegex.checkConstraint("&abc"));
		Assertions.assertFalse(constraintRegex.checkConstraint("éabc"));
		Assertions.assertFalse(constraintRegex.checkConstraint("%abc"));
		Assertions.assertFalse(constraintRegex.checkConstraint("'abc"));
	}

}
