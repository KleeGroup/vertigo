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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.lang.Assertion;

/**
 * Handler SAX, permettant de parser le OOM.
 *
 * @author pchretien
 */
final class OOMHandler extends DefaultHandler {
	private static final String ATTR_ID = "Id";
	private static final String ATTR_REF = "Ref";

	private final Map<XmlId, OOMObject> map;

	private OOMTag currentTag;

	private String chars;

	OOMHandler(final Map<XmlId, OOMObject> map) {
		Assertion.checkNotNull(map);
		//-----
		this.map = map;
		final OOMObject root = OOMObject.createdRoot();
		currentTag = OOMTag.createRootTag(root);
	}

	/** {@inheritDoc} */
	@Override
	public void startElement(final String unusedUri, final String unusedLocalName, final String name, final org.xml.sax.Attributes attributes) {
		//Dans le cas des références si on trouve une référence sur un objet courant alors on l'ajoute.
		final String ref = attributes.getValue(ATTR_REF);
		if (ref != null && OOMType.isNodeByRef(name) && currentTag.getCurrentOOM() != null) {
			// Si le tag courant est associé à un objet alors on ajoute à cet objet la référence.
			final XmlId idOOM = new XmlId(ref);
			currentTag.getCurrentOOM().addIdOOM(idOOM);
		}

		//Seuls les tags commenéant par o: possède un id et réciproquement
		final boolean isObj = name.startsWith("o:");
		if (isObj) {
			final String id = attributes.getValue(ATTR_ID);
			final OOMType type = OOMType.getType(name);
			if (type != null && id != null) {
				//Il existe un nouvel objet géré associé à ce Tag
				final XmlId idOOM = new XmlId(id);
				final OOMObject obj = currentTag.getParentOOM().createObjectOOM(idOOM, type);
				map.put(idOOM, obj);
				currentTag = currentTag.createTag(obj);
			} else {
				//Ce tag ne contient aucun nouvel objet intéressant
				currentTag = currentTag.createTag();
			}
		}
		//Initialisation des contenus de balise
		chars = "";
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(final String unusedUri, final String unusedLocalName, final String name) {
		final boolean isObj = name.startsWith("o:");
		if (isObj) {
			currentTag = currentTag.getParent();
		}
		//======================================================================
		if (currentTag.getCurrentOOM() != null) {
			currentTag.getCurrentOOM().setProperty(name, chars);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void characters(final char[] charArray, final int start, final int length) {
		chars += new String(charArray, start, length);
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(final org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	}
}
