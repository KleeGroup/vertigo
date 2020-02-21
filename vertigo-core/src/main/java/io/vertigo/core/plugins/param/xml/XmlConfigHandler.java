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
package io.vertigo.core.plugins.param.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;

/**
 * @author  pchretien
 */
final class XmlConfigHandler extends DefaultHandler {
	enum TagName {
		config,
		path,
		param
	}

	private final Map<String, Param> params;
	private String currentPath;

	XmlConfigHandler() {
		this.params = new HashMap<>();
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		switch (TagName.valueOf(qName)) {
			case config:
				break;
			case path:
				currentPath = attrs.getValue("name").trim();
				break;
			case param:
				final String paramName = attrs.getValue("name").trim();
				Assertion.checkArgument(!paramName.endsWith("."), "a path must not be ended with a point");
				final String paramValue = attrs.getValue("value").trim();
				final Param param = Param.of(currentPath + "." + paramName, paramValue);
				params.put(param.getName(), param);
				break;
			default:
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		switch (TagName.valueOf(qName)) {
			case config:
			case param:
				break;
			case path:
				currentPath = null;
				break;
			default:
		}
	}

	Map<String, Param> getParams() {
		return params;
	}
}
