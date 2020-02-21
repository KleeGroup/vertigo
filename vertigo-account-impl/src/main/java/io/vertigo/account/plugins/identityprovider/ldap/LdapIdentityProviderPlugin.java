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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.account.impl.account.AccountMapperHelper;
import io.vertigo.account.impl.identityprovider.IdentityProviderPlugin;
import io.vertigo.app.Home;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.StreamFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class LdapIdentityProviderPlugin implements IdentityProviderPlugin, Activeable {
	private static final int MAX_ROWS = 500;
	private static final String LDAP_PHOTO_MIME_TYPE = "image/jpeg";
	private static final String PHOTO_RESERVED_FIELD = "photo";

	private static final Logger LOGGER = LogManager.getLogger(LdapIdentityProviderPlugin.class);

	private static final String DEFAULT_CONTEXT_FACTORY_CLASS_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String SIMPLE_AUTHENTICATION_MECHANISM_NAME = "simple";
	private static final String DEFAULT_REFERRAL = "follow";

	private final CodecManager codecManager;
	private final String ldapServer;
	private final String ldapAccountBaseDn;
	private final String ldapReaderLogin;
	private final String ldapReaderPassword;

	private final String ldapUserAuthAttribute;

	private final String userIdentityEntity;
	private final String ldapUserAttributeMappingStr;
	private AccountMapperHelper<String, DtField> mapperHelper;

	/**
	 * Constructor.
	 * @param ldapServerHost Ldap Server host
	 * @param ldapServerPort Ldap server port (default : 389)
	 * @param ldapAccountBaseDn Base de recherche des DNs d'Accounts
	 * @param ldapReaderLogin Login du reader LDAP
	 * @param ldapReaderPassword Password du reader LDAP
	 * @param ldapUserAuthAttribute Ldap attribute use to find user by it's authToken
	 * @param userIdentityEntity DtDefinition used for User
	 * @param ldapUserAttributeMappingStr Mapping from LDAP to Account
	 * @param codecManager Codec Manager
	 */
	@Inject
	public LdapIdentityProviderPlugin(
			@ParamValue("ldapServerHost") final String ldapServerHost,
			@ParamValue("ldapServerPort") final String ldapServerPort,
			@ParamValue("ldapAccountBaseDn") final String ldapAccountBaseDn,
			@ParamValue("ldapReaderLogin") final String ldapReaderLogin,
			@ParamValue("ldapReaderPassword") final String ldapReaderPassword,
			@ParamValue("ldapUserAuthAttribute") final String ldapUserAuthAttribute,
			@ParamValue("userIdentityEntity") final String userIdentityEntity,
			@ParamValue("ldapUserAttributeMapping") final String ldapUserAttributeMappingStr,
			final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(ldapServerHost);
		Assertion.checkArgNotEmpty(ldapServerPort);
		Assertion.checkArgNotEmpty(ldapAccountBaseDn);
		Assertion.checkArgNotEmpty(ldapReaderLogin);
		Assertion.checkNotNull(ldapReaderPassword);
		Assertion.checkArgNotEmpty(ldapUserAuthAttribute);
		Assertion.checkArgNotEmpty(userIdentityEntity);
		Assertion.checkArgNotEmpty(ldapUserAttributeMappingStr);
		Assertion.checkNotNull(codecManager);
		ldapServer = ldapServerHost + ":" + ldapServerPort;
		this.ldapAccountBaseDn = ldapAccountBaseDn;
		this.ldapReaderLogin = ldapReaderLogin;
		this.ldapReaderPassword = ldapReaderPassword;
		this.ldapUserAuthAttribute = ldapUserAuthAttribute;
		this.userIdentityEntity = userIdentityEntity;
		this.ldapUserAttributeMappingStr = ldapUserAttributeMappingStr;
		this.codecManager = codecManager;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final DtDefinition userDtDefinition = Home.getApp().getDefinitionSpace().resolve(userIdentityEntity, DtDefinition.class);
		mapperHelper = new AccountMapperHelper(userDtDefinition, ldapUserAttributeMappingStr)
				.withReservedDestField(PHOTO_RESERVED_FIELD)
				.parseAttributeMapping();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E getUserByAuthToken(final String userAuthToken) {
		final LdapContext ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
		try {
			return (E) getUserByAuthToken(userAuthToken, ldapContext);
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getUsersCount() {
		throw new UnsupportedOperationException("Can't count all account from LDAP : anti-spooffing protections");
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> List<E> getAllUsers() {
		final LdapContext ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
		try {
			return searchUser("(" + ldapUserAuthAttribute + "=*)", MAX_ROWS, ldapContext);
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> Optional<VFile> getPhoto(final UID<E> accountURI) {
		final LdapContext ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
		try {
			final String displayName = "photo-" + accountURI.getId() + ".jpg";
			final String photoAttributeName = mapperHelper.getReservedSourceAttribute(PHOTO_RESERVED_FIELD);
			return parseOptionalVFile(displayName, getLdapAttributes(accountURI.getId(), Collections.singleton(photoAttributeName), ldapContext));
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	private Entity getUserByAuthToken(final String authToken, final LdapContext ctx) {
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, "(&(" + ldapUserAuthAttribute + "=" + protectLdap(authToken) + "))", 2, mapperHelper.sourceAttributes(), ctx);
		Assertion.checkState(!result.isEmpty(), "Can't found any user with authToken : {0}", ldapUserAuthAttribute);
		Assertion.checkState(result.size() == 1, "Too many user with same authToken ({0} shoud be unique)", ldapUserAuthAttribute);
		return parseUser(result.get(0));
	}

	private <E extends Entity> List<E> searchUser(final String searchRequest, final int top, final LdapContext ldapContext) {
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, searchRequest, top, mapperHelper.sourceAttributes(), ldapContext);
		return (List<E>) result.stream()
				.map(this::parseUser)
				.collect(Collectors.toList());
	}

	private Attributes getLdapAttributes(final Serializable accountId, final Set<String> returningAttributes, final LdapContext ldapContext) {
		final String ldapIdAttr = mapperHelper.getSourceIdField();
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, "(" + ldapIdAttr + "=" + accountId + ")", 2, returningAttributes, ldapContext);
		Assertion.checkState(!result.isEmpty(), "Can't found any user with id : {0}", accountId);
		Assertion.checkState(result.size() == 1, "Too many user with same id ({0} shoud be unique)", accountId);
		return result.get(0);
	}

	private Optional<VFile> parseOptionalVFile(final String displayName, final Attributes attrs) {
		final Object rawData = parseRawAttribute(mapperHelper.getReservedSourceAttribute(PHOTO_RESERVED_FIELD), attrs);
		if (rawData == null) {
			return Optional.empty();
		}
		if (rawData instanceof String) {
			//si string alors base64
			return Optional.of(base64toVFile(displayName, (String) rawData));
		} else if (rawData instanceof byte[]) {
			return Optional.of(byteArrayToVFile(displayName, (byte[]) rawData));
		} else {
			throw new IllegalArgumentException("Can't get photo " + mapperHelper.getReservedSourceAttribute(PHOTO_RESERVED_FIELD) + " from LDAP, format not supported " + rawData.getClass());
		}
	}

	private static String parseNullableAttribute(final String attributeName, final Attributes attrs) {
		if (attributeName != null) {
			final Attribute attribute = attrs.get(attributeName);
			if (attribute != null) {
				try {
					final Object value = attribute.get();
					Assertion.checkNotNull(value);
					return String.valueOf(value);
				} catch (final NamingException e) {
					throw WrappedException.wrap(e, "Ldap attribute {0} found, but is empty", attributeName);
				}
			}
		}
		return null;
	}

	private static Object parseRawAttribute(final String attributeName, final Attributes attrs) {
		if (attributeName != null) {
			final Attribute attribute = attrs.get(attributeName);
			if (attribute != null) {
				try {
					final Object value = attribute.get();
					Assertion.checkNotNull(value);
					return value;
				} catch (final NamingException e) {
					throw WrappedException.wrap(e, "Ldap attribute {0} found, but is empty", attributeName);
				}
			}
		}
		return null;
	}

	private VFile base64toVFile(final String displayName, final String base64Content) {
		final byte[] photo = codecManager.getBase64Codec().decode(base64Content);
		return byteArrayToVFile(displayName, photo);
	}

	private static VFile byteArrayToVFile(final String displayName, final byte[] photo) {
		return new StreamFile(displayName, LDAP_PHOTO_MIME_TYPE, Instant.now(), photo.length, () -> new ByteArrayInputStream(photo));
	}

	private LdapContext createLdapContext(final String principal, final String credentials) {
		final Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CONTEXT_FACTORY_CLASS_NAME);
		env.put(Context.REFERRAL, DEFAULT_REFERRAL);

		env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION_MECHANISM_NAME);
		final String url = "ldap://" + ldapServer;
		env.put(Context.PROVIDER_URL, url);
		if (credentials != null) {
			env.put(Context.SECURITY_PRINCIPAL, principal);
			env.put(Context.SECURITY_CREDENTIALS, credentials);
		} else {
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		}
		try {
			return new InitialLdapContext(env, null);
		} catch (final CommunicationException e) {
			throw WrappedException.wrap(e, "Can't connect to LDAP : {0} ", ldapServer);
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't connect user : {0} ", principal);
		}
	}

	private static String protectLdap(final String principal) {
		return EsapiLdapEncoder.encodeForDN(principal);
	}

	private static void closeLdapContext(final LdapContext ldapContext) {
		try {
			ldapContext.close();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("LDAP connection successfully \"{}\"", ldapContext);
			}
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Error when closing LdapContext");
		}
	}

	private Entity parseUser(final Attributes attrs) {
		try {
			final Entity user = Entity.class.cast(DtObjectUtil.createDtObject(mapperHelper.getDestDefinition()));
			for (final DtField dtField : mapperHelper.destAttributes()) {
				final String value = parseNullableAttribute(mapperHelper.getSourceAttribute(dtField), attrs);
				if (value != null) {
					setTypedValue(dtField, user, value);
				}
			}
			return user;
		} catch (final FormatterException e) {
			throw WrappedException.wrap(e, "Can't parse Account from LDAP");
		}
	}

	private static void setTypedValue(final DtField dtField, final Entity user, final String valueStr) throws FormatterException {
		final Serializable typedValue = (Serializable) dtField.getDomain().stringToValue(valueStr);
		dtField.getDataAccessor().setValue(user, typedValue);
	}

	private static List<Attributes> searchLdapAttributes(final String ldapBaseDn, final String searchRequest, final int top, final Collection<String> returningAttributes, final LdapContext ctx) {
		final List<Attributes> userAttributes = new ArrayList<>();
		final SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		constraints.setReturningAttributes(buildReturingAttributes(returningAttributes));
		constraints.setCountLimit(top);
		try {
			//LDAP ctx.search can be vulnerable to LDAP injection : we already protect all user entries by EsapiLdapEncoder
			final NamingEnumeration<SearchResult> answer = ctx.search(ldapBaseDn, searchRequest, constraints);
			while (answer.hasMore()) {
				final Attributes attrs = answer.next().getAttributes();
				userAttributes.add(attrs);
			}
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't search LDAP user with request: {0}", searchRequest);
		}
		return userAttributes;
	}

	private static String[] buildReturingAttributes(final Collection<String> returningAttributes) {
		Assertion.checkNotNull(returningAttributes);
		//-----
		return returningAttributes.toArray(new String[returningAttributes.size()]);
	}

}
