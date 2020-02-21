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
package io.vertigo.vega.engines.webservice.json;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.UiList;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Version modifiable des UiList.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public abstract class AbstractUiListModifiable<D extends DtObject> extends AbstractList<UiObject<D>> implements UiList<D>, Serializable {

	private static final long serialVersionUID = -8398542301760300787L;
	private final DefinitionReference<DtDefinition> dtDefinitionRef;
	private final Class<D> objectType;

	private final String inputKey;

	// Index
	private final Map<UiObject<D>, D> dtoByUiObject = new HashMap<>();

	// Buffer
	private final UiListDelta<D> uiListDelta;
	private final List<UiObject<D>> bufferUiObjects;

	// Data

	private final DtListDelta<D> dtListDelta;
	private final DtList<D> dtList;

	/**
	 * Constructor.
	 * @param dtList Inner DtList
	 */
	protected AbstractUiListModifiable(final DtList<D> dtList, final String inputKey) {
		Assertion.checkNotNull(dtList);
		//-----
		this.dtList = dtList;
		this.inputKey = inputKey;
		final DtDefinition dtDefinition = dtList.getDefinition();
		dtDefinitionRef = new DefinitionReference<>(dtDefinition);
		this.objectType = (Class<D>) ClassUtil.classForName(dtDefinition.getClassCanonicalName());
		// ---
		uiListDelta = new UiListDelta<>(objectType, new HashMap<>(), new HashMap<>(), new HashMap<>());
		dtListDelta = new DtListDelta<>(new DtList<>(dtDefinition), new DtList<>(dtDefinition), new DtList<>(dtDefinition));
		bufferUiObjects = new ArrayList<>(dtList.size());
		rebuildBuffer();
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiList#getObjectType()
	 */
	@Override
	public Class<D> getObjectType() {
		return objectType;
	}

	protected abstract UiObject<D> createUiObject(final D dto);

	private void rebuildBuffer() {
		uiListDelta.getCreatesMap().clear();
		uiListDelta.getUpdatesMap().clear();
		uiListDelta.getDeletesMap().clear();
		// ---
		dtoByUiObject.clear();
		bufferUiObjects.clear();
		for (int row = 0; row < dtList.size(); row++) {
			final D dto = dtList.get(row);
			final UiObject<D> uiObjects = createUiObject(dto);
			bufferUiObjects.add(uiObjects);
			dtoByUiObject.put(uiObjects, dto);
		}
	}

	/**
	 * @return DtDefinition de l'objet métier
	 */
	@Override
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	private String findContextKey(final UiObject<D> uiObject) {
		Assertion.checkNotNull(uiObject);
		Assertion.checkState(bufferUiObjects.contains(uiObject), "UiObjet {0} not found in UiList with key {1}", uiObject, inputKey);
		// ---
		return inputKey + ".get(" + indexOf(uiObject) + ")";
	}

	/**
	 * @param dto Element to removed
	 * @return If element was removed
	 */
	public boolean remove(final UiObject<D> dto) {
		final boolean result = bufferUiObjects.remove(dto);
		if (result) {
			if (uiListDelta.getCreatesMap().containsKey(dto.getInputKey())) {
				//Si on supprime (remove) un objet déjà ajouté (add),
				//alors il suffit de l'enlever de la liste des éléments ajoutés.
				uiListDelta.getCreatesMap().remove(dto.getInputKey());
			} else {
				//Sinon on l'ajoute à la liste des éléments supprimés.
				uiListDelta.getDeletesMap().put(dto.getInputKey(), dto);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public UiObject<D> remove(final int index) {
		final UiObject<D> dto = get(index);
		final boolean result = remove(dto);
		Assertion.checkState(result, "Erreur de suppression i={0}", index);
		return dto;
	}

	/**
	 * @param dto Element to add
	 * @return true (as specified by Collection.add)
	 */
	public boolean add(final D dto) {
		return add(createUiObject(dto));
	}

	/**
	 * @param uiObject Element to add
	 * @return true (as specified by Collection.add)
	 */
	@Override
	public boolean add(final UiObject<D> uiObject) {
		final boolean result = bufferUiObjects.add(uiObject);
		uiObject.setInputKey(findContextKey(uiObject));
		if (result) {
			if (uiListDelta.getDeletesMap().containsKey(uiObject.getInputKey())) {
				//Si on ajoute (add) un objet précédemment supprimé (add),
				//alors il suffit de l'enlever de la liste des éléments supprimés.
				uiListDelta.getDeletesMap().remove(uiObject.getInputKey());
			} else {
				uiListDelta.getCreatesMap().put(uiObject.getInputKey(), uiObject);
			}
		}
		return result;
	}

	/**
	 * @return DtListDelta
	 */
	public DtListDelta<D> getDtListDelta() {
		Assertion.checkNotNull(dtListDelta);
		//
		return dtListDelta;
	}

	/** {@inheritDoc} */
	@Override
	public UiObject<D> get(final int row) {
		//id>=0 : par index dans la UiList (pour boucle, uniquement dans la même request)
		Assertion.checkState(row >= 0, "Le getteur utilisé n'est pas le bon: utiliser getByRowId");
		final UiObject<D> uiObject = bufferUiObjects.get(row);
		Assertion.checkNotNull(uiObject);
		return uiObject;
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(final Object o) {
		if (o instanceof DtObject) {
			return indexOfDtObject((DtObject) o);
		} else if (o instanceof UiObject) {
			return indexOfUiObject((UiObject<D>) o);
		}
		return super.indexOf(o);
	}

	/**
	 * @param dtObject DtObject recherché
	 * @return index de l'objet dans la liste
	 */
	private int indexOfDtObject(final DtObject dtObject) {
		Assertion.checkNotNull(dtObject);
		//-----
		for (int i = 0; i < bufferUiObjects.size(); i++) {
			if (dtObject.equals(bufferUiObjects.get(i).getServerSideObject())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param uiObject UiObject recherché
	 * @return index de l'objet dans la liste
	 */
	private int indexOfUiObject(final UiObject<D> uiObject) {
		Assertion.checkNotNull(uiObject);
		//-----
		return bufferUiObjects.indexOf(uiObject);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return bufferUiObjects.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkFormat(final UiMessageStack uiMessageStack) {
		//1. check Error => KUserException
		//on valide les éléments internes
		boolean isValid = true;
		for (final UiObject<D> uiObject : bufferUiObjects) {
			uiObject.setInputKey(findContextKey(uiObject));
			isValid = isValid && uiObject.checkFormat(uiMessageStack);
		}
		return isValid;
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> mergeAndCheckInput(final List<DtObjectValidator<D>> validators, final UiMessageStack uiMessageStack) {
		checkFormat(uiMessageStack);
		dtListDelta.getDeleted().clear();
		dtListDelta.getCreated().clear();
		dtListDelta.getUpdated().clear();

		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> uiObject : bufferUiObjects) {
			if (uiObject.isModified()) {
				final D validatedDto = uiObject.mergeAndCheckInput(validators, uiMessageStack);
				if (!uiListDelta.getCreatesMap().containsKey(uiObject.getInputKey())) {
					dtListDelta.getUpdated().add(validatedDto);
				} else {
					dtListDelta.getCreated().add(validatedDto);
				}
			}
		}

		//2. Opérations
		for (final UiObject<D> uiObject : uiListDelta.getDeletesMap().values()) {
			final D dto = dtoByUiObject.get(uiObject);
			if (dto != null) {//on ne garde que les dto qui ETAIENT dans la dtc
				dtListDelta.getDeleted().add(dto);
				//on ne supprime pas tout de suite de la dtc, car cela invalidera les index de originIndexByUiObject
			}
		}

		//on vérifie avant s'il y a des elements pour le cas des listes non modifiable
		//il faudrait plutot que la DtListInput soit non modifiable aussi
		if (!dtListDelta.getCreated().isEmpty()) {
			dtList.addAll(dtListDelta.getCreated());
		}
		if (!dtListDelta.getDeleted().isEmpty()) {
			dtList.removeAll(dtListDelta.getDeleted());
		}
		//-----
		Assertion.checkState(bufferUiObjects.size() == dtList.size(), "bufferList.size() <> dtList.size() : mauvaise synchronisation dtList / bufferList");

		//3. On reconstruit buffer et indexes
		rebuildBuffer();
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<UiObject<D>> iterator() {
		return new UiListModifiableIterator();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final AbstractUiListModifiable<D> other = AbstractUiListModifiable.class.cast(o);
		return bufferUiObjects.equals(other.bufferUiObjects);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return bufferUiObjects.hashCode();
	}

	/** innerclass, volontairement non static */
	class UiListModifiableIterator implements Iterator<UiObject<D>> {
		private int expectedSize; //count removed elements
		private int currentIndex; //init a 0

		/**
		 * Constructor.
		 */
		UiListModifiableIterator() {
			expectedSize = size();
		}

		/** {@inheritDoc} */
		@Override
		public boolean hasNext() {
			checkForComodification();
			return currentIndex < size();
		}

		/** {@inheritDoc} */
		@Override
		public UiObject<D> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			checkForComodification();
			final UiObject<D> next = get(currentIndex);
			currentIndex++;
			return next;
		}

		/** {@inheritDoc} */
		@Override
		public void remove() {
			AbstractUiListModifiable.this.remove(get(currentIndex - 1));
			expectedSize--;
		}

		private void checkForComodification() {
			if (expectedSize != size()) {
				throw new ConcurrentModificationException();
			}
		}
	}

}
