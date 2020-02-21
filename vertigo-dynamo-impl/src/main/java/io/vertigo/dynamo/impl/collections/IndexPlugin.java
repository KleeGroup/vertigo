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
package io.vertigo.dynamo.impl.collections;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Plugin de construction et d'interrogation de l'index d'une DtList.
 * @author npiedeloup
 */
public interface IndexPlugin extends Plugin {
	/**
	 * Retourne une liste filtrée en fonction de la saisie utilisateur.
	 * @param <D> Type d'objet
	 * @param keywords Liste de Mot-clé recherchés séparés par espace(préfix d'un mot)
	 * @param searchedFields Liste des champs sur lesquel porte la recherche  (non null)
	 * @param listFilters Liste des filtres supplémentaires (facettes, sécurité, ...)
	 * @param listState List state (sort, skip, limit)
	 * @param boostedFieldOpt Champ boosté (optionnel : aucun)
	 * @param dtc Liste d'origine à filtrer
	 * @return Liste résultat
	 */
	<D extends DtObject> DtList<D> getCollection(
			final String keywords,
			final Collection<DtField> searchedFields,
			final List<ListFilter> listFilters,
			final DtListState listState,
			final Optional<DtField> boostedFieldOpt,
			final DtList<D> dtc);
}
