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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * JsonDeserializer of DtList.
 * Warn : no validators, should use UiList instead.
 * @author npiedeloup
 */
final class DtListDeserializer<D extends DtObject> implements JsonDeserializer<DtList<D>> {
	/** {@inheritDoc} */
	@Override
	public DtList<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
		final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
		final JsonArray jsonArray = json.getAsJsonArray();

		final DtList<D> dtList = new DtList<>(dtoClass);
		for (final JsonElement element : jsonArray) {
			final D inputDto = context.deserialize(element, dtoClass);
			dtList.add(inputDto);
		}
		return dtList;
	}
}
