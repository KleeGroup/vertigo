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
package io.vertigo.dynamo.impl.store.datastore.cache;

import java.io.Serializable;

import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * Implementation d'une liste filtré par un Criteria.
 * @author dchallas
 * @param <E> the type of entity
 */
final class DtListURIForCriteria<E extends Entity> extends DtListURI {
	private static final long serialVersionUID = 7926630153187124165L;

	private static final String CRITERIA_PREFIX = "CRITERIA";

	private final String sortFieldName;
	private final Boolean sortDesc;
	private final int skipRows;
	private final Integer maxRows;

	private final Criteria<E> criteria;

	/**
	 * Constructor.
	 *  @param dtDefinition Id de la Définition de DT
	 * @param criteria critere
	 * @param dtListState Etat de la liste : Sort, Top, Offset
	 */
	DtListURIForCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final DtListState dtListState) {
		super(dtDefinition);
		Assertion.checkNotNull(criteria);
		Assertion.checkNotNull(dtListState);
		this.criteria = criteria;
		this.sortFieldName = dtListState.getSortFieldName().orElse(null);
		this.sortDesc = dtListState.isSortDesc().orElse(null);
		this.skipRows = dtListState.getSkipRows();
		this.maxRows = dtListState.getMaxRows().orElse(null);
	}

	/**
	 * @return Criteres de la liste
	 */
	public Criteria<E> getCriteria() {
		return criteria;
	}

	/**
	 * @return Nombre de lignes max
	 */
	public DtListState getDtListState() {
		return DtListState.of(maxRows, skipRows, sortFieldName, sortDesc);
	}

	/**
	* Construit automatiquement un Criteria à partir d'un DtObject de critère.
	* Les noms des champs dans l'objet de critère doivent correspondre à ceux de l'objet métier.
	* @param dtoCriteria Objet de critère
	* @return Criteria resultant
	*/
	public static <E extends Entity> Criteria<E> createCriteria(final DtObject dtoCriteria) {
		Assertion.checkNotNull(dtoCriteria);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtoCriteria);

		Criteria<E> criteria = Criterions.alwaysTrue();
		for (final DtField field : dtDefinition.getFields()) {
			final String fieldName = field.getName();
			if (field.getType() != DtField.FieldType.COMPUTED) {
				final Object value = field.getDataAccessor().getValue(dtoCriteria);
				if (value instanceof String && field.getType() != DtField.FieldType.FOREIGN_KEY) {
					//si String et pas une FK : on met en préfix
					criteria = criteria.and(Criterions.startsWith(() -> fieldName, (String) value));
				} else if (value != null) {
					criteria = criteria.and(Criterions.isEqualTo(() -> fieldName, (Serializable) value));
				}
			}
			//si null, alors on ne filtre pas
		}
		return criteria;
	}

	@Override
	public String buildUrn() {
		final String sizeUrn = D2A_SEPARATOR + (skipRows != 0 ? String.valueOf(skipRows) + "-" : "") + (maxRows != null ? String.valueOf(maxRows) : "ALL");
		final String sortUrn = sortFieldName != null ? D2A_SEPARATOR + sortFieldName + (sortDesc != null ? sortDesc ? "-Desc" : "-Asc" : "") : "";
		return CRITERIA_PREFIX + sizeUrn + sortUrn + D2A_SEPARATOR + getCriteria().hashCode();
	}

}
