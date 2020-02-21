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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.vertigo.dynamo.domain.model.DtListState;

final class DtListStateDeserializer implements JsonDeserializer<DtListState> {

	@Override
	public DtListState deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();

		final Integer top = jsonObject.has("top") ? jsonObject.get("top").getAsInt() : null;
		final int skip = jsonObject.has("skip") ? jsonObject.get("skip").getAsInt() : 0;
		final String sortFieldName = jsonObject.has("sortFieldName") ? jsonObject.get("sortFieldName").getAsString() : null;
		final Boolean sortDesc = jsonObject.has("sortDesc") ? jsonObject.get("sortDesc").getAsBoolean() : null;

		return DtListState.of(top, skip, sortFieldName, sortDesc);
	}
}
