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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.util.BeanUtil;

/**
 * Permet d'accéder aux données d'un objet par son champ.
 * L'objet doit posséder les méthodes (setter et getter) en concordance avec le nom du champ.
 *
 * @author  pchretien
 */
public final class DataAccessor {
	private final DtField dtField;

	DataAccessor(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//-----
		this.dtField = dtField;
	}

	/**
	 * Setter Générique.
	 * Garantit que la valeur passée est conforme
	 *  - au type enregistré pour le champ
	 *  - les contraintes ne sont pas vérifiées.
	 *
	 * @param dto the object in which data will be set
	 * @param value Object
	 */
	public void setValue(final DtObject dto, final Object value) {
		//On vérifie le type java de l'objet.
		dtField.getDomain().checkValue(value);
		//-----
		BeanUtil.setValue(dto, dtField.getName(), value);
	}

	/**
	 * Getter générique.
	 * Garantit que la valeur retournée est conforme
	 *  - au type enregistré pour le champ
	 *
	 *  Attention : en mode BEAN cette méthode lance une erreur
	 * si il existe une seule erreur sur le champ concerné !!
	 *
	 * @param dto the object in which data will be retrieved
	 * @return valeur non typée
	 */
	public Object getValue(final DtObject dto) {
		//Dans le cas d'un champ statique
		return BeanUtil.getValue(dto, dtField.getName());
	}
}
