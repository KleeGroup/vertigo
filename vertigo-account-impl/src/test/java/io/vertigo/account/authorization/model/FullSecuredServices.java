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
package io.vertigo.account.authorization.model;

import io.vertigo.account.authorization.annotations.Secured;
import io.vertigo.account.authorization.annotations.SecuredOperation;
import io.vertigo.core.component.Component;

/**
 * @author npiedeloup
 */
@Secured({ "AdmUsr", "AdmPro" })
public class FullSecuredServices implements Component {

	public int fakeService1() {
		return 1;
	}

	public int fakeService2() {
		return 2;
	}

	public int fakeService3(@SecuredOperation("read") final Record record) {
		return 3;
	}

	public int fakeService4(@SecuredOperation("write") final Record record) {
		return 4;
	}

}
