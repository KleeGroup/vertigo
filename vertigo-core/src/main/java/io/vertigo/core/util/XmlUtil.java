/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

/**
 * XSD Validation.
 * @author pchretien
 */
public final class XmlUtil {

	private static final String INTERNAL_XML_SCHEMA_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory";

	/**
	 * Constructor.
	 */
	private XmlUtil() {
		//private constructor
	}

	/**
	 * Util to validate XML with a XSD.
	 * @param xsd XSD
	 * @param xml XML to validate
	 */
	public static void validateXmlByXsd(final URL xml, final URL xsd) {
		Assertion.check()
				.isNotNull(xml)
				.isNotNull(xsd);
		//---
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI, INTERNAL_XML_SCHEMA_FACTORY, Thread.currentThread().getContextClassLoader());
		try {
			schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final Validator validator = schemaFactory
					.newSchema(xsd)
					.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			final Source source = new StreamSource(xml.openStream());
			validate(xml, validator, source);
		} catch (final SocketException e) {
			throw WrappedException.wrap(e, "'{0}' may refer an DTD, you should removed <!DOCTYPE header tag", xml);
		} catch (final SAXException | IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static void validate(final URL xml, final Validator validator, final Source source) throws IOException {
		try {
			validator.validate(source);
		} catch (final SAXException e) {
			throw WrappedException.wrap(e, "'{0}' is not valid", xml);
		}
	}
}
