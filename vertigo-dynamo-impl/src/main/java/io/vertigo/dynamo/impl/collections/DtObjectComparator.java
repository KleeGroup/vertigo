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

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Comparateur des DtObject.
 * S'appuie sur SortState.
 * Si la colonne est un type primitif alors on effectue le tri sur ce type.
 * Si la colonne est une UID on délégue à l'UID.
 * @param <D> Type de l'objet
 *
 * @author pchretien
 */
final class DtObjectComparator<D extends DtObject> implements Comparator<D> {

	//On ne veut pas d'un comparateur sérializable !!!
	/**
	 * Comparateur du tri
	 */
	private final Comparator<Object> comparator;

	/**
	 * Field du tri
	 */
	private final DtField sortField;

	/**
	 * Constructor.
	 * @param storeManager Manager de persistence
	 * @param sortField the sort field
	 * @param sortDesc sort order
	 */
	DtObjectComparator(final StoreManager storeManager, final DtField sortField, final boolean sortDesc) {
		Assertion.checkNotNull(sortField);
		//-----
		this.sortField = sortField;
		//On recherche le comparateur associé au champ de la collection
		//Si il n'y a pas de comparateur alors on applique la comparaison standard.
		//On regarde si on est sur une ForeignKey et sur une MasterDataList
		if (sortField.getType() == FieldType.FOREIGN_KEY && storeManager.getMasterDataConfig().containsMasterData(sortField.getFkDtDefinition())) {
			//Il existe une Liste de référence associée
			//Dans le cas des liste de référence on délégue la comparaison
			final DtListURIForMasterData mdlUri = storeManager.getMasterDataConfig().getDtListURIForMasterData(sortField.getFkDtDefinition());
			this.comparator = createMasterDataComparator(sortDesc, storeManager, mdlUri);
		} else {
			//Cas par défaut
			this.comparator = (v1, v2) -> compareValues(v1, v2, sortDesc);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final D dto1, final D dto2) {
		Assertion.checkNotNull(dto1);
		Assertion.checkNotNull(dto2);
		//Les DTC ne contiennent pas d'éléments null.
		//-----
		final DataAccessor dataAccessor = sortField.getDataAccessor();
		return comparator.compare(dataAccessor.getValue(dto1), dataAccessor.getValue(dto2));
	}

	/**
	 * Compare values.
	 * @param sortDesc sort order
	 * @param fieldValue1 value 1
	 * @param fieldValue2 value 2
	 * @return compare value1 to value2
	 */
	// Visibilité package car utilisé au sein d'une classe interne.
	static int compareValues(final Object fieldValue1, final Object fieldValue2, final boolean sortDesc) {
		final int result;
		if (fieldValue1 == null && fieldValue2 == null) {
			return 0;
		}
		if (fieldValue1 == null) {
			result = 1;
		} else if (fieldValue2 == null) {
			result = -1;
		} else if (fieldValue1 instanceof String) { //Objet1 et Objet2 sont désormais non null.
			// pour ignorer les accents
			final Collator compareOperator = Collator.getInstance(Locale.FRENCH);
			compareOperator.setStrength(Collator.PRIMARY);
			result = compareOperator.compare((String) fieldValue1, (String) fieldValue2);
		} else if (fieldValue1 instanceof Comparable<?>) {
			result = ((Comparable) fieldValue1).compareTo(fieldValue2);
		} else {
			result = fieldValue1.toString().compareTo(fieldValue2.toString());
		}
		return sortDesc ? -result : result;
	}

	/**
	 * Fournit le comparateur à utiliser pour trier une colonne référenéant une MasterDataList.
	 * @return Comparator à utiliser pour trier la colonne.
	 */
	private static Comparator<Object> createMasterDataComparator(final boolean sortDesc, final StoreManager storeManager, final DtListURIForMasterData dtcURIForMasterData) {
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(dtcURIForMasterData);
		//-----
		final DataStore dataStore = storeManager.getDataStore();
		final DtField mdFieldSort = dtcURIForMasterData.getDtDefinition().getSortField().get();
		return new MasterDataComparator(dtcURIForMasterData, sortDesc, dataStore, mdFieldSort);
	}

	private static final class MasterDataComparator implements Comparator<Object> {
		private final DtListURIForMasterData dtcURIForMasterData;
		private final boolean sortDesc;
		private final DataStore dataStore;
		private final DtField mdFieldSort;

		MasterDataComparator(final DtListURIForMasterData dtcURIForMasterData, final boolean sortDesc, final DataStore dataStore, final DtField mdFieldSort) {
			this.dtcURIForMasterData = dtcURIForMasterData;
			this.sortDesc = sortDesc;
			this.dataStore = dataStore;
			this.mdFieldSort = mdFieldSort;
		}

		private Object getSortValue(final Object o) {
			final UID<Entity> uid = UID.of(dtcURIForMasterData.getDtDefinition(), o);
			DtObject dto;
			try {
				dto = dataStore.readOne(uid);
			} catch (final Exception e) {
				//Il ne peut pas y avoir d'exception typée dans un comparateur.
				throw WrappedException.wrap(e);
			}
			return mdFieldSort.getDataAccessor().getValue(dto);
		}

		@Override
		public int compare(final Object o1, final Object o2) {
			if (o1 != null && o2 != null) {
				final Object lib1 = getSortValue(o1);
				final Object lib2 = getSortValue(o2);
				return compareValues(lib1, lib2, sortDesc);
			}
			return compareValues(o1, o2, sortDesc); //si l'un des deux est null on retombe sur la comparaison standard
		}
	}

}
