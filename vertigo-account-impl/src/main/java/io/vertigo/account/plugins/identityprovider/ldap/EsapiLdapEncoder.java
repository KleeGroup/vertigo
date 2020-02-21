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
package io.vertigo.account.plugins.identityprovider.ldap;

/**
 * Extract from org.owasp.esapi.reference.DefaultEncoder.
 * Reference implementation of the Encoder interface. This implementation takes
 * a whitelist approach to encoding, meaning that everything not specifically identified in a
 * list of "immune" characters is encoded.
 *
 * @author Jeff Williams (jeff.williams .at. aspectsecurity.com) <a
 *         href="http://www.aspectsecurity.com">Aspect Security</a>
 * @since June 1, 2007
 * @see org.owasp.esapi.Encoder
 */
final class EsapiLdapEncoder {

	private EsapiLdapEncoder() {
		//private constructor
	}

	/**
	 * Encode data for use in LDAP queries.
	 * @param input the text to encode for LDAP
	 * @return input encoded for use in LDAP
	 */
	public static String encodeForLDAP(final String input) {
		if (input == null) {
			return null;
		}
		// TODO: replace with LDAP codec
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			switch (c) {
				case '\\':
					sb.append("\\5c");
					break;
				case '*':
					sb.append("\\2a");
					break;
				case '(':
					sb.append("\\28");
					break;
				case ')':
					sb.append("\\29");
					break;
				case '\0':
					sb.append("\\00");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Encode data for use in an LDAP distinguished name.
	 * @param input the text to encode for an LDAP distinguished name
	 * @return input encoded for use in an LDAP distinguished name
	 */
	public static String encodeForDN(final String input) {
		if (input == null) {
			return null;
		}
		// TODO: replace with DN codec
		final StringBuilder sb = new StringBuilder();
		if ((input.length() > 0) && ((input.charAt(0) == ' ') || (input.charAt(0) == '#'))) {
			sb.append('\\'); // add the leading backslash if needed
		}
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			switch (c) {
				case '\\':
					sb.append("\\\\");
					break;
				case ',':
					sb.append("\\,");
					break;
				case '+':
					sb.append("\\+");
					break;
				case '"':
					sb.append("\\\"");
					break;
				case '<':
					sb.append("\\<");
					break;
				case '>':
					sb.append("\\>");
					break;
				case ';':
					sb.append("\\;");
					break;
				default:
					sb.append(c);
			}
		}
		// add the trailing backslash if needed
		if ((input.length() > 1) && (input.charAt(input.length() - 1) == ' ')) {
			sb.insert(sb.length() - 1, '\\');
		}
		return sb.toString();
	}

}
