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
package io.vertigo.studio.plugins.mda.domain.sql.model;

import java.util.List;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.lang.Assertion;

/**
 * Méthode Freemarker 'sql'.
 * si config.setSharedVariable("sql", new TemplateMethodSql());
 * Exemple : ${sql(field.domain.dataType)}
 *
 * @author  dchallas
 */
public final class SqlMethodModel implements TemplateMethodModelEx {

	/** {@inheritDoc}*/
	@Override
	public TemplateModel exec(final List params) throws TemplateModelException {
		final Object type = ((StringModel) params.get(0)).getWrappedObject();
		if (type instanceof SqlDtFieldModel) {
			final Domain domain = ((SqlDtFieldModel) type).getSource().getDomain();
			return new SimpleScalar(getSqlType((domain)));
		} else if (type instanceof Domain) {
			return new SimpleScalar(getSqlType(((Domain) type)));
		}
		throw new TemplateModelException("Le paramètre type n'est pas un Domain.");
	}

	private static String getSqlType(final Domain domain) {
		final String storeType = domain.getProperties().getValue(DtProperty.STORE_TYPE);
		Assertion.checkNotNull(storeType, "La propriété StoreType est obligatoire dans le cas de génération de Sql. Domaine incriminé : {0}", domain.getName());
		return storeType;
	}

}
