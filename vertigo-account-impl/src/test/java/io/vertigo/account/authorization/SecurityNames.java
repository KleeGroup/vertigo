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

import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.model.Record;

public final class SecurityNames {

	/**
	 * Enumération des Authorizations globales.
	 */
	public enum GlobalAuthorizations implements AuthorizationName {
		AtzAdmUsr, AtzAdmPro, AtzAdmApp
	}

	/**
	 * Enumération des Authorizations globales.
	 */
	public enum RecordAuthorizations implements AuthorizationName {
		AtzRecord$read,
		AtzRecord$read2,
		AtzRecord$read3,
		AtzRecord$readHp,
		AtzRecord$delete,
		AtzRecord$notify,
		AtzRecord$create,
		AtzRecord$write, //
		AtzRecord$test,
		AtzRecord$test2,
		AtzRecord$test3
	}

	/**
	 * Enumération des opérations de Dossier.
	 */
	public enum RecordOperations implements OperationName<Record> {
		read,
		read2,
		read3,
		readHp,
		write,
		create,
		delete,
		notify, //
		test,
		test2,
		test3
	}
}
