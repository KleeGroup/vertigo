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

import java.io.Serializable;

import io.vertigo.lang.Assertion;

/**
 * Filtre de liste.
 * Construit ListFilter sous forme de chaine.
 * @author pchretien, npiedeloup
 */
public final class ListFilter implements Serializable {
	private static final long serialVersionUID = -4685077662421935642L;

	private final String filterValue;

	private ListFilter(final String filterValue) {
		Assertion.checkNotNull(filterValue);
		//---
		this.filterValue = filterValue;
	}

	/**
	 * Constructeur d'un filtre à partir d'une syntaxe.
	 * Syntaxe acceptée :
	 * FIELD_NAME:VALUE => FilterByValue.
	 *
	 * FIELD_NAME:[MINVALUE TO MAXVALUE]
	 * - Le min et max doivent être du même type.
	 * - Le caractère * peut être utiliser pour indiquer qu'il n'y a pas de borne max ou min.
	 * - Les accolades sont ouvrantes ou fermantes pour indiquer si la valeur est comprise ou non
	 *
	 * @param filterValue Valeur du filtre
	 * @return the listFilter corresponding to the given pattern
	 */
	public static ListFilter of(final String filterValue) {
		return new ListFilter(filterValue);
	}

	/**
	 * @return Valeur du filtre
	 */
	public String getFilterValue() {
		return filterValue;
	}
}
