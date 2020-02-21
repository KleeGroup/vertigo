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
package io.vertigo.commons.codec.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.util.ListBuilder;

/**
 * Test du codec de sérialisation.
 *
 * @author pchretien
 */
public final class SerializationCodecTest extends AbstractCodecTest<Serializable, byte[]> {

	/** {@inheritDoc} */
	@Override
	public Codec<Serializable, byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getSerializationCodec();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() {
		assertNull(codec.encode(null));
		assertNull(codec.decode(null));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		for (final Serializable value : createObjects()) {
			final byte[] serializedValue = codec.encode(value);
			assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() {
		for (final Serializable value : createObjects()) {
			final byte[] serializedValue = codec.encode(value);
			assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testFailDecode() {
		// object ne correspondant pas à une classe;
		Assertions.assertThrows(RuntimeException.class, () -> {
			final byte[] s = "qdfsdf".getBytes();
			codec.decode(s);
		});
	}

	// ===========================================================================
	// =========================== Données de tests
	// ==============================
	// ===========================================================================
	private static List<Serializable> createObjects() {
		return new ListBuilder<Serializable>()
				.add(54) // Test d'un entier
				.add("") // Test d'une chaine vide
				.add("   ") // Test d'une chaine remplie avec des espaces
				.add("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ  1234567890") // Test
				// d'une
				// chaine
				// de
				// caractères
				// simples
				.add("éèêë üû ïî àäâ öô") // Test des accents
				.add(">< & # @ %") // Test des caractères HTML XML
				.add(" % ' ") // Test des caractères SQL
				.add("abcdef aéàè ' () {}  \" ' 12345 \\ / /254sd %§!*-+/")
				// Test d'une chaine de caractères avec caractères  spéciaux mélangés
				.add(createPerson()) // Test d'un objet
				.build();
	}

	private static TestPerson createPerson() {
		final TestPerson mother = new TestPerson("jeanne", "lagrange", 58, null, null);
		final TestPerson father = new TestPerson("charles", "dupond", 86, null, null);
		return new TestPerson("edmond", "dupond", 72, mother, father);
	}

	private static class TestPerson implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String firstName;
		private final String lastName;
		private final Integer weight;

		private final TestPerson mother;
		private final TestPerson father;

		TestPerson(final String firstName, final String lastName, final Integer weight, final TestPerson mother, final TestPerson father) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.weight = weight;
			this.mother = mother;
			this.father = father;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object o) {
			if (o instanceof TestPerson) {
				final TestPerson person = (TestPerson) o;
				return lastName.equals(person.lastName)
						&& firstName.equals(person.firstName)
						&& weight.equals(person.weight)
						&& mother == null
						&& person.mother == null
						||
						mother.equals(person.mother)
								&& father == null
								&& person.father == null
						|| father.equals(person.father);
			}
			return false;
		}
	}

}
