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
package io.vertigo.dynamo.store.datastore;

import java.util.List;

import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.UID;

/**
 * Gestion des relations NN.
 *
 * @author  dchallas
 */
public interface BrokerNN {
	/**
	 * Ajout un objet à la collection existante.
	 * @param dtListURI DtList de référence
	 * @param uidToAppend UID de l'objet à ajout à la NN
	 */
	void appendNN(final DtListURIForNNAssociation dtListURI, final UID uidToAppend);

	/**
	 * Mise à jour des associations n-n. Annule et remplace.
	 * @param dtListURI DtList de référence
	 * @param uidList  uriList
	 */
	void updateNN(final DtListURIForNNAssociation dtListURI, final List<UID> uidList);

	/**
	 * Supprime toutes les relations liés à l'objet.
	 * @param dtListURI DtList de référence
	 */
	void removeAllNN(final DtListURIForNNAssociation dtListURI);

	/**
	 * Supprime la relation liés aux deux objets.
	 * Lance une erreur si pas de relation
	 * @param dtListURI DtList de référence
	 * @param uidToDelete URI de l'objet à supprimer de la NN
	 */
	void removeNN(final DtListURIForNNAssociation dtListURI, final UID uidToDelete);
}
