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
package io.vertigo.account.plugins.authentication.ldap;

import java.util.Hashtable;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.account.impl.authentication.AuthenticationPlugin;
import io.vertigo.account.impl.authentication.UsernamePasswordAuthenticationToken;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * LDAP impl of Authentification.
 * @author npiedeloup
 */
public final class LdapAuthenticationPlugin implements AuthenticationPlugin {
	private static final Logger LOGGER = LogManager.getLogger(LdapAuthenticationPlugin.class);

	private static final String DEFAULT_CONTEXT_FACTORY_CLASS_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String SIMPLE_AUTHENTICATION_MECHANISM_NAME = "simple";
	private static final String DEFAULT_REFERRAL = "follow";

	private static final String USERDN_SUBSTITUTION_TOKEN = "{0}";
	private String userLoginPrefix;
	private String userLoginSuffix;
	private final String ldapServer;

	/**
	 * Constructor.
	 * @param userLoginTemplate userLoginTemplate
	 * @param ldapServerHost Ldap Server host
	 * @param ldapServerPort Ldap server port (default : 389)
	 */
	@Inject
	public LdapAuthenticationPlugin(
			@ParamValue("userLoginTemplate") final String userLoginTemplate,
			@ParamValue("ldapServerHost") final String ldapServerHost,
			@ParamValue("ldapServerPort") final String ldapServerPort) {
		parseUserLoginTemplate(userLoginTemplate);
		ldapServer = ldapServerHost + ":" + ldapServerPort;
	}

	/** {@inheritDoc} */
	@Override
	public boolean supports(final AuthenticationToken token) {
		return token instanceof UsernamePasswordAuthenticationToken;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> authenticateAccount(final AuthenticationToken token) {
		Assertion.checkNotNull(token);
		//---
		final UsernamePasswordAuthenticationToken usernamePasswordToken = (UsernamePasswordAuthenticationToken) token;
		LdapContext ldapContext = null;
		try {
			final String userProtectedDn = userLoginPrefix + protectLdap(usernamePasswordToken.getPrincipal()) + userLoginSuffix;
			ldapContext = createLdapContext(userProtectedDn, usernamePasswordToken.getPassword());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Ouverture de connexion LDAP  '{}'", ldapContext);
			}
			return Optional.of(token.getPrincipal());
		} catch (final NamingException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("Can't authenticate user '{}'", token.getPrincipal(), e);
			} else {
				LOGGER.info("Can't authenticate user '{}'", token.getPrincipal());
			}
			return Optional.empty(); //can't connect user
		} finally {
			if (ldapContext != null) {
				closeLdapContext(ldapContext);
			}
		}
	}

	private void parseUserLoginTemplate(final String template) {
		Assertion.checkArgNotEmpty(template, "User DN template cannot be null or empty.");
		//----
		final int index = template.indexOf(USERDN_SUBSTITUTION_TOKEN);
		if (index < 0) {
			final String msg = "User Login template must contain the '" +
					USERDN_SUBSTITUTION_TOKEN + "' replacement token to understand where to " +
					"insert the runtime authentication principal.";
			throw new IllegalArgumentException(msg);
		}
		final String prefix = template.substring(0, index);
		final String suffix = template.substring(prefix.length() + USERDN_SUBSTITUTION_TOKEN.length());

		userLoginPrefix = prefix;
		userLoginSuffix = suffix;
	}

	private LdapContext createLdapContext(final String userProtectedPrincipal, final String credentials) throws NamingException {
		final Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CONTEXT_FACTORY_CLASS_NAME);
		env.put(Context.REFERRAL, DEFAULT_REFERRAL);

		env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION_MECHANISM_NAME);
		final String url = "ldap://" + ldapServer;
		env.put(Context.PROVIDER_URL, url);
		if (credentials != null) {
			env.put(Context.SECURITY_PRINCIPAL, userProtectedPrincipal);
			env.put(Context.SECURITY_CREDENTIALS, credentials);
		} else {
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		}
		try {
			return new InitialLdapContext(env, null);
		} catch (final CommunicationException e) {
			throw WrappedException.wrap(e, "Can't connect to LDAP : {0} ", ldapServer);
		}
	}

	private static String protectLdap(final String principal) {
		return EsapiLdapEncoder.encodeForDN(principal);
	}

	private static void closeLdapContext(final LdapContext ldapContext) {
		try {
			ldapContext.close();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Fermeture connexion Ldap  \" {} \"", ldapContext);
			}
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Erreur lors de la fermeture de l'objet LdapContext");
		}
	}
}
