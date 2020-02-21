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
package io.vertigo.vega.impl.webservice.catalog;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.SessionLess;

/**
 * Default RestService to list services published.
 * @author npiedeloup (22 juil. 2014 11:12:02)
 */
public final class CatalogWebServices implements WebServices {

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/catalog")
	public List<String> publishCatalog() {
		final List<WebServiceDefinition> webServiceDefinitions = new ArrayList<>(Home.getApp().getDefinitionSpace().getAll(WebServiceDefinition.class));
		Collections.sort(webServiceDefinitions, Comparator.comparing(WebServiceDefinition::getSortPath));
		return publishCatalog(webServiceDefinitions);
	}

	private static List<String> publishCatalog(final Collection<WebServiceDefinition> webServiceDefinitions) {
		final List<String> result = new ArrayList<>();

		final StringBuilder sb = new StringBuilder();
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			final String doc = webServiceDefinition.getDoc();
			if (!doc.isEmpty()) {
				sb.append(" /*")
						.append(webServiceDefinition.getDoc())
						.append("*/")
						.append('\n');
			}
			sb.append(webServiceDefinition.getVerb().name())
					.append(' ')
					.append(webServiceDefinition.getPath())
					.append(" (");
			String sep = "";
			for (final WebServiceParam webServiceParam : webServiceDefinition.getWebServiceParams()) {
				sb.append(sep);
				sb.append(webServiceParam);
				sep = ", ";
			}
			sb.append(')');
			final Type returnType = webServiceDefinition.getMethod().getGenericReturnType();
			if (!void.class.isAssignableFrom(webServiceDefinition.getMethod().getReturnType())) {
				sb.append(" -> ");
				appendTypeToString(sb, returnType);
			}
			result.add(sb.toString());
			sb.setLength(0);
		}
		return result;
	}

	private static void appendTypeToString(final StringBuilder sb, final Type returnType) {
		if (returnType instanceof ParameterizedType) {
			sb.append(((ParameterizedType) returnType).getRawType())
					.append('<');
			String sep = "";
			for (final Type typeArgument : ((ParameterizedType) returnType).getActualTypeArguments()) {
				sb.append(sep);
				appendTypeToString(sb, typeArgument);
				sep = ",";
			}
			sb.append('>');
		} else if (returnType instanceof Class) {
			sb.append(((Class) returnType).getSimpleName());
		} else {
			//le toString colle pour les autres cas
			sb.append(returnType);
		}
	}
}
