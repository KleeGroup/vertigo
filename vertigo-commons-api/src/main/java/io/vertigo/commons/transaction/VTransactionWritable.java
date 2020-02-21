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
 * Soit on commit, soit on rollback une transaction.
 * Le commit ou le rollback est propagé sur toutes les ressources participant à la transaction.
 * Pour des raisons de simplicité on se refuse à utiliser le commit à 2 phases.
 * Les ressources sont commitées selon leur priorités.
 * La transaction possède un état interne qui est modifié de façon irréversible lors du commit ou du rollback.
 * Une transaction est soit démarrée, soit terminée.
 *
 * Une transaction peut posséder (ou être) une transaction imbriquée.
 *
 * @author  pchretien
 */
public interface VTransactionWritable extends VTransaction, AutoCloseable {
	/**
	 * Valide la transaction.
	 * Cette méthode commit puis libère dans l'ordre toutes les ressources participant à la transaction.
	 * Si aucune ressource n'est présente, cette méthode ne fait rien.
	 */
	void commit();

	/**
	 * Annule la transaction.
	 * Cette méthode annule puis libère dans l'ordre toutes les ressources participant à la transaction.
	 *
	 * Si aucune ressource n'est présente,
	 * ou bien si un commit ou un rollback a déja fermé la transaction
	 * alors cette méthode ne fait rien.
	 */
	void rollback();

	//method is overriden to delete "throw Exception"
	//close is similar to rollback
	/** {@inheritDoc} */
	@Override
	void close();

}
