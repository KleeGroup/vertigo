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
package io.vertigo.commons.transaction;

/**
 * This resource must be managed by a transaction.
 * A transaction can have
 *  - at most one active resource (read/ write)
 *  - many readonly resources
 *
 * This resource is for example :
 * - a data connaction to a 'classic' DB Oracle, Sybase, MySQL, postgreSQL....
 * - a mailer
 * - a connection to a fileSystem
 *
 *
 * @author  pchretien
 */
public interface VTransactionResource {

	/**
	 * Commits the resource.
	 * @throws Exception
	 */
	void commit() throws Exception;

	/**
	 * Rollbacks the respource.
	 * @throws Exception
	 */
	void rollback() throws Exception;

	/**
	 * Releases the resource.
	 * This methods is Systematically called after a rollback or a commit.
	 * It's a kind of close.
	 *
	 * @throws Exception
	 */
	void release() throws Exception;
}
