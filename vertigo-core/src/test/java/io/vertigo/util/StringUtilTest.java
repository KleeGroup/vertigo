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
package io.vertigo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test de l'utilitaitre de manipulation des strings.
 * @author pchretien
 */
public final class StringUtilTest {
	private static final String XXX_YYY_ZZZ = "XXX_YYY_ZZZ";

	@Test
	public void testIsEmpty() {
		assertTrue(StringUtil.isEmpty(null));
		assertTrue(StringUtil.isEmpty(""));
		assertTrue(StringUtil.isEmpty("  "));
		assertFalse(StringUtil.isEmpty("a"));
		assertFalse(StringUtil.isEmpty(" a "));
	}

	/**
	 * Test de la fonction de remplacement.
	 */
	@Test
	public void testStringReplace() {
		assertEquals("azertyuiop", StringUtil.replace("azertYYuiop", "YY", "y"));
		assertEquals("yazertyuiopy", StringUtil.replace("YYazertYYuiopYY", "YY", "y"));
		assertEquals("yyyaYay", StringUtil.replace("YYYYYYaYaYY", "YY", "y"));
		assertEquals("YY", StringUtil.replace("YYY", "YY", "Y"));

		//On vérifie si la chaine à remplacer n'existe pas.
		assertEquals("azertyuiop", StringUtil.replace("azertyuiop", "ZZ", "Y"));
	}

	@Test
	public void testStringReplaceWithNull() {
		Assertions.assertThrows(NullPointerException.class,
				() -> StringUtil.replace((String) null, "YY", "Y"));
	}

	@Test
	public void testStringReplaceWithNullOldString() {
		Assertions.assertThrows(NullPointerException.class,
				() -> StringUtil.replace("azertYYuiop", null, "Y"));
	}

	@Test
	public void testStringReplaceWithEmptyOldString() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> StringUtil.replace("azertYYuiop", "", "Y"));
	}

	@Test
	public void testStringReplaceWithNullNewString() {
		Assertions.assertThrows(NullPointerException.class,
				() -> StringUtil.replace("azertYYuiop", "YY", null));
	}

	@Test
	public void testCaseTransform() {
		assertEquals("XxxYyyZzz", StringUtil.constToUpperCamelCase(XXX_YYY_ZZZ));
		assertEquals("xxxYyyZzz", StringUtil.constToLowerCamelCase(XXX_YYY_ZZZ));
		assertEquals("xxxYZzz", StringUtil.constToLowerCamelCase("XXX_Y_ZZZ"));
		assertEquals("xxxYyy12", StringUtil.constToLowerCamelCase("XXX_YYY_12"));

		assertEquals("xxxYyy12Ppp", StringUtil.constToLowerCamelCase("XXX_YYY_12_PPP"));
		assertEquals("xxxYyy1", StringUtil.constToLowerCamelCase("XXX_YYY_1"));
		assertEquals("xxxYyy12_3", StringUtil.constToLowerCamelCase("XXX_YYY_12_3"));
		assertEquals("TAdresseAdr", StringUtil.constToUpperCamelCase("T_ADRESSE_ADR"));
		assertEquals("x2Yyy", StringUtil.constToLowerCamelCase("X_2_YYY"));
		assertEquals("X2Yyy", StringUtil.constToUpperCamelCase("X_2_YYY"));
		assertEquals("TAdresseAdr10", StringUtil.constToUpperCamelCase("T_ADRESSE_ADR_10"));
		assertEquals("TAdresseAdr10", StringUtil.constToUpperCamelCase("T_ADRESSE_ADR_10_"));
	}

	@Test
	public void testCaseTransformWithErrors() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> StringUtil.constToLowerCamelCase("XXX_YYY12_PPP"));
	}

	@Test
	public void testCaseTransformWithErrors2() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> StringUtil.constToLowerCamelCase("XXX_YYY_12PPP"));
	}

	@Test
	public void testCamelToSnakeCase() {
		assertEquals("xxx_yyy_zzz", StringUtil.camelToSnakeCase("XxxYyyZzz"));
		assertEquals("xxx_yyy_zzz", StringUtil.camelToSnakeCase("xxxYyyZzz"));
		assertEquals("xxx_123", StringUtil.camelToSnakeCase("Xxx123"));
		assertEquals("xxx_123_y", StringUtil.camelToSnakeCase("Xxx123Y"));
		assertEquals("xxx_123y", StringUtil.camelToSnakeCase("Xxx123y"));
	}

	@Test
	public void testCaseUnTransform() {
		assertEquals("X_Z_YYY", StringUtil.camelToConstCase("xZYyy"));
		assertEquals("X_Z_YYY", StringUtil.camelToConstCase("XZYyy"));
		assertEquals(XXX_YYY_ZZZ, StringUtil.camelToConstCase("XxxYyyZzz"));
		assertEquals(XXX_YYY_ZZZ, StringUtil.camelToConstCase("xxxYyyZzz"));
		assertEquals("XXX_Y_ZZZ", StringUtil.camelToConstCase("xxxYZzz"));
		assertEquals("XXX_YYY_12", StringUtil.camelToConstCase("xxxYyy12"));
		assertEquals("XXX_YYY_12PPP", StringUtil.camelToConstCase("xxxYyy12ppp"));
		assertEquals("XXX_YYY_12_PPP", StringUtil.camelToConstCase("xxxYyy12Ppp"));
		assertEquals("XXX_YYY_12_3", StringUtil.camelToConstCase("xxxYyy12_3"));
		assertEquals("XXX_Y_Y_Y_1", StringUtil.camelToConstCase("XxxYYY1"));
		assertEquals("T_ADRESSE_ADR", StringUtil.camelToConstCase("TAdresseAdr"));
	}

	@Test
	public void testCaseTransformBijection() {
		final String[] values = { XXX_YYY_ZZZ, "XXX_YYY_12", "XXX_YYY_12_PPP", "XXX_YYY_1", "XXX_YYY_12_3", "RESTE_A_PAYER", "T_ADRESSE_ADR", "XXX_2_Y", "X_2_YYY", "XXX_Z_Y", "X_Z_YYY", "2_YYY", "12_YYY", };

		for (final String value : values) {
			assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToLowerCamelCase(value)));
			assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToUpperCamelCase(value)));
		}
	}

	@Test
	public void testFistToLowerCase() {
		assertEquals("xZYyy", StringUtil.first2LowerCase("xZYyy"));
		assertEquals("xZYyy", StringUtil.first2LowerCase("XZYyy"));
		assertEquals("", StringUtil.first2LowerCase(""));
	}

	@Test
	public void testFistToUpperCase() {
		assertEquals("XZYyy", StringUtil.first2UpperCase("xZYyy"));
		assertEquals("XZYyy", StringUtil.first2UpperCase("XZYyy"));
		assertEquals("", StringUtil.first2UpperCase(""));
	}

	@Test
	public void testSimpleLetter() {
		assertTrue(StringUtil.isSimpleLetterOrDigit('a'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('b'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('c'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('x'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('y'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('z'));
		//----
		assertTrue(StringUtil.isSimpleLetterOrDigit('A'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('B'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('C'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('X'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('Y'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('Z'));
		//----
		assertTrue(StringUtil.isSimpleLetterOrDigit('0'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('1'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('2'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('3'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('4'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('5'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('6'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('7'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('8'));
		assertTrue(StringUtil.isSimpleLetterOrDigit('9'));
	}

	@Test
	public void testNoSimpleLetter() {
		assertFalse(StringUtil.isSimpleLetterOrDigit('é'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('à'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('ù'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('â'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('"'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('+'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('-'));
		assertFalse(StringUtil.isSimpleLetterOrDigit('&'));
	}

	@Test
	public void testformat() {
		assertEquals("bonjour le monde", StringUtil.format("bonjour le monde"));
		assertEquals("bonjour le monde", StringUtil.format("bonjour {0} monde", "le"));
		assertEquals("bonjour le monde", StringUtil.format("bonjour {0} {1}", "le", "monde"));
		assertEquals("bonjour 'le' monde", StringUtil.format("bonjour ''{0}'' monde", "le"));
		assertEquals("bonjour 'le' monde", StringUtil.format("bonjour '{0}' monde", "le"));
		assertEquals("bonjour 'le' monde", StringUtil.format("bonjour ''{0}' monde", "le"));
	}

	@Test
	public void testformatWithNull() {
		Assertions.assertThrows(NullPointerException.class,
				() -> StringUtil.format(null));
	}

	@Test
	public void testformatWithError() {
		Assertions.assertThrows(Exception.class,
				//Si on oublie de fermer une parenthèse
				() -> StringUtil.format("bonjour {0 monde", "le"));
	}
}
