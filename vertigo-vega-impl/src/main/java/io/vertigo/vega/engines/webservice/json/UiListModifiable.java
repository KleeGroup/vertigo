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

import java.util.Collections;
import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Delta operations on List.
 * @author npiedeloup (16 sept. 2014 18:13:55)
 * @param <D> Object type
 */
public final class UiListModifiable<D extends DtObject> extends AbstractUiListModifiable<D> {
	private static final long serialVersionUID = -8008715790791553036L;

	protected UiListModifiable(final Class<D> objectType) {
		super(new DtList<>(objectType), null);
	}

	@Override
	public boolean checkFormat(final UiMessageStack uiMessageStack) {
		boolean isValid = true;
		for (final UiObject uiObject : this) {
			isValid = isValid && uiObject.checkFormat(uiMessageStack);
		}
		return isValid;

	}

	/**
	 * Merged and validate input data and set error into message stack.
	 * @param dtObjectValidators Used validators, may depends on object type.
	 * @param uiMessageStack Message stack to update
	 * @return Updated and validated business object
	 */
	@Override
	public DtList<D> mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(dtObjectValidators);
		//-----
		final DtList<D> dtList = new DtList<>(getObjectType());
		for (final UiObject<D> element : this) {
			//entry.getValue().setInputKey(inputKey + "." + listName + "." + entry.getKey());
			final D dto = element.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
		}
		return dtList;
	}

	@Override
	public boolean equals(final Object o) {
		/* A list equals only the same list */
		return o == this;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	protected UiObject<D> createUiObject(final D dto) {
		return new VegaUiObject<>(dto, Collections.emptySet());
	}

}
