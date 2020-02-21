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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * ParameterizedType use for UiListDelta.
 * @author npiedeloup
 */
final class UiListDeltaDeserializer<D extends DtObject> implements JsonDeserializer<UiListDelta<D>> {

	/** {@inheritDoc} */
	@Override
	public UiListDelta<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
		final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
		final Type uiObjectType = new KnownParameterizedType(UiObject.class, dtoClass);
		final JsonObject jsonObject = json.getAsJsonObject();

		final Map<String, UiObject<D>> collCreates = parseUiObjectMap(jsonObject, "collCreates", uiObjectType, context);
		final Map<String, UiObject<D>> collUpdates = parseUiObjectMap(jsonObject, "collUpdates", uiObjectType, context);
		final Map<String, UiObject<D>> collDeletes = parseUiObjectMap(jsonObject, "collDeletes", uiObjectType, context);

		return new UiListDelta<>(dtoClass, collCreates, collUpdates, collDeletes);
	}

	private Map<String, UiObject<D>> parseUiObjectMap(final JsonObject jsonObject, final String propertyName, final Type uiObjectType, final JsonDeserializationContext context) {
		final Map<String, UiObject<D>> uiObjectMap = new HashMap<>();
		final JsonObject jsonUiObjectMap = jsonObject.getAsJsonObject(propertyName);
		if (jsonUiObjectMap != null) {
			for (final Entry<String, JsonElement> entry : jsonUiObjectMap.entrySet()) {
				final String entryName = entry.getKey();
				final UiObject<D> inputDto = context.deserialize(entry.getValue(), uiObjectType);
				uiObjectMap.put(entryName, inputDto);
			}
		}
		return uiObjectMap;
	}
}
