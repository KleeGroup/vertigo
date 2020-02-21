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

import java.util.Map;
import java.util.Set;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.engines.webservice.json.UiListDelta;
import io.vertigo.vega.engines.webservice.json.UiListModifiable;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * @author npiedeloup
 */
final class UiObjectUtil {
	private static final String FORBIDDEN_OPERATION_FIELD_MODIFICATION = "Can't modify field: {0}";

	private UiObjectUtil() {
		//nothing
	}

	static void postReadUiListDelta(final UiListDelta<DtObject> uiListDelta, final String inputKey, final WebServiceParam webServiceParam) {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getCreatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, webServiceParam);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getUpdatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, webServiceParam);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getDeletesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, webServiceParam);
		}
	}

	static void postReadUiList(final UiListModifiable<DtObject> uiList, final String inputKey, final WebServiceParam webServiceParam) {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		int index = 0;
		for (final UiObject<DtObject> entry : uiList) {
			final String uiObjectInputKey = prefix + "idx" + index;
			postReadUiObject(entry, uiObjectInputKey, webServiceParam);
			index++;
		}
	}

	static void postReadUiObject(final UiObject<DtObject> uiObject, final String inputKey, final WebServiceParam webServiceParam) {
		uiObject.setInputKey(inputKey);
		checkUnauthorizedFieldModifications(uiObject, webServiceParam);
	}

	private static void checkUnauthorizedFieldModifications(final UiObject<DtObject> uiObject, final WebServiceParam webServiceParam) {
		for (final String excludedField : webServiceParam.getExcludedFields()) {
			if (uiObject.isModified(excludedField)) {
				throw new VSecurityException(MessageText.of(FORBIDDEN_OPERATION_FIELD_MODIFICATION, excludedField));
			}
		}
		final Set<String> includedFields = webServiceParam.getIncludedFields();
		if (!includedFields.isEmpty()) {
			for (final String modifiedField : uiObject.getModifiedFields()) {
				if (!includedFields.contains(modifiedField)) {
					throw new VSecurityException(MessageText.of(FORBIDDEN_OPERATION_FIELD_MODIFICATION, modifiedField));
				}
			}
		}
	}
}
