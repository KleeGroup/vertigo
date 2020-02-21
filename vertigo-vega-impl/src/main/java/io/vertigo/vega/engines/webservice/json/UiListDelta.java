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
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Delta operations on List.
 * @author npiedeloup (16 sept. 2014 18:13:55)
 * @param <D> Object type
 */
public final class UiListDelta<D extends DtObject> implements Serializable {
	private static final long serialVersionUID = 1592171971937013208L;
	private final Map<String, UiObject<D>> collCreates;
	private final Map<String, UiObject<D>> collUpdates;
	private final Map<String, UiObject<D>> collDeletes;

	private final Class<D> objectType;

	/**
	 * Constructor.
	 * @param objectType Object type
	 * @param collCreates Map of created inputs
	 * @param collUpdates Map of updated inputs
	 * @param collDeletes Map of removed inputs
	 */
	// TODO remettre protected
	public UiListDelta(final Class<D> objectType, final Map<String, UiObject<D>> collCreates, final Map<String, UiObject<D>> collUpdates, final Map<String, UiObject<D>> collDeletes) {
		Assertion.checkNotNull(objectType);
		Assertion.checkNotNull(collCreates);
		Assertion.checkNotNull(collUpdates);
		Assertion.checkNotNull(collDeletes);
		//-----
		this.objectType = objectType;
		this.collCreates = collCreates;
		this.collUpdates = collUpdates;
		this.collDeletes = collDeletes;
	}

	/**
	 * @return Object type
	 */
	public Class<D> getObjectType() {
		return objectType;
	}

	/**
	 * @return Created uiObjects by client id
	 */
	public Map<String, UiObject<D>> getCreatesMap() {
		return collCreates;
	}

	/**
	 * @return Updated uiObjects by client id
	 */
	public Map<String, UiObject<D>> getUpdatesMap() {
		return collUpdates;
	}

	/**
	 * @return Deleted uiObject by client id
	 */
	public Map<String, UiObject<D>> getDeletesMap() {
		return collDeletes;
	}

	/**
	 * Merged and validate input data and set error into message stack.
	 * @param dtObjectValidators Used validators, may depends on object type.
	 * @param uiMessageStack Message stack to update
	 * @return Updated and validated business object
	 */
	public DtListDelta<D> mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(dtObjectValidators);
		//-----
		final DtList<D> dtListCreates = mergeAndCheckInput(collCreates, dtObjectValidators, uiMessageStack);
		final DtList<D> dtListUpdates = mergeAndCheckInput(collUpdates, dtObjectValidators, uiMessageStack);
		final DtList<D> dtListDeletes = mergeAndCheckInput(collDeletes, dtObjectValidators, uiMessageStack);

		return new DtListDelta<>(dtListCreates, dtListUpdates, dtListDeletes);
	}

	private DtList<D> mergeAndCheckInput(final Map<String, UiObject<D>> uiObjectMap, final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		final DtList<D> dtList = new DtList<>(objectType);
		for (final Map.Entry<String, UiObject<D>> entry : uiObjectMap.entrySet()) {
			//entry.getValue().setInputKey(inputKey + "." + listName + "." + entry.getKey());
			final D dto = entry.getValue().mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
		}
		return dtList;
	}
}
