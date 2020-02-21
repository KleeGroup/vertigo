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
package io.vertigo.account.authorization;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.account.authorization.SecurityNames.GlobalAuthorizations;
import io.vertigo.account.authorization.SecurityNames.RecordAuthorizations;
import io.vertigo.account.authorization.SecurityNames.RecordOperations;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.account.authorization.model.Record;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.database.impl.sql.vendor.postgresql.PostgreSqlDataBase;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.criteria.CriteriaCtx;
import io.vertigo.lang.Tuple;

/**
 * @author pchretien
 */
public final class VSecurityManagerTest extends AbstractTestCaseJU5 {

	private static final long DEFAULT_REG_ID = 1L;
	private static final long DEFAULT_DEP_ID = 2L;
	private static final long DEFAULT_COM_ID = 3L;
	private static final long DEFAULT_UTI_ID = 1000L;
	private static final long DEFAULT_TYPE_ID = 10L;
	private static final double DEFAULT_MONTANT_MAX = 100d;

	private long currentDosId = 1;

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private AuthorizationManager authorizationManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return MyNodeConfig.config();
	}

	@Test
	public void testCreateUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		Assertions.assertEquals(Locale.FRANCE, userSession.getLocale());
		Assertions.assertEquals(TestUserSession.class, userSession.getClass());
	}

	@Test
	public void testInitCurrentUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assertions.assertTrue(securityManager.getCurrentUserSession().isPresent());
			Assertions.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assertions.assertFalse(userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assertions.assertFalse(userSession.isPresent());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assertions.assertTrue(securityManager.getCurrentUserSession().isPresent());
			//
			authorizationManager.obtainUserAuthorizations().clearSecurityKeys();
			authorizationManager.obtainUserAuthorizations().clearAuthorizations();
			authorizationManager.obtainUserAuthorizations().clearRoles();
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assertions.assertFalse(securityManager.getCurrentUserSession().isPresent());
	}

	@Test
	public void testRole() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("RAdmin", Role.class);
		Assertions.assertTrue("RAdmin".equals(admin.getName()));
		final Role secretary = definitionSpace.resolve("RSecretary", Role.class);
		Assertions.assertTrue("RSecretary".equals(secretary.getName()));
	}

	@Test
	public void testAccess() {
		//TODO
	}

	@Test
	public void testNotAuthorized() {
		//TODO
	}

	@Test
	public void testToString() {
		final Authorization admUsr = getAuthorization(GlobalAuthorizations.AtzAdmUsr);
		admUsr.toString();
		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);
		admPro.toString();
		/*Pour la couverture de code, et 35min de dette technique.... */
	}

	@Test
	public void testAuthorized() {
		final Authorization admUsr = getAuthorization(GlobalAuthorizations.AtzAdmUsr);
		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);

		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admUsr)
					.addAuthorization(admPro);

			Assertions.assertTrue(authorizationManager.hasAuthorization(GlobalAuthorizations.AtzAdmUsr));
			Assertions.assertTrue(authorizationManager.hasAuthorization(GlobalAuthorizations.AtzAdmPro));
			Assertions.assertFalse(authorizationManager.hasAuthorization(GlobalAuthorizations.AtzAdmApp));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntity() {

		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordRead);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$read);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read));

			Assertions.assertFalse(Arrays.asList("read", "read2", "read3").retainAll(authorizationManager.getAuthorizedOperations(record)));
			Assertions.assertFalse(Arrays.asList("read", "read2", "read3").retainAll(authorizationManager.getAuthorizedOperations(recordTooExpensive)));
			Assertions.assertFalse(Arrays.asList("read", "read2", "read3").retainAll(authorizationManager.getAuthorizedOperations(recordOtherUser)));
			Assertions.assertFalse(Arrays.asList("read", "read2", "read3").retainAll(authorizationManager.getAuthorizedOperations(recordOtherUserAndTooExpensive)));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testPredicateOnEntity() {

		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead).addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test2))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test3))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$write))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$create))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$delete));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$read);
			Assertions.assertTrue(canReadRecord);

			final Predicate<Record> readRecordPredicate = authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read).toPredicate();
			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(readRecordPredicate.test(record));
			Assertions.assertTrue(readRecordPredicate.test(recordTooExpensive));
			Assertions.assertTrue(readRecordPredicate.test(recordOtherUser));
			Assertions.assertFalse(readRecordPredicate.test(recordOtherUserAndTooExpensive));

			Assertions.assertEquals("(AMOUNT <= #amount0# OR UTI_ID_OWNER = #utiIdOwner1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read).toString());
			Assertions.assertEquals("(AMOUNT <= #amount0# OR UTI_ID_OWNER = #utiIdOwner1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read2).toString());
			Assertions.assertEquals("((AMOUNT <= #amount0# AND (UTI_ID_OWNER is null or UTI_ID_OWNER != #utiIdOwner1# )) OR (AMOUNT > #amount0# AND UTI_ID_OWNER = #utiIdOwner1#))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read3).toString());
			Assertions.assertEquals("false", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.readHp).toString());
			Assertions.assertEquals("((UTI_ID_OWNER = #utiIdOwner0# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')) OR (TYP_ID = #typId1# AND AMOUNT > #amount2# AND AMOUNT <= #amount3# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.write).toString());
			Assertions.assertEquals("(TYP_ID = #typId0# AND AMOUNT <= #amount1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.create).toString());
			Assertions.assertEquals("(TYP_ID = #typId0# OR (UTI_ID_OWNER = #utiIdOwner1# AND ETA_CD in ('CRE', 'VAL')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.delete).toString());

			Assertions.assertEquals("(((UTI_ID_OWNER is null or UTI_ID_OWNER != #utiIdOwner0# ) AND (AMOUNT < #amount1# OR AMOUNT = #amount1# OR AMOUNT <= #amount1#)) OR (UTI_ID_OWNER = #utiIdOwner0# AND (AMOUNT > #amount1# OR AMOUNT = #amount1# OR AMOUNT >= #amount1#)))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test).toString());
			Assertions.assertEquals("(ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD = #etaCd0# OR (ETA_CD is null or ETA_CD != #etaCd0# ) OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC') OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC'))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test2).toString());
			Assertions.assertEquals("((((((REG_ID = #regId0# AND DEP_ID = #depId1# AND COM_ID is not null) OR (REG_ID = #regId0# AND DEP_ID = #depId1#)) OR (REG_ID = #regId0# AND DEP_ID = #depId1# AND COM_ID is null )) OR ((REG_ID is null or REG_ID != #regId0# ) AND (DEP_ID is null or DEP_ID != #depId1# ) AND COM_ID is not null )) OR (REG_ID = #regId0# AND DEP_ID is null AND COM_ID is null)) OR (REG_ID = #regId0# AND (DEP_ID is null OR DEP_ID = #depId1#) AND COM_ID is null))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test3).toString());

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$notify);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("false", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.notify).toString());

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testSecuritySqlOnEntity() {

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead).addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test2))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test3))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$write))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$create))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$delete));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$read);
			Assertions.assertTrue(canReadRecord);

			final SqlDialect sqlDialect = new PostgreSqlDataBase().getSqlDialect();
			final Tuple<String, CriteriaCtx> readRecordSql = authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read).toSql(sqlDialect);
			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertEquals("(AMOUNT <= #amount0# OR UTI_ID_OWNER = #utiIdOwner1#)", readRecordSql.getVal1());
			Assertions.assertEquals(100.0, readRecordSql.getVal2().getAttributeValue("amount0"));
			Assertions.assertEquals(1000L, readRecordSql.getVal2().getAttributeValue("utiIdOwner1"));

			Assertions.assertEquals("(AMOUNT <= #amount0# OR UTI_ID_OWNER = #utiIdOwner1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(AMOUNT <= #amount0# OR UTI_ID_OWNER = #utiIdOwner1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read2).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("((AMOUNT <= #amount0# AND (UTI_ID_OWNER is null or UTI_ID_OWNER != #utiIdOwner1# )) OR (AMOUNT > #amount0# AND UTI_ID_OWNER = #utiIdOwner1#))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.read3).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("0=1", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.readHp).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("((UTI_ID_OWNER = #utiIdOwner0# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')) OR (TYP_ID = #typId1# AND AMOUNT > #amount2# AND AMOUNT <= #amount3# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.write).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(TYP_ID = #typId0# AND AMOUNT <= #amount1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.create).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(TYP_ID = #typId0# OR (UTI_ID_OWNER = #utiIdOwner1# AND ETA_CD in ('CRE', 'VAL')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.delete).toSql(sqlDialect).getVal1());

			Assertions.assertEquals("(((UTI_ID_OWNER is null or UTI_ID_OWNER != #utiIdOwner0# ) AND (AMOUNT < #amount1# OR AMOUNT = #amount1# OR AMOUNT <= #amount1#)) OR (UTI_ID_OWNER = #utiIdOwner0# AND (AMOUNT > #amount1# OR AMOUNT = #amount1# OR AMOUNT >= #amount1#)))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD = #etaCd0# OR (ETA_CD is null or ETA_CD != #etaCd0# ) OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC') OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC'))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test2).toSql(sqlDialect).getVal1());
			//TODO
			Assertions.assertEquals("((((((REG_ID = #regId0# AND DEP_ID = #depId1# AND COM_ID is not null) OR (REG_ID = #regId0# AND DEP_ID = #depId1#)) OR (REG_ID = #regId0# AND DEP_ID = #depId1# AND COM_ID is null )) OR ((REG_ID is null or REG_ID != #regId0# ) AND (DEP_ID is null or DEP_ID != #depId1# ) AND COM_ID is not null )) OR (REG_ID = #regId0# AND DEP_ID is null AND COM_ID is null)) OR (REG_ID = #regId0# AND (DEP_ID is null OR DEP_ID = #depId1#) AND COM_ID is null))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.test3).toSql(sqlDialect).getVal1());

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$notify);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("0=1", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.notify).toSql(sqlDialect).getVal1());

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testSecuritySearchOnEntity() {

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test2))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$test3))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$readHp))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$write))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$create))
					.addAuthorization(getAuthorization(RecordAuthorizations.AtzRecord$delete));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$read);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertEquals("(+amount:<=100.0) (+utiIdOwner:1000)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.read));
			Assertions.assertEquals("(amount:<=100.0 utiIdOwner:1000)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.read2));
			Assertions.assertEquals("((+amount:<=100.0 -utiIdOwner:1000) (+amount:>100.0 +utiIdOwner:1000))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.read3));
			Assertions.assertEquals("(*:*)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.readHp));
			Assertions.assertEquals("(+utiIdOwner:1000 +etaCd:('CRE' 'VAL' 'PUB' 'NOT' 'REA')) (+typId:10 +amount:>0 +amount:<=100.0 +etaCd:('CRE' 'VAL' 'PUB' 'NOT' 'REA'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.write));
			Assertions.assertEquals("(+typId:10 +amount:<=100.0)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.create));
			Assertions.assertEquals("(+typId:10) (+utiIdOwner:1000 +etaCd:('CRE' 'VAL'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.delete));

			Assertions.assertEquals("((-utiIdOwner:1000 +(amount:<100.0 amount:100.0 amount:<=100.0)) (+utiIdOwner:1000 +(amount:>100.0 amount:100.0 amount:>=100.0)))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.test));
			Assertions.assertEquals("(etaCd:('CRE' 'VAL' 'PUB') etaCd:('CRE' 'VAL' 'PUB') etaCd:'PUB' -etaCd:'PUB' etaCd:('PUB' 'NOT' 'REA' 'ARC') etaCd:('PUB' 'NOT' 'REA' 'ARC'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.test2));
			//GEO<${geo} OR GEO<=${geo} OR GEO=${geo} OR GEO!=${geo} OR GEO>${geo} OR GEO>=${geo}
			Assertions.assertEquals("((+regId:1 +depId:2 +_exists_:comId) (+regId:1 +depId:2) (+regId:1 +depId:2 -_exists_:comId) (-regId:1 -depId:2 _exists_:comId) (+regId:1 -_exists_:depId -_exists_:comId) (+regId:1 +(depId:2 -_exists_:depId) -_exists_:comId))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.test3));

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$notify);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("", authorizationManager.getSearchSecurity(Record.class, RecordOperations.notify));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityGrant() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Authorization recordCreate = getAuthorization(RecordAuthorizations.AtzRecord$create);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordCreate);

			final boolean canCreateRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$create);
			Assertions.assertTrue(canCreateRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.read));

			//create -> TYP_ID=${typId} and MONTANT<=${montantMax}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.create));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.create));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.create));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.create));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.create));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityOverride() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$readHp);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordRead);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$readHp);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityEnumAxes() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Authorization recordWrite = getAuthorization(RecordAuthorizations.AtzRecord$write);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordWrite);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$write);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.read));

			//write -> (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.write));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityTreeAxes() {
		final Record record = createRecord();
		record.setEtaCd("PUB");

		final Record recordOtherType = createRecord();
		recordOtherType.setEtaCd("PUB");
		recordOtherType.setTypId(11L);

		final Record recordOtherEtat = createRecord();
		recordOtherEtat.setEtaCd("CRE");

		final Record recordOtherUser = createRecord();
		recordOtherUser.setEtaCd("PUB");
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setEtaCd("PUB");
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordOtherCommune = createRecord();
		recordOtherCommune.setEtaCd("PUB");
		recordOtherCommune.setComId(3L);

		final Record recordDepartement = createRecord();
		recordDepartement.setEtaCd("PUB");
		recordDepartement.setComId(null);

		final Record recordOtherDepartement = createRecord();
		recordOtherDepartement.setEtaCd("PUB");
		recordOtherDepartement.setDepId(10L);
		recordOtherDepartement.setComId(null);

		final Record recordRegion = createRecord();
		recordRegion.setEtaCd("PUB");
		recordRegion.setDepId(null);
		recordRegion.setComId(null);

		final Record recordNational = createRecord();
		recordNational.setEtaCd("PUB");
		recordNational.setRegId(null);
		recordNational.setDepId(null);
		recordNational.setComId(null);

		final Authorization recordNotify = getAuthorization(RecordAuthorizations.AtzRecord$notify);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordNotify);

			Assertions.assertTrue(authorizationManager.hasAuthorization(RecordAuthorizations.AtzRecord$notify));

			//grant read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read));
			//grant read2 -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.read2));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.read2));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.read2));

			//notify -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.notify));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherType, RecordOperations.notify));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherEtat, RecordOperations.notify));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.notify));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.notify));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherCommune, RecordOperations.notify));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordDepartement, RecordOperations.notify));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherDepartement, RecordOperations.notify));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordRegion, RecordOperations.notify));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordNational, RecordOperations.notify));

			//override write -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			//default write don't apply : (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherType, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherEtat, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherCommune, RecordOperations.write));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordDepartement, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherDepartement, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordRegion, RecordOperations.write));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordNational, RecordOperations.write));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		//TODO
	}

	private Record createRecord() {
		final Record record = new Record();
		record.setDosId(++currentDosId);
		record.setRegId(DEFAULT_REG_ID);
		record.setDepId(DEFAULT_DEP_ID);
		record.setComId(DEFAULT_COM_ID);
		record.setTypId(DEFAULT_TYPE_ID);
		record.setTitle("Record de test #" + currentDosId);
		record.setAmount(DEFAULT_MONTANT_MAX);
		record.setUtiIdOwner(DEFAULT_UTI_ID);
		record.setEtaCd("CRE");
		return record;
	}

	private Authorization getAuthorization(final AuthorizationName authorizationName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(authorizationName.name(), Authorization.class);
	}

}
