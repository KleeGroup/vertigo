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
package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Gestion d'une liste de référence.
 * Une liste de référence est effectué au titre d'un type de référentiel (MasterDataDefinition).
 * En effet un même type de référentiel (Article par exemple) comporte plusieurs listes :
 *
 * -Tous les articles
 * -Tous les articles actifs
 * -Tous les articles en promotion (donc actifs...)
 *
 * @author pchretien
 */
public final class DtListURIForMasterData extends DtListURI {
	private static final long serialVersionUID = -7808114745411163474L;

	/** the code that identifies a masterData. */
	private final String code;

	/**
	 * Constructor.
	 * @param dtDefinition Définition de la liste de référentiel
	 * @param code Code de la liste de référence. Tous les codes commencent par MDL_.
	 */
	public DtListURIForMasterData(final DtDefinition dtDefinition, final String code) {
		super(dtDefinition);
		//-----
		this.code = code;
	}

	@Override
	public String buildUrn() {
		if (code == null) {
			return getDtDefinition().getName();
		}
		return getDtDefinition().getName() + D2A_SEPARATOR + code;
	}
}
