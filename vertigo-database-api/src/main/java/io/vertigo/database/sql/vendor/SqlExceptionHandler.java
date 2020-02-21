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
package io.vertigo.database.sql.vendor;

import java.sql.SQLException;

/**
 * This class handles sql exceptions to create userException
 * or to wrap the original checked exception into an unchecked exception.
 *
 * @author npiedeloup
 */
public interface SqlExceptionHandler {

	/**
	 * Handles and Transforms SQL exception into simple runtime Exception.
	 * Basic execption are managed according their code. ( or range)
	 * @param sqle the original sql exception
	 * @param statementInfos sql statement and params
	 * @return the transformed execption as a runtime execption
	 */
	RuntimeException handleSQLException(SQLException sqle, String statementInfos);
}
