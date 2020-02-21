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
package io.vertigo.dynamo.collections.model;

import java.io.Serializable;

import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.lang.Assertion;

/**
 * Valeur de facette relatif à une définition.
 * Les valeurs sont
 *  - soit déclarée.
 *  - soit déduite.
 * Exemple :
 *  - Fourchettes de prix (valeurs déclarées)
 *  - Fourchettes de dates (valeurs déclarées)
 *  - Termes les plus usités (valeurs déduites)
 *  - Clustering sémantique (valeurs déduites)
 * Fait partie du métamodèle lorsque la facette est déclarée par ses ranges.
 * Fait partie du modèle lorsque les valeurs sont déduites.
 *
 * @author pchretien
 */
public final class FacetValue implements Serializable {
	private static final long serialVersionUID = -7077655936787603783L;
	private final String code;
	private final MessageText label;
	private final ListFilter listFilter;

	/**
	 * Contructor.
	 * @param code the code of the facet
	 * @param listFilter the list filter
	 * @param label the label of the facet
	 */
	public FacetValue(final String code, final ListFilter listFilter, final MessageText label) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkNotNull(listFilter);
		Assertion.checkNotNull(label);
		//-----
		this.code = code;
		this.listFilter = listFilter;
		this.label = label;
	}

	/**
	 * @return the code of the facet
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns the label of the facet.
	 * This label must be human readable.
	 *
	 * examples :
	 * - 'small files' can be preferred to an expression.
	 *
	 * @return the label of the facet
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * @return the listFilter
	 */
	public ListFilter getListFilter() {
		return listFilter;
	}
}
