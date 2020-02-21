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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.lang.Assertion;

/**
 * Handler de lecture du fichier XMI généré par Enterprise Architect.
 * La méthode de lecture est directement dépendantes des extensions EA
 * pour les informations des éléments.
 * @author pforhan
 *
 */
public final class EAXmiHandler extends DefaultHandler {
	private static final String ATTR_ID = "xmi:id";
	private static final String ATTR_REF = "xmi:idref";
	private static final String ATTR_TYPE = "xmi:type";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ASSOCIATION = "association";

	private final Map<XmlId, EAXmiObject> map;

	private EAXmiObject currentObject;

	// La première phase définit les objets, les attributs, les associations
	// La deuxième phase remplit les propriétés des objets.
	private boolean phase2;

	private static final Logger LOG = LogManager.getLogger(EAXmiHandler.class);

	EAXmiHandler(final Map<XmlId, EAXmiObject> map) {
		Assertion.checkNotNull(map);
		//-----
		this.map = map;
		currentObject = EAXmiObject.createdRoot();
	}

	/** {@inheritDoc} */
	@Override
	public void startElement(final String unusedUri, final String unusedLocalName, final String name, final Attributes attributes) {
		LOG.debug(" Début de tag : {}", name);
		// Type xmi du tag
		String typeElement = attributes.getValue(ATTR_TYPE);
		// Si le type est null, alors on se base sur le nom du tag (cas des extensions EA)
		if (typeElement == null) {
			typeElement = name;
		}
		LOG.debug("Type : {}", typeElement);

		//Les références
		final String ref = attributes.getValue(ATTR_REF);

		if (ref != null && typeElement != null && EAXmiType.isNodeByRef(typeElement)) {
			phase2 = true;
			LOG.debug("On est dans la référence {} ref : {}", name, ref);
			// Si le tag courant est associé à un objet alors on ajoute à cet objet la référence.
			final XmlId eaXmiId = new XmlId(ref);
			final EAXmiObject eaXmiObject = map.get(eaXmiId);
			if (eaXmiObject != null) {
				currentObject = eaXmiObject;
				LOG.debug("Current Object : {}", currentObject.getName());
			}
			// On ne gère que les éléments objets qui nous intéressent
		} else if (EAXmiType.isObjet(typeElement, name)) {
			parseXmiObject(attributes, typeElement);
			// On peut être dans le cas d'un début de tag utile, on le passe pour le traiter.
		} else if (currentObject != null) {
			currentObject.setProperty(name, attributes);
		}
	}

	private void parseXmiObject(final Attributes attributes, final String typeElement) {
		LOG.debug("On est dans l'objet ");
		final String id = attributes.getValue(ATTR_ID);
		final String leNom = attributes.getValue(ATTR_NAME);
		final EAXmiType type = EAXmiType.getType(typeElement);
		final String association = attributes.getValue(ATTR_ASSOCIATION);
		// On a un type, un id et on n'est pas dans un attribut ajouté à cause d'une association.
		if (type != null && id != null && !(type.isAttribute() && association != null)) {
			//Il existe un nouvel objet géré associé à ce Tag
			final XmlId eaxmiid = new XmlId(id);
			final EAXmiObject obj;
			// Nouvelle classe ou association, on revient au package pour créer l'objet.
			if (currentObject.getType() != null && currentObject.getType().isClass() && type.isClass()) {
				obj = currentObject.getParent().createEAXmiObject(eaxmiid, type, leNom);
			} else {
				obj = currentObject.createEAXmiObject(eaxmiid, type, leNom);
			}
			map.put(eaxmiid, obj);
			currentObject = obj;

		}
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(final String unusedUri, final String unusedLocalName, final String name) {
		// Si c'est un attribut l'objet courant, on revient à la classe qui le contient.
		if (!phase2 && currentObject.getType() != null && currentObject.getType().isAttribute()) {
			currentObject = currentObject.getParent();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(final org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	}

}
