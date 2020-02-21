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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import io.vertigo.core.component.Component;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * Convert Object to Json, and json to Object.
 * Support :
 * - Exception
 * - exclude fields
 * - Security token
 * - UiObject (contains a DtObject buffer and security token if present)
 * @author npiedeloup (17 juil. 2014 11:56:17)
 */
public interface JsonEngine extends Component {

	/** list value fieldname. */
	String EXTENDED_VALUE_FIELDNAME = "value";
	/** server side token fieldname. */
	String SERVER_SIDE_TOKEN_FIELDNAME = "serverToken";

	/**
	 * Standard convert full object to Json.
	 * @param data Object
	 * @return Json string
	 */
	String toJson(Object data);

	/**
	 * Convert Exception to Json
	 * @param th Throwable
	 * @return Json string
	 */
	String toJsonError(Throwable th);

	/**
	 * Convert object to Json but excluded fields.
	 * @param data Object
	 * @param metaDatas metaDatas to include in Json
	 * @param includedFields Set of fields to include (empty means all fields include)
	 * @param excludedFields Set of fields to exclude
	 * @return Json string
	 */
	String toJsonWithMeta(Object data, Map<String, Serializable> metaDatas, final Set<String> includedFields, Set<String> excludedFields);

	/**
	 * Standard convert Json to object.
	 * While converting accept missing object fields and unknown object fields (and then just forgot json value)
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramType Object type
	 * @return Object filled with json typed data
	 */
	<D> D fromJson(String json, Type paramType);

	/**
	 * Specific convertion Json to UiObject.
	 * UiObject is used as a buffer from client input.
	 * While converting accept missing object fields and unknown object fields (and then just forgot json value)
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramType Object type
	 * @return UiObject filled with a DtObject partially filled and the accessToken if present
	 */
	<D extends DtObject> UiObject<D> uiObjectFromJson(String json, Type paramType);

	/**
	 * Specific convertion Json to UiObjectExtended.
	 * UiObjectExtended is used as a buffer from client input with extended attributes.
	 * While converting accept missing object fields and unknown object fields (and then put json's values in extention part)
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramType Object type
	 * @return UiObjectExtended filled with a DtObject partially filled, the accessToken if present, and all other properties send
	 */
	//<D extends DtObject> UiObjectExtended<D> uiObjectExtendedFromJson(String json, Type paramType);

	/**
	 * Specific convertion Json to UiContext.
	 * UiContext is used as a buffer from client input
	 * @param json Json string
	 * @param paramTypes Objects type
	 * @return UiContext filled with a DtObject partially filled and the accessTOken if present
	 */
	UiContext uiContextFromJson(String json, Map<String, Type> paramTypes);

	/**
	 * Specific convertion Json to UiListDelta.
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramType Object type
	 * @return UiListDelta filled with created/updated/deleted DtObjects list partially filled and the accessToken if present
	 */
	<D extends DtObject> UiListDelta<D> uiListDeltaFromJson(String json, Type paramType);

	/**
	 * Specific convertion Json to UiList.
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramType Object type
	 * @return UiList filled with DtObjects list partially filled and the accessToken if present
	 */
	<D extends DtObject> UiListModifiable<D> uiListFromJson(String json, Type paramType);

}
