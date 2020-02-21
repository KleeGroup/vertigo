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
 * Transaction.
 * Une transaction :
 * - contient un ensemble de ressources de type BDD, fichiers, JMS...
 * - est soit démarrée, soit terminée.
 * - peut posséder (ou être) une transaction imbriquée.
 *
 * @author pchretien
 */
public interface VTransaction {

	/**
	 * Ajoute une ressource à la transaction en précisant son ordre au sein de la transaction.
	 * Il n'est pas possible d'enregistrer pour une même transaction, deux ressources avec le même identifiant.
	 * @param id Identifiant de la ressource transactionnelle au sein de la transaction
	 * @param resource Ressource transactionnelle
	 * @param <R> Ressource transactionnelle
	 */
	<R extends VTransactionResource> void addResource(VTransactionResourceId<R> id, R resource);

	/**
	 * @param transactionResourceId Identifiant/type de ressource transactionnelle.
	 * @return Ressource transactionnelle correspondant à l'id
	 * @param <R> Ressource transactionnelle
	 */
	<R extends VTransactionResource> R getResource(VTransactionResourceId<R> transactionResourceId);

	/**
	 * Adds function that is executed just before transaction commit.
	 * Functions are executed in registration order
	 * If an exception occures then
	 * - current transaction is rollbacked
	 * - other beforeCommit functions are not executed
	 *
	 * Examples :
	 * - saves a file and keeps the main database as the master
	 *
	 * @param function the function to execute beforeCommit
	 */
	void addBeforeCommit(final Runnable function);

	/**
	 * Adds function that is executed after transaction commit or rollback.
	 * Functions are executed in registration order
	 * These functions are always executed.
	 * If an exception occures then
	 * - current transaction was NOT rollbacked
	 * - all afterCompletion functions ARE executed
	 *
	 * Examples :
	 * - send a mail or a notification when an operation has been successfully done (an item updated,...)
	 * - clear temp data if operation failed
	 *
	 * @param function the function to execute afterCompletion
	 */
	void addAfterCompletion(final VTransactionAfterCompletionFunction function);
}
