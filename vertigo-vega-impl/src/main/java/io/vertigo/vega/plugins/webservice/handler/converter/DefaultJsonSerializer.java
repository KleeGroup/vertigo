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
package io.vertigo.vega.plugins.webservice.handler.converter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.model.ExtendedObject;
import spark.Response;

/**
 * Default JsonConverter.
 * @author npiedeloup
 */
public final class DefaultJsonSerializer implements JsonSerializer {

	private final JsonEngine jsonWriterEngine;

	/**
	 * @param jsonWriterEngine jsonWriterEngine
	 */
	@Inject
	public DefaultJsonSerializer(final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		//-----
		this.jsonWriterEngine = jsonWriterEngine;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return !VFile.class.isAssignableFrom(paramClass);
	}

	/**
	 * encodeType.
	 * jgarnier le 20/05/2015 : les content-type ne doivent pas contenir de ":" mais des "=" pour les paramètres.
	 * http://www.w3.org/Protocols/rfc1341/4_Content-Type.html : Content-Type := type "/" subtype *[";" parameter]
	 * avec : parameter := attribute "=" value
	 */
	enum EncoderType {

		/** Type JSON simple */
		JSON(""),
		/** Type JSON UiContext */
		JSON_UI_CONTEXT("json+uicontext="),
		/** Type JSON list */
		JSON_LIST("json+list=%s"),
		/** Type JSON entity */
		JSON_ENTITY("json+entity=%s");

		private static final String HAS_META_MARKER = "+meta";
		private final Pattern contentTypePattern;
		private final String contentType;

		EncoderType(final String contentType) {
			this.contentType = contentType;
			contentTypePattern = Pattern.compile(contentType.replaceAll("%s", ".+"));
		}

		/**
		 * @param entityName Entity name
		 * @param meta has meta
		 * @return contentType
		 */
		public String createContentType(final String entityName, final boolean meta) {
			return String.format(contentType, entityName) + (meta ? HAS_META_MARKER : "");
		}

		/**
		 * @param testedContentType contentType to test
		 * @return If testedContentType is 'this' EncoderType
		 */
		public boolean isContentType(final String testedContentType) {
			return contentTypePattern.matcher(testedContentType).find();
		}
	}

	/**
	 * EncodedType : encoderType + hasMeta + entityName.
	 */
	static final class EncodedType {
		private final EncoderType encoderType;
		private final boolean meta;
		private final String entityName;

		/**
		 * constructor.
		 * @param encoderType encoderType
		 * @param meta has meta
		 * @param entityName entityName
		 */
		EncodedType(final EncoderType encoderType, final boolean meta, final String entityName) {
			this.encoderType = encoderType;
			this.meta = meta;
			this.entityName = entityName;
		}

		/**
		 * @return encoderType
		 */
		public EncoderType getEncoderType() {
			return encoderType;
		}

		/**
		 * @return contentType
		 */
		public boolean hasMeta() {
			return meta;
		}

		/**
		 * @return entityName
		 */
		public String getEntityName() {
			return entityName;
		}

		/**
		 * @return contentType
		 */
		public String obtainContentType() {
			return encoderType.createContentType(entityName, meta);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object result, final Response response, final WebServiceDefinition webServiceDefinition) {
		final EncodedType encodedType = findEncodedType(result);
		final StringBuilder contentType = new StringBuilder("application/json;charset=UTF-8");
		if (encodedType.getEncoderType() != EncoderType.JSON) {
			contentType.append(';').append(encodedType.obtainContentType());
		}
		response.type(contentType.toString());
		return writeValue(result, response, webServiceDefinition);
	}

	private static EncodedType findEncodedType(final Object value) {
		final EncodedType encodedType;
		if (value instanceof DtList) {
			final DtList<?> dtList = (DtList<?>) value;
			encodedType = new EncodedType(EncoderType.JSON_LIST, hasComplexTypeMeta(dtList), dtList.getDefinition().getClassSimpleName());
		} else if (value instanceof List) {
			final String entityName = ((List) value).isEmpty() ? Object.class.getSimpleName() : ((List) value).get(0).getClass().getSimpleName();
			encodedType = new EncodedType(EncoderType.JSON_LIST, false, entityName);
		} else if (value instanceof DtObject) {
			encodedType = new EncodedType(EncoderType.JSON_ENTITY, false, value.getClass().getSimpleName());
		} else if (value instanceof UiContext) {
			encodedType = new EncodedType(EncoderType.JSON_UI_CONTEXT, false, value.getClass().getSimpleName());
		} else if (value instanceof ExtendedObject<?>) {
			//ce type n'est qu'un conteneur de l'objet sous jacent, lorsqu'il ne contient que le tokenId il ne modifie par le type mime
			final EncodedType innerEncodedType = findEncodedType(((ExtendedObject) value).getInnerObject());
			//si le type interne n'as pas de meta, et que le ExtendedObject contient d'autres metas que le seul serverSideToken, on change le type mime
			if (!innerEncodedType.hasMeta()
					&& !(((ExtendedObject) value).isEmpty()
							|| ((ExtendedObject) value).size() == 1 && ((ExtendedObject) value).containsKey(JsonEngine.SERVER_SIDE_TOKEN_FIELDNAME))) {
				encodedType = new EncodedType(innerEncodedType.getEncoderType(), true, innerEncodedType.getEntityName());
			} else {
				encodedType = innerEncodedType;
			}
		} else {
			encodedType = new EncodedType(EncoderType.JSON, false, value.getClass().getSimpleName());
		}
		return encodedType;
	}

	private static boolean hasComplexTypeMeta(final DtList<?> dtList) {
		for (final String entry : dtList.getMetaDataNames()) {
			final Optional<Serializable> value = dtList.getMetaData(entry, Serializable.class);
			if (value.isPresent()) {
				final Class<?> metaClass = value.get().getClass();
				if (!(metaClass.isPrimitive()
						|| String.class.isAssignableFrom(metaClass)
						|| Integer.class.isAssignableFrom(metaClass)
						|| Long.class.isAssignableFrom(metaClass)
						|| Float.class.isAssignableFrom(metaClass)
						|| Double.class.isAssignableFrom(metaClass)
						|| Date.class.isAssignableFrom(metaClass)
						|| LocalDate.class.isAssignableFrom(metaClass)
						|| Instant.class.isAssignableFrom(metaClass))) {
					return true;
				}
			}
		}
		return false;
	}

	private String writeValue(final Object value, final Response response, final WebServiceDefinition webServiceDefinition) {
		Assertion.checkNotNull(value);
		//-----
		if (value instanceof String
				|| value instanceof Date || value instanceof LocalDate || value instanceof Instant
				|| value instanceof Number
				|| value instanceof Boolean) {
			//optim for primitives
			return jsonWriterEngine.toJson(value);
		} else if (value instanceof DtList && hasComplexTypeMeta((DtList) value)) {
			return toJson(value, getListMetas((DtList) value), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof List) {
			writeListMetaToHeader((List) value, response);
			return toJson(value, Collections.emptyMap(), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof DtObject || value instanceof FacetedQueryResult<?, ?>) {
			return toJson(value, Collections.emptyMap(), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof UiContext) {
			//TODO build json in jsonWriterEngine
			final StringBuilder sb = new StringBuilder().append("{");
			String sep = "";
			for (final Map.Entry<String, Serializable> entry : ((UiContext) value).entrySet()) {
				sb.append(sep);
				final String encodedValue = writeValue(entry.getValue(), response, webServiceDefinition);
				sb.append('\"').append(entry.getKey()).append("\":").append(encodedValue).append("");
				sep = ", ";
			}
			sb.append('}');
			return sb.toString();
		} else if (value instanceof ExtendedObject<?>) {
			final ExtendedObject<?> extendedObject = (ExtendedObject<?>) value;
			return toJson(extendedObject.getInnerObject(), extendedObject, webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else {
			Assertion.checkArgument(webServiceDefinition.getIncludedFields().isEmpty() && webServiceDefinition.getExcludedFields().isEmpty(),
					"IncludedFields and ExcludedFields aren't supported for this object type: {0}, on WebService {1}", value.getClass().getSimpleName(), webServiceDefinition.getMethod());
			return jsonWriterEngine.toJson(value);
		}
	}

	private void writeListMetaToHeader(final List<?> list, final Response response) {
		if (list instanceof DtList) {
			final DtList<?> dtList = (DtList<?>) list;
			for (final String entry : dtList.getMetaDataNames()) {
				final Optional<Serializable> valueOpt = dtList.getMetaData(entry, Serializable.class);
				if (valueOpt.isPresent()) {
					if (valueOpt.get() instanceof String) {
						response.header(entry, (String) valueOpt.get()); //TODO escape somethings ?
					} else {
						response.header(entry, jsonWriterEngine.toJson(valueOpt.get()));
					}
				}
			}
		} //else nothing, there is no meta on standard list
	}

	private static Map<String, Serializable> getListMetas(final DtList<?> dtList) {
		final Map<String, Serializable> metaDatas = new HashMap<>();
		for (final String entry : dtList.getMetaDataNames()) {
			final Optional<Serializable> valueOpt = dtList.getMetaData(entry, Serializable.class);
			valueOpt.ifPresent(value -> metaDatas.put(entry, value));
		}
		return metaDatas;

	}

	private String toJson(final Object value, final Map<String, Serializable> metaData, final Set<String> includedFields, final Set<String> excludedFields) {
		return jsonWriterEngine.toJsonWithMeta(value, metaData, includedFields, excludedFields);
	}
}
