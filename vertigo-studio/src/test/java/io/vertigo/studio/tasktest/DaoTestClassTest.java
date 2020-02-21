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
package io.vertigo.studio.tasktest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.lang.DataStream;
import io.vertigo.studio.data.domain.Contact;
import io.vertigo.studio.data.tasktest.DaoTestClass;

public class DaoTestClassTest extends DaoTestClass {

	@Test
	public void check_oneParam() {
		Assertions.assertNotNull(dum().dum(Contact.class));
		Assertions.assertNotNull(dum().dum(Integer.class));
		Assertions.assertNotNull(dum().dum(Double.class));
		Assertions.assertNotNull(dum().dum(Boolean.class));
		Assertions.assertNotNull(dum().dum(String.class));
		Assertions.assertNotNull(dum().dum(Date.class));
		Assertions.assertNotNull(dum().dum(LocalDate.class));
		Assertions.assertNotNull(dum().dum(Instant.class));
		Assertions.assertNotNull(dum().dum(BigDecimal.class));
		Assertions.assertNotNull(dum().dum(Long.class));
		Assertions.assertNull(dum().dum(DataStream.class));
	}

	@Test
	public void check_DtParam() {
		final Contact myContact = dum().dumNew(Contact.class);
		Assertions.assertNotNull(myContact);
	}

	@Test
	public void check_DtListParam() {
		Assertions.assertNotNull(dum().dumDtList(Contact.class));
	}
}
