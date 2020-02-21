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

import io.vertigo.core.component.Manager;

/**
 * Gestionnaire de transactions.
 * Une transaction contient un ensemble de ressources de type
 *  - BDD,
 *  - fichiers,
 *  - JMS...
 *
 * Le Manager permet :
 * - Soit d'obtenir la transaction courante (la créant au besoin),
 * - Soit de créer une transaction autonome (au sein de la transaction courante).
 *
 * @author  pchretien
 */
public interface VTransactionManager extends Manager {
	/**
	 * Crée la transaction courante.
	 * Il est nécessaire qu'aucune transaction courante vivante n'existe.
	 * @return Transaction courante.
	 */
	VTransactionWritable createCurrentTransaction();

	/**
	 * Crée une transaction autonome sous la transaction courante déjà démarrée.
	 * Il est impératif qu'une transaction courante vivante existe.
	 * Cette transaction deviendra la transaction courante et devra être commitée ou rollbackée
	 * avant d'agir sur la transaction parente.
	 * @return Nouvelle transaction courante
	 */
	VTransactionWritable createAutonomousTransaction();

	/**
	 * Récupère la transaction courante.
	 * Il est nécessaire que cette transaction existe.
	 * @return Transaction courante.
	 */
	VTransaction getCurrentTransaction();

	/**
	 * Indique si une transaction courante existe.
	 * @return Si il existe une transcation courante.
	 */
	boolean hasCurrentTransaction();
}
