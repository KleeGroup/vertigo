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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.collections.model.SelectedFacetValues;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.ListVAccessor;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.model.VAccessor;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Tuple;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.webservice.WebServiceTypeUtil;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.UiList;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * @author pchretien, npiedeloup
 */
public final class GoogleJsonEngine implements JsonEngine {
	private static final String FIRST_LEVEL_KEY = "this";
	private final Gson gson;

	private enum SearchApiVersion {
		V1(FacetedQueryResultJsonSerializerV1.class), //first api
		V2(FacetedQueryResultJsonSerializerV2.class), //with array instead of object
		V3(FacetedQueryResultJsonSerializerV3.class), //with code label, count on facets
		V4(FacetedQueryResultJsonSerializerV4.class); //with highlights and code, label for facet

		private final Class<? extends JsonSerializer<FacetedQueryResult<?, ?>>> jsonSerializerClass;

		<C extends JsonSerializer<FacetedQueryResult<?, ?>>> SearchApiVersion(final Class<C> jsonSerializerClass) {
			this.jsonSerializerClass = jsonSerializerClass;
		}

		Class<? extends JsonSerializer<FacetedQueryResult<?, ?>>> getJsonSerializerClass() {
			return jsonSerializerClass;
		}
	}

	@Inject
	public GoogleJsonEngine(@ParamValue("serializeNulls") final Optional<Boolean> serializeNulls, @ParamValue("searchApiVersion") final Optional<String> searchApiVersionStr) {
		final SearchApiVersion searchApiVersion = SearchApiVersion.valueOf(searchApiVersionStr.orElse(SearchApiVersion.V4.name()));
		gson = createGson(serializeNulls.orElse(false), searchApiVersion);
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object data) {
		return gson.toJson(data);
	}

	/** {@inheritDoc} */
	@Override
	public String toJsonWithMeta(final Object data, final Map<String, Serializable> metaDatas, final Set<String> includedFields, final Set<String> excludedFields) {
		final JsonElement jsonValue = gson.toJsonTree(data);
		filterFields(jsonValue, includedFields, excludedFields);

		if (metaDatas.isEmpty() && (data instanceof List || jsonValue.isJsonPrimitive())) {
			return gson.toJson(jsonValue); //only case where result wasn't an object
		}

		final JsonObject jsonResult;
		if (data instanceof List || jsonValue.isJsonPrimitive()) {
			jsonResult = new JsonObject();
			jsonResult.add(EXTENDED_VALUE_FIELDNAME, jsonValue);
		} else {
			jsonResult = jsonValue.getAsJsonObject();
		}
		final JsonObject jsonMetaData = gson.toJsonTree(metaDatas).getAsJsonObject();
		for (final Entry<String, JsonElement> entry : jsonMetaData.entrySet()) {
			jsonResult.add(entry.getKey(), entry.getValue());
		}
		return gson.toJson(jsonResult);
	}

	/** {@inheritDoc} */
	@Override
	public String toJsonError(final Throwable th) {
		final String exceptionMessage = th.getMessage() != null ? th.getMessage() : th.getClass().getSimpleName();
		return gson.toJson(Collections.singletonMap("globalErrors", Collections.singletonList(exceptionMessage)));
	}

	/** {@inheritDoc} */
	@Override
	public <D> D fromJson(final String json, final Type paramType) {
		return gson.fromJson(json, paramType);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiObject<D> uiObjectFromJson(final String json, final Type paramType) {
		final Type typeOfDest = createParameterizedType(UiObject.class, paramType);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiListDelta<D> uiListDeltaFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiListModifiable<D> uiListFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListModifiable.class, dtoClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public UiContext uiContextFromJson(final String json, final Map<String, Type> paramTypes) {
		final UiContext result = new UiContext();
		try {
			final JsonElement jsonElement = new JsonParser().parse(json);
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (final Entry<String, Type> entry : paramTypes.entrySet()) {
				final String key = entry.getKey();
				final Type paramType = entry.getValue();
				final JsonElement jsonSubElement = jsonObject.get(key);

				final Serializable value;
				if (WebServiceTypeUtil.isAssignableFrom(DtObject.class, paramType)) {
					final Type typeOfDest = new KnownParameterizedType(UiObject.class, paramType);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (WebServiceTypeUtil.isAssignableFrom(DtListDelta.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
					final Type typeOfDest = new KnownParameterizedType(UiListDelta.class, dtoClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (WebServiceTypeUtil.isAssignableFrom(DtList.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
					final Type typeOfDest = new KnownParameterizedType(UiListModifiable.class, dtoClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else {
					value = gson.fromJson(jsonSubElement, paramType);
				}
				result.put(key, value);
			}
			return result;
		} catch (final IllegalStateException e) {
			throw new JsonSyntaxException("JsonObject expected", e);
		}
	}

	private static Type createParameterizedType(final Class<?> rawClass, final Type paramType) {
		final Type[] typeArguments = { paramType };
		return new KnownParameterizedType(rawClass, typeArguments);
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		/** {@inheritDoc} */
		@Override
		public boolean shouldSkipField(final FieldAttributes arg0) {
			return arg0.getAnnotation(JsonExclude.class) != null;
		}

		@Override
		public boolean shouldSkipClass(final Class<?> arg0) {
			return false;
		}
	}

	private static final class ClassJsonSerializer implements JsonSerializer<Class> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Class src, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(src.getName());
		}
	}

	private static final class OptionJsonSerializer implements JsonSerializer<Optional> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Optional src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isPresent()) {
				return context.serialize(src.get());
			}
			return null; //rien
		}
	}

	private final class EntityJsonAdapter<D extends DtObject> implements JsonSerializer<D>, JsonDeserializer<D> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final D src, final Type typeOfSrc, final JsonSerializationContext context) {
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(src.getClass());
			final JsonObject jsonObject = new JsonObject();

			dtDefinition.getFields()
					.stream()
					.filter(dtField -> dtField.getType() != FieldType.COMPUTED)// we don't serialize computed fields
					.forEach(field -> {
						jsonObject.add(field.getName(), context.serialize(field.getDataAccessor().getValue(src)));
					});

			Stream.of(src.getClass().getDeclaredFields())
					.filter(field -> VAccessor.class.isAssignableFrom(field.getType()))
					.map(field -> getAccessor(field, src))
					.filter(VAccessor::isLoaded)
					.forEach(accessor -> {
						jsonObject.add(accessor.getRole(), context.serialize(accessor.get()));
					});

			Stream.of(src.getClass().getDeclaredFields())
					.filter(field -> ListVAccessor.class.isAssignableFrom(field.getType()))
					.map(field -> getListAccessor(field, src))
					.filter(ListVAccessor::isLoaded)
					.forEach(accessor -> {
						jsonObject.add(StringUtil.first2LowerCase(accessor.getRole()), context.serialize(accessor.get()));
					});
			return jsonObject;

		}

		@Override
		public D deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition((Class<D>) typeOfT);

			// we use as base the default deserialization
			final D dtObject = (D) gson.getDelegateAdapter(null, TypeToken.get(typeOfT)).fromJsonTree(json);
			final JsonObject jsonObject = json.getAsJsonObject();

			//for now Many relationships (represented by ListVAccessor) are readonly so we don't handle them at deserialization

			// case of the lazy objet passed
			Stream.of(((Class<D>) typeOfT).getDeclaredFields())
					.filter(field -> VAccessor.class.isAssignableFrom(field.getType()))
					.map(field -> Tuple.of(field, getAccessor(field, dtObject)))
					.filter(tuple -> jsonObject.has(tuple.getVal2().getRole()))
					.forEach(tuple -> tuple.getVal2().set(context.deserialize(jsonObject.get(tuple.getVal2().getRole()), ClassUtil.getGeneric(tuple.getVal1()))));

			// case of the fk we need to handle after because it's the primary information
			dtDefinition.getFields()
					.stream()
					.filter(field -> field.getType() == FieldType.FOREIGN_KEY)
					.forEach(field -> field.getDataAccessor()
							.setValue(
									dtObject,
									context.deserialize(jsonObject.get(field.getName()), field.getDomain().getJavaClass())));

			return dtObject;

		}

	}

	private static VAccessor getAccessor(final Field field, final Object object) {
		try {
			field.setAccessible(true);
			return (VAccessor) field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static ListVAccessor getListAccessor(final Field field, final Object object) {
		try {
			field.setAccessible(true);
			return (ListVAccessor) field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static final class VAccessorJsonSerializer implements JsonSerializer<VAccessor> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final VAccessor src, final Type typeOfSrc, final JsonSerializationContext context) {
			return null;

		}

	}

	private static final class DefinitionReferenceJsonSerializer implements JsonSerializer<DefinitionReference> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final DefinitionReference src, final Type typeOfSrc, final JsonSerializationContext context) {
			return context.serialize(src.get().getName());
		}
	}

	private static final class MapJsonSerializer implements JsonSerializer<Map> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Map src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ListJsonSerializer implements JsonSerializer<List> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final List src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class URIJsonAdapter implements JsonSerializer<UID>, JsonDeserializer<UID> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final UID uri, final Type typeOfSrc, final JsonSerializationContext context) {
			if (typeOfSrc instanceof ParameterizedType) {
				return new JsonPrimitive(String.valueOf(uri.getId()));
			}
			return new JsonPrimitive(uri.urn());

		}

		/** {@inheritDoc} */
		@Override
		public UID deserialize(final JsonElement json, final Type paramType, final JsonDeserializationContext paramJsonDeserializationContext) {
			final String uidJsonValue = json.getAsString();
			if (paramType instanceof ParameterizedType
					&& uidJsonValue != null && uidJsonValue.indexOf('@') == -1) { //Temporaly we accecpt two UID patterns : key only or urn
				final Class<Entity> entityClass = (Class<Entity>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that UID has one parameterized type
				final DtDefinition entityDefinition = DtObjectUtil.findDtDefinition(entityClass);
				Object entityId;
				try {
					entityId = entityDefinition.getIdField().get().getDomain().stringToValue(uidJsonValue);
				} catch (final FormatterException e) {
					throw new JsonParseException("Unsupported UID format " + uidJsonValue, e);
				}
				return UID.of(entityClass, entityId);
			}
			return UID.of(uidJsonValue);
		}
	}

	private static class UTCDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Date date, final Type type, final JsonSerializationContext jsonSerializationContext) {
			//Use INPUT_DATE_FORMATS[0] => ISO8601 format
			return new JsonPrimitive(UTCDateUtil.format(date));
		}

		/** {@inheritDoc} */
		@Override
		public Date deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return UTCDateUtil.parse(jsonElement.getAsString());
		}
	}

	private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final LocalDate date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-mm-dd"
		}

		/** {@inheritDoc} */
		@Override
		public LocalDate deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return LocalDate.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
		}
	}

	private static class ZonedDateTimeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final ZonedDateTime date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")))); // "yyyy-mm-ddTHH:MI:SSZ"
		}

		/** {@inheritDoc} */
		@Override
		public ZonedDateTime deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return ZonedDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")));
		}
	}

	private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Instant date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.toString()); // "yyyy-mm-ddTHH:MI:SSZ"
		}

		/** {@inheritDoc} */
		@Override
		public Instant deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return UTCDateUtil.parseInstant(jsonElement.getAsString());
		}
	}

	private static class EmptyStringAsNull implements JsonDeserializer<String> {

		/** {@inheritDoc} */
		@Override
		public String deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			final String value = jsonElement.getAsString();
			if (value != null && value.isEmpty()) {
				return null;
			}
			return value;
		}
	}

	private Gson createGson(final boolean serializeNulls, final SearchApiVersion searchApiVersion) {
		try {
			final GsonBuilder gsonBuilder = new GsonBuilder();
			if (serializeNulls) {
				gsonBuilder.serializeNulls();
			}
			return gsonBuilder
					.setPrettyPrinting()
					//.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.registerTypeHierarchyAdapter(Entity.class, new EntityJsonAdapter())
					.registerTypeAdapter(Date.class, new UTCDateAdapter())
					.registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
					.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
					.registerTypeAdapter(Instant.class, new InstantAdapter())
					.registerTypeAdapter(String.class, new EmptyStringAsNull())// add "" <=> null
					.registerTypeAdapter(UiObject.class, new UiObjectDeserializer<>())
					.registerTypeAdapter(UiListDelta.class, new UiListDeltaDeserializer<>())
					.registerTypeHierarchyAdapter(UiList.class, new UiListDeserializer<>())
					.registerTypeAdapter(DtList.class, new DtListDeserializer<>())
					.registerTypeAdapter(DtListState.class, new DtListStateDeserializer())
					.registerTypeAdapter(FacetedQueryResult.class, searchApiVersion.getJsonSerializerClass().newInstance())
					.registerTypeAdapter(SelectedFacetValues.class, new SelectedFacetValuesDeserializer())
					.registerTypeAdapter(List.class, new ListJsonSerializer())
					.registerTypeAdapter(Map.class, new MapJsonSerializer())
					.registerTypeAdapter(DefinitionReference.class, new DefinitionReferenceJsonSerializer())
					.registerTypeAdapter(Optional.class, new OptionJsonSerializer())
					.registerTypeAdapter(VAccessor.class, new VAccessorJsonSerializer())
					.registerTypeAdapter(Class.class, new ClassJsonSerializer())
					.registerTypeAdapter(UID.class, new URIJsonAdapter())
					.addSerializationExclusionStrategy(new JsonExclusionStrategy())
					.create();
		} catch (InstantiationException | IllegalAccessException e) {
			throw WrappedException.wrap(e, "Can't create Gson");
		}
	}

	private void filterFields(final JsonElement jsonElement, final Set<String> includedAllFields, final Set<String> excludedAllFields) {
		if (jsonElement == null) {
			return; //if filtering an missing field
		} else if (jsonElement.isJsonArray()) {
			final JsonArray jsonArray = jsonElement.getAsJsonArray();
			for (final JsonElement jsonSubElement : jsonArray) {
				filterFields(jsonSubElement, includedAllFields, excludedAllFields);
			}
		} else if (jsonElement.isJsonObject()) {
			filterObjectFields(jsonElement, includedAllFields, excludedAllFields);
		}
		//else Primitive : no exclude
	}

	private void filterObjectFields(final JsonElement jsonElement, final Set<String> includedAllFields, final Set<String> excludedAllFields) {
		final Map<String, Tuple<Set<String>, Set<String>>> filteredSubFields = parseSubFieldName(includedAllFields, excludedAllFields);
		final Tuple<Set<String>, Set<String>> firstLevel = filteredSubFields.get(FIRST_LEVEL_KEY);
		final Set<String> includedFields;
		final Set<String> excludedFields;
		if (firstLevel != null) { //Sonar préfère à contains
			includedFields = filteredSubFields.get(FIRST_LEVEL_KEY).getVal1();
			excludedFields = filteredSubFields.get(FIRST_LEVEL_KEY).getVal2();
		} else {
			includedFields = Collections.emptySet();
			excludedFields = Collections.emptySet();
		}

		final JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (final String excludedField : excludedFields) {
			jsonObject.remove(excludedField);
		}

		if (!includedFields.isEmpty()) {
			final Set<String> notIncludedFields = new HashSet<>();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				if (!includedFields.contains(entry.getKey())) {
					notIncludedFields.add(entry.getKey());
				}
			}
			for (final String notIncludedField : notIncludedFields) {
				jsonObject.remove(notIncludedField);
			}
		}

		for (final Map.Entry<String, Tuple<Set<String>, Set<String>>> filteredField : filteredSubFields.entrySet()) {
			if (filteredField.getValue() != null) {
				filterFields(jsonObject.get(filteredField.getKey()), filteredField.getValue().getVal1(), filteredField.getValue().getVal2());
			}
		}
	}

	private Map<String, Tuple<Set<String>, Set<String>>> parseSubFieldName(final Set<String> includedFields, final Set<String> excludedFields) {
		if (includedFields.isEmpty() && excludedFields.isEmpty()) {
			return Collections.emptyMap();
		}
		final Map<String, Tuple<Set<String>, Set<String>>> subFields = new HashMap<>();
		parseSubFieldName(includedFields, subFields, Tuple::getVal1);
		parseSubFieldName(excludedFields, subFields, Tuple::getVal2);
		return subFields;
	}

	private static void parseSubFieldName(final Set<String> filteredFields, final Map<String, Tuple<Set<String>, Set<String>>> subFields, final Function<Tuple<Set<String>, Set<String>>, Set<String>> getter) {
		for (final String filteredField : filteredFields) {
			final int commaIdx = filteredField.indexOf('.');
			final String key;
			final String value;
			if (commaIdx > -1) {
				key = filteredField.substring(0, commaIdx);
				value = filteredField.substring(commaIdx + 1);
			} else {
				key = FIRST_LEVEL_KEY;
				value = filteredField;
			}
			final Tuple<Set<String>, Set<String>> tuple = subFields.computeIfAbsent(key,
					k -> Tuple.of(new HashSet<>(), new HashSet<>()));
			getter.apply(tuple).add(value);
		}
	}
}
