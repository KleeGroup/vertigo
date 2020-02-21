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
package io.vertigo.app.config.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.XmlUtil;

/**
 * Parser XML du paramétrage de l'application.
 * @author npiedeloup, pchretien
 */
final class XmlModulesParser {

	private XmlModulesParser() {
		//
	}

	/**
	 * Parser à partir d'une description XML et d'un fichier de propriétés.
	 */
	static void parseAll(final NodeConfigBuilder nodeConfigBuilder, final Properties properties, final List<URL> managersURLs) {
		Assertion.checkNotNull(nodeConfigBuilder);
		Assertion.checkNotNull(managersURLs);
		Assertion.checkNotNull(properties);
		//-----
		final XmlModulesParams params = new XmlModulesParams(properties);
		for (final URL managersURL : managersURLs) {
			parse(nodeConfigBuilder, managersURL, params);
		}
		//-----
		Assertion.checkArgument(params.unreadProperties().isEmpty(), "Some boot properties are unused {0}; Check they must starts with 'boot.'", params.unreadProperties());
	}

	private static void parse(final NodeConfigBuilder nodeConfigBuilder, final URL managersURL, final XmlModulesParams params) {
		Assertion.checkNotNull(nodeConfigBuilder);
		Assertion.checkNotNull(managersURL);
		//-----
		try {
			doParse(nodeConfigBuilder, managersURL, params);
		} catch (final ParserConfigurationException pce) {
			throw WrappedException.wrap(pce, "Erreur de configuration du parseur (fichier {0}), lors de l'appel à newSAXParser()", managersURL.getPath());
		} catch (final SAXException se) {
			throw WrappedException.wrap(se, "Erreur de parsing (fichier {0}), lors de l'appel à parse()", managersURL.getPath());
		} catch (final IOException ioe) {
			throw WrappedException.wrap(ioe, "Erreur d'entrée/sortie (fichier {0}), lors de l'appel à parse()", managersURL.getPath());
		}
	}

	private static void doParse(final NodeConfigBuilder nodeConfigBuilder, final URL managersURL, final XmlModulesParams params) throws ParserConfigurationException, SAXException, IOException {
		//---validation XSD
		final URL xsd = XmlModulesParser.class.getResource("vertigo_1_0.xsd");
		XmlUtil.validateXmlByXsd(managersURL, xsd);
		//---fin validation XSD

		final XmlModulesHandler handler = new XmlModulesHandler(nodeConfigBuilder, params);
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(managersURL.openStream()), handler);
	}
}
