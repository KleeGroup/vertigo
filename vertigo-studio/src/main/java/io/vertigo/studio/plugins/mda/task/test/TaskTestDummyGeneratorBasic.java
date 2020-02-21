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
package io.vertigo.studio.plugins.mda.task.test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.DataStream;
import io.vertigo.lang.VSystemException;

/**
 * Basic dummy values generator. (One possible value for each type)
 *
 * @author sezratty, mlaroche
 *
 */
public class TaskTestDummyGeneratorBasic implements TaskTestDummyGenerator {

	/* (non-Javadoc)
	 * @see io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGenerator#dum(java.lang.Class)
	 */
	@Override
	public <T> T dum(final Class<T> type) {
		if (DtObject.class.isAssignableFrom(type)) {
			//we are a dtObject
			return (T) dum(DtObjectUtil.findDtDefinition((Class<DtObject>) type));
		} else if (Integer.class.equals(type) || int.class.equals(type)) {
			return (T) Integer.valueOf(1);
		} else if (Double.class.equals(type) || double.class.equals(type)) {
			return (T) Double.valueOf(1.00);
		} else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			return (T) Boolean.TRUE;
		} else if (String.class.equals(type)) {
			return (T) "String";
		} else if (Date.class.equals(type)) {
			return (T) new Date();
		} else if (LocalDate.class.equals(type)) {
			return (T) LocalDate.now();
		} else if (Instant.class.equals(type)) {
			return (T) Instant.now();
		} else if (java.math.BigDecimal.class.equals(type)) {
			return (T) new BigDecimal(10);
		} else if (Long.class.equals(type) || long.class.equals(type)) {
			return (T) Long.valueOf(1L);
		} else if (DataStream.class.equals(type)) {
			return null; //TODO better
		} else {
			throw new VSystemException("Type  : '{0}' is not supported for generating dummy values", type.getCanonicalName());
		}
	}

	/* (non-Javadoc)
	 * @see io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGenerator#dumList(java.lang.Class)
	 */
	@Override
	public <T> List<T> dumList(final Class<T> clazz) {
		return Collections.singletonList(dum(clazz));

	}

	/* (non-Javadoc)
	 * @see io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGenerator#dumDtList(java.lang.Class)
	 */
	@Override
	public <D extends DtObject> DtList<D> dumDtList(final Class<D> dtoClass) {
		return DtList.of(dum(dtoClass));
	}

	/* (non-Javadoc)
	 * @see io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGenerator#dumNew(java.lang.Class)
	 */
	@Override
	public <D extends DtObject> D dumNew(final Class<D> dtoClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtoClass);
		final D object = dum(dtoClass);
		if (dtDefinition.getIdField().isPresent()) {
			dtDefinition.getIdField().get().getDataAccessor().setValue(object, null);// we make it pristine
		}
		return object;

	}

	private DtObject dum(final DtDefinition def) {
		/* Créé une instance du dto. */
		final DtObject dto = DtObjectUtil.createDtObject(def);
		/* Parcourt les champs */
		def.getFields().stream()
				.filter(dtField -> dtField.getType() == FieldType.COMPUTED)// we don't treat computed field (no setter)
				.forEach(dtField -> {
					final Domain domain = dtField.getDomain();
					final Class javaClass = domain.getJavaClass();
					final Object value;
					if (dtField.getDomain().isMultiple()) {
						if (dtField.getDomain().getScope().isDataObject()) {
							value = dumDtList(javaClass);
						} else {
							value = dumList(javaClass);
						}
					} else {
						value = dum(javaClass);
					}
					dtField.getDataAccessor().setValue(dto, value);
				});
		/* Retourne l'instance factice */
		return dto;
	}

}
