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
package io.vertigo.studio.plugins.mda.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;

/**
 * Helper.
 *
 * @author emangin
 */
public final class DomainUtil {

	/**
	 * Constructeur privé pour classe utilitaire.
	 */
	private DomainUtil() {
		//RAS
	}

	/**
	 * Construite le type java (sous forme de chaine de caractère) correspondant
	 * à un Domaine.
	 * @param domain DtDomain
	 * @return String
	 */
	public static String buildJavaType(final Domain domain) {
		final String className;
		switch (domain.getScope()) {
			case PRIMITIVE:
				String javaType = domain.getJavaClass().getName();

				//On simplifie l'écriture des types primitifs
				//java.lang.String => String
				if (javaType.startsWith("java.lang.")) {
					javaType = javaType.substring("java.lang.".length());
				}
				className = javaType;
				break;
			case DATA_OBJECT:
				className = domain.getDtDefinition().getClassCanonicalName();
				break;
			case VALUE_OBJECT:
				className = domain.getJavaClass().getName();
				break;
			default:
				throw new IllegalStateException();
		}
		if (domain.isMultiple()) {
			return domain.getTargetJavaClass().getName() + '<' + className + '>';
		}
		return className;
	}

	/**
	 * Construite le label du type java (sous forme de chaine de caractère) correspondant
	 * à un Domaine.
	 * @param domain DtDomain
	 * @return String
	 */
	public static String buildJavaTypeLabel(final Domain domain) {
		final String classLabel;
		switch (domain.getScope()) {
			case PRIMITIVE:
				classLabel = domain.getJavaClass().getSimpleName();
				break;
			case DATA_OBJECT:
				classLabel = domain.getDtDefinition().getClassSimpleName();
				break;
			case VALUE_OBJECT:
				classLabel = domain.getJavaClass().getSimpleName();
				break;
			default:
				throw new IllegalStateException();
		}
		if (domain.isMultiple()) {
			return domain.getTargetJavaClass().getSimpleName() + " de " + classLabel;
		}
		return classLabel;
	}

	public static Collection<DtDefinition> getDtDefinitions() {
		return sortDefinitionCollection(Home.getApp().getDefinitionSpace().getAll(DtDefinition.class));
	}

	public static Map<String, Collection<DtDefinition>> getDtDefinitionCollectionMap() {
		return getDefinitionCollectionMap(getDtDefinitions());
	}

	public static Collection<AssociationSimpleDefinition> getSimpleAssociations() {
		return sortAssociationsCollection(Home.getApp().getDefinitionSpace().getAll(AssociationSimpleDefinition.class));
	}

	public static Collection<AssociationNNDefinition> getNNAssociations() {
		return sortAssociationsCollection(Home.getApp().getDefinitionSpace().getAll(AssociationNNDefinition.class));
	}

	/**
	 * trie de la collection.
	 * @param definitionCollection collection à trier
	 * @return collection triée
	 */
	public static List<DtDefinition> sortDefinitionCollection(final Collection<DtDefinition> definitionCollection) {
		final List<DtDefinition> list = new ArrayList<>(definitionCollection);
		list.sort(Comparator.comparing(DtDefinition::getName));
		return list;
	}

	/**
	 * @param definitionCollection collection à traiter
	 * @return map ayant le package name en clef
	 */
	private static Map<String, Collection<DtDefinition>> getDefinitionCollectionMap(final Collection<DtDefinition> definitions) {
		final Map<String, Collection<DtDefinition>> map = new LinkedHashMap<>();

		for (final DtDefinition definition : definitions) {
			map.computeIfAbsent(definition.getPackageName(),
					k -> new ArrayList<>())
					.add(definition);
		}
		return map;
	}

	private static <A extends AssociationDefinition> Collection<A> sortAssociationsCollection(final Collection<A> associationCollection) {
		final List<A> list = new ArrayList<>(associationCollection);
		list.sort(Comparator.comparing(AssociationDefinition::getName));
		return list;
	}
}
