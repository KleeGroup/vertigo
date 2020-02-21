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

import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 *
 * Dummy values generator
 * @author mlaroche
 *
 */
public interface TaskTestDummyGenerator {

	/**
	 * Creates a dummy value for the specified type
	 * @param type class of the wanted object
	 * @param <T> class of the wanted object
	 * @return dummy value
	 */
	<T> T dum(final Class<T> type);

	/**
	 * Creates a list of dummy values for the specified type
	 * @param clazz class of the wanted object
	 * @param <T> class of the wanted object
	 * @return dummy values as List
	 */
	<T> List<T> dumList(final Class<T> clazz);

	/**
	 * Creates a dtList of dummy values for the specified type
	 * @param dtoClass class of the wanted object
	 * @param <D> class of the wanted object
	 * @return dummy values as DtList
	 */
	<D extends DtObject> DtList<D> dumDtList(final Class<D> dtoClass);

	/**
	 * Creates a dummy dtObject for the specified type as new (no pk)
	 * @param dtoClass class of the wanted object
	 * @param <D> class of the wanted object
	 * @return dummy value
	 */
	<D extends DtObject> D dumNew(final Class<D> dtoClass);
}
