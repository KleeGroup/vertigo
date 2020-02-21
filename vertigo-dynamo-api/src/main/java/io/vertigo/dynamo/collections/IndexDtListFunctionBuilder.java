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
package io.vertigo.dynamo.collections;

import java.util.Collection;
import java.util.function.UnaryOperator;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Builder;

/**
 * Processor that can be composed of filters or sorters and be applied on a list. *
 * @author npiedeloup
 * @param <D> the type of dtObject in the modified list
 */
public interface IndexDtListFunctionBuilder<D extends DtObject> extends Builder<UnaryOperator<DtList<D>>> {

	/**
	 * Création d'un tri de colonne.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @return Ce builder
	 */
	IndexDtListFunctionBuilder<D> sort(final String fieldName, final boolean desc);

	//=======================FILTER============================================
	/**
	 * Filtre une DTC par recherche plein text, ne modifie pas la collection d'origine.
	 * (préférez le mettre comme premiere opération : pour passer en mode index)
	 * @param keywords Liste de Mot-clé recherchés séparés par espace(préfix d'un mot)
	 * @param maxRows Nombre max de lignes retournées
	 * @param searchedFields Liste des champs sur lesquel porte la recherche  (nullable : tous)
	 * @return Ce builder
	 */
	IndexDtListFunctionBuilder<D> filter(final String keywords, final int maxRows, final Collection<DtField> searchedFields);

	/**
	 * Constructeur d'un filtre champ = valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 * @return Ce builder
	 */
	IndexDtListFunctionBuilder<D> filterByValue(final String fieldName, final String value);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 *
	 * @param listFilter Filtre de liste
	 * @return Ce builder
	 */
	IndexDtListFunctionBuilder<D> filter(final ListFilter listFilter);

	//=======================SUB LIST==========================================
	/**
	 * Sous Liste d'une DTC, ne modifie pas la collection d'origine.
	 * @param start Indexe de début (Inclus)
	 * @param end Indexe de fin (Exclus)
	 * @return Ce builder
	 */
	IndexDtListFunctionBuilder<D> filterSubList(final int start, final int end);

}
