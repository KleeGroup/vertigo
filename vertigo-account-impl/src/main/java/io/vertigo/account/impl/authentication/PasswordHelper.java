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
package io.vertigo.account.impl.authentication;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import io.vertigo.app.Home;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Utility class used for password managment.
 *
 * @author npiedeloup
 */
public final class PasswordHelper {

	private static final int MAX_POWER_ITERATION = 256;
	private static final int PBKDF2_POWER_ITERATIONS = 12; //4096 iterations
	private static final int PBKDF2_KEY_LENGTH = 256; // bits
	private static final int SALT_LENGTH = 8; //must be  ceil(saltSizeInBytes / 3) * 4 = 6*4/3
	private static final int POWER_ITERATION_LENGTH = 2;

	private final Charset defaultCharsetUTF8;
	private final SecretKeyFactory secretKeyFactory;
	private final CodecManager codecManager;
	private final SecureRandom rnd;

	/**
	 * Constructor.
	 */
	public PasswordHelper() {
		try {
			secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		} catch (final NoSuchAlgorithmException e) {
			throw WrappedException.wrap(e);
		}
		defaultCharsetUTF8 = Charset.forName("UTF-8");
		codecManager = Home.getApp().getComponentSpace().resolve(CodecManager.class);
		rnd = new SecureRandom();
	}

	/**
	 * Encode a user password, with salt.
	 * @param userPassword not encoded user password
	 * @return salted and encoded password
	 */
	public String createPassword(final String userPassword) {
		return encodePassword(generateNewSalt(), PBKDF2_POWER_ITERATIONS, userPassword);
	}

	/**
	 * Check userPassword against the encoded already known password.
	 * @param encodedPassword salted and encoded password
	 * @param userSubmittedPassword not encoded user password
	 * @return if userPassword is correct.
	 */
	public boolean checkPassword(final String encodedPassword, final String userSubmittedPassword) {
		return encodedPassword.equals(encodePassword(extractSalt(encodedPassword), extractNbIteration(encodedPassword), userSubmittedPassword));
	}

	private String generateNewSalt() {
		final byte[] byteSalt = rnd.generateSeed(6);
		return encodeBase64(byteSalt);
	}

	private static String extractSalt(final String password) {
		return password.substring(0, SALT_LENGTH);
	}

	private static int extractNbIteration(final String password) {
		return decodeInt(password.substring(SALT_LENGTH, SALT_LENGTH + POWER_ITERATION_LENGTH));
	}

	private String encodePassword(final String salt, final int powerIteration, final String password) {
		return salt + encodeInt(powerIteration) + encodeBase64(toPBKDF2(password, salt, (int) Math.pow(2, powerIteration)));
	}

	/**
	 * Encode un tableau d'octets en base 64.
	 * @param data La donnee.
	 * @return La valeur encodee.
	 */
	private String encodeBase64(final byte[] data) {
		return codecManager.getBase64Codec().encode(data);
	}

	private static String encodeInt(final int value) {
		Assertion.checkArgument(value < MAX_POWER_ITERATION, "Can't support 2^{0} iterations (max 255)", value);
		//-----
		return String.format("%02x", value);
	}

	private static int decodeInt(final String encoded) {
		return Integer.parseInt(encoded, 16);
	}

	/**
	 * Calcule le hash PBKDF2 d'une chaine de caractere.
	 * @param string Chaine de caractere.
	 * @return Tableau d'octets.
	 */
	//see : https://adambard.com/blog/3-wrong-ways-to-store-a-password/
	private byte[] toPBKDF2(final String password, final String salt, final int nbIterations) {
		final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(defaultCharsetUTF8), nbIterations, PBKDF2_KEY_LENGTH);
		try {
			return secretKeyFactory.generateSecret(keySpec).getEncoded();
		} catch (final InvalidKeySpecException e) {
			throw WrappedException.wrap(e);
		}
	}

}
