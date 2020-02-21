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

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.account.authorization.SecurityNames.GlobalAuthorizations;
import io.vertigo.account.authorization.SecurityNames.RecordAuthorizations;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.model.FullSecuredServices;
import io.vertigo.account.authorization.model.PartialSecuredServices;
import io.vertigo.account.authorization.model.Record;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.DefinitionSpace;

/**
 * @author pchretien
 */
public final class VSecurityAspectTest extends AbstractTestCaseJU5 {

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

	@Inject
	private FullSecuredServices fullSecuredServices;
	@Inject
	private PartialSecuredServices partialSecuredServices;

	@Override
	protected NodeConfig buildNodeConfig() {
		return MyNodeConfig.config();
	}

	@Test
	public void testAuthorized() {
		final Authorization admUsr = getAuthorization(GlobalAuthorizations.AtzAdmUsr);
		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);
		final Authorization recordWrite = getAuthorization(RecordAuthorizations.AtzRecord$write);
		final Record record = createRecord();

		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admUsr)
					.addAuthorization(admPro)
					.addAuthorization(recordWrite);

			Assertions.assertEquals(1, fullSecuredServices.fakeService1());
			Assertions.assertEquals(2, fullSecuredServices.fakeService2());
			Assertions.assertEquals(3, fullSecuredServices.fakeService3(record));
			Assertions.assertEquals(4, fullSecuredServices.fakeService4(record));

			Assertions.assertEquals(1, partialSecuredServices.fakeService1());
			Assertions.assertEquals(2, partialSecuredServices.fakeService2());
			Assertions.assertEquals(3, partialSecuredServices.fakeService3(record));
			Assertions.assertEquals(4, partialSecuredServices.fakeService4(record));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testUnauthorized() {
		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);
		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final Record record = createRecord();

		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admPro)
					.addAuthorization(recordRead);

			Assertions.assertEquals(1, fullSecuredServices.fakeService1());
			Assertions.assertEquals(2, fullSecuredServices.fakeService2());
			Assertions.assertEquals(3, fullSecuredServices.fakeService3(record));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(record));

			Assertions.assertEquals(1, partialSecuredServices.fakeService1());
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService2());
			Assertions.assertEquals(3, partialSecuredServices.fakeService3(record));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(record));
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

		final Authorization admUsr = getAuthorization(GlobalAuthorizations.AtzAdmUsr);
		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);
		final Authorization recordWrite = getAuthorization(RecordAuthorizations.AtzRecord$write);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admUsr)
					.addAuthorization(admPro)
					.addAuthorization(recordWrite);

			Assertions.assertEquals(1, fullSecuredServices.fakeService1());
			Assertions.assertEquals(2, fullSecuredServices.fakeService2());
			Assertions.assertEquals(3, fullSecuredServices.fakeService3(record));
			Assertions.assertEquals(4, fullSecuredServices.fakeService4(record));

			Assertions.assertEquals(3, fullSecuredServices.fakeService3(recordTooExpensive));
			Assertions.assertEquals(4, fullSecuredServices.fakeService4(recordTooExpensive));

			Assertions.assertEquals(3, fullSecuredServices.fakeService3(recordOtherUser));
			Assertions.assertEquals(4, fullSecuredServices.fakeService4(recordOtherUser));

			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService3(recordOtherUserAndTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(recordOtherUserAndTooExpensive));

			Assertions.assertEquals(1, partialSecuredServices.fakeService1());
			Assertions.assertEquals(2, partialSecuredServices.fakeService2());
			Assertions.assertEquals(3, partialSecuredServices.fakeService3(record));
			Assertions.assertEquals(4, partialSecuredServices.fakeService4(record));

			Assertions.assertEquals(3, partialSecuredServices.fakeService3(recordTooExpensive));
			Assertions.assertEquals(4, partialSecuredServices.fakeService4(recordTooExpensive));

			Assertions.assertEquals(3, partialSecuredServices.fakeService3(recordOtherUser));
			Assertions.assertEquals(4, partialSecuredServices.fakeService4(recordOtherUser));

			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService3(recordOtherUserAndTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(recordOtherUserAndTooExpensive));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testUnauthorizedOnEntity() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization admPro = getAuthorization(GlobalAuthorizations.AtzAdmPro);
		final Authorization recordRead = getAuthorization(RecordAuthorizations.AtzRecord$read);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admPro)
					.addAuthorization(recordRead);

			Assertions.assertEquals(1, fullSecuredServices.fakeService1());
			Assertions.assertEquals(2, fullSecuredServices.fakeService2());
			Assertions.assertEquals(3, fullSecuredServices.fakeService3(record));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(record));

			Assertions.assertEquals(3, fullSecuredServices.fakeService3(recordTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(recordTooExpensive));

			Assertions.assertEquals(3, fullSecuredServices.fakeService3(recordOtherUser));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(recordOtherUser));

			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService3(recordOtherUserAndTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> fullSecuredServices.fakeService4(recordOtherUserAndTooExpensive));

			Assertions.assertEquals(1, partialSecuredServices.fakeService1());
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService2());
			Assertions.assertEquals(3, partialSecuredServices.fakeService3(record));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(record));

			Assertions.assertEquals(3, partialSecuredServices.fakeService3(recordTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(recordTooExpensive));

			Assertions.assertEquals(3, partialSecuredServices.fakeService3(recordOtherUser));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(recordOtherUser));

			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService3(recordOtherUserAndTooExpensive));
			Assertions.assertThrows(VSecurityException.class, () -> partialSecuredServices.fakeService4(recordOtherUserAndTooExpensive));

		} finally {
			securityManager.stopCurrentUserSession();
		}
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
