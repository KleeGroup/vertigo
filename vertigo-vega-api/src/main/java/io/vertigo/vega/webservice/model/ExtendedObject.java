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
package io.vertigo.vega.webservice.model;

import java.io.Serializable;
import java.util.HashMap;

import io.vertigo.lang.Assertion;

/**
 * ExtendedObject to extends an object with meta data.
 * @param <O> Inner object type
 */
public final class ExtendedObject<O> extends HashMap<String, Serializable> {
	private static final long serialVersionUID = -8118714236186836600L;

	private final O innerObject;

	/**
	 * Constructor.
	 * @param innerObject inner object
	 */
	public ExtendedObject(final O innerObject) {
		Assertion.checkNotNull(innerObject);
		//-----
		this.innerObject = innerObject;
	}

	/**
	 * @return Inner object
	 */
	public O getInnerObject() {
		return innerObject;
	}
}
