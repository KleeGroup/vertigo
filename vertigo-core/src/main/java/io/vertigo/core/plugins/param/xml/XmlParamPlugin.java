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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamPlugin;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;
import io.vertigo.util.XmlUtil;

/**
 * Parser XML du paramétrage de la config.
 * @author  pchretien
 */
public final class XmlParamPlugin implements ParamPlugin {
	private final Map<String, Param> params;

	/**
	 * Constructor.
	 * @param resourceManager Selector
	 * @param url Url du fichier XML de configuration
	 */
	@Inject
	public XmlParamPlugin(final ResourceManager resourceManager, @ParamValue("url") final String url) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(url);
		//-----
		final URL configURL = resourceManager.resolve(url);
		params = readXML(configURL);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Param> getParam(final String paramName) {
		Assertion.checkArgNotEmpty(paramName);
		//-----
		return Optional.ofNullable(params.get(paramName));
	}

	/**
	 * Charge une configuration, et complète celle existante.
	 */
	private static Map<String, Param> readXML(final URL configURL) {
		Assertion.checkNotNull(configURL);
		//-----
		try {
			return doReadXML(configURL);
		} catch (final ParserConfigurationException pce) {
			throw WrappedException.wrap(pce, StringUtil.format("Erreur de configuration du parseur (fichier {0}), lors de l'appel à newSAXParser()", configURL.getPath()));
		} catch (final SAXException se) {
			throw WrappedException.wrap(se, StringUtil.format("Erreur de parsing (fichier {0}), lors de l'appel à parse()", configURL.getPath()));
		} catch (final IOException ioe) {
			throw WrappedException.wrap(ioe, StringUtil.format("Erreur d'entrée/sortie (fichier {0}), lors de l'appel à parse()", configURL.getPath()));
		}
	}

	private static Map<String, Param> doReadXML(final URL configURL) throws SAXException, IOException, ParserConfigurationException {
		xsdValidate(configURL);
		//---
		final XmlConfigHandler handler = new XmlConfigHandler();
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(configURL.openStream()), handler);
		return handler.getParams();
	}

	private static void xsdValidate(final URL configURL) {
		//--- validation XSD
		final URL xsd = XmlParamPlugin.class.getResource("vertigo-config_1_0.xsd");
		XmlUtil.validateXmlByXsd(configURL, xsd);
		//--- fin validation XSD
	}

}
