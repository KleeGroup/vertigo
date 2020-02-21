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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.plugins.environment.loaders.xml.AbstractXmlLoader;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlClass;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Loader de fichier XMI version Enterprise Architect.
 * @author pforhan
 */
public final class EAXmiLoader extends AbstractXmlLoader {
	private static final Pattern CODE_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
	private static final Logger LOG = LogManager.getLogger(EAXmiLoader.class);
	private final Map<XmlId, EAXmiObject> map = new LinkedHashMap<>();

	/**
	 * Constructor.
	 * @param constFieldNameInSource FieldName in file is in CONST_CASE instead of camelCase
	 * @param resourceManager the vertigo resourceManager
	 */
	public EAXmiLoader(final boolean constFieldNameInSource, final ResourceManager resourceManager) {
		super(constFieldNameInSource, resourceManager);
	}

	@Override
	protected DefaultHandler getHandler() {
		return new EAXmiHandler(map);
	}

	/**
	 * Récupération des classes déclarées dans le XMI.
	 * @return Liste des classes
	 */
	@Override
	public List<XmlClass> getClasses() {
		return map.values()
				.stream()
				.peek(obj -> LOG.debug("class : {}", obj))
				//On ne conserve que les classes et les domaines
				.filter(obj -> obj.getType() == EAXmiType.Class)
				.map(obj -> createClass(obj, isConstFieldNameInSource()))
				.collect(Collectors.toList());
	}

	/**
	 * Récupération des associations déclarées dans le XMI.
	 * @return Liste des associations
	 */
	@Override
	public List<XmlAssociation> getAssociations() {
		return map.values()
				.stream()
				.filter(obj -> obj.getType() == EAXmiType.Association)
				.map(obj -> createAssociation(obj, isConstFieldNameInSource()))
				.collect(Collectors.toList());
	}

	private static XmlClass createClass(final EAXmiObject obj, final boolean constFieldNameInSource) {
		LOG.debug("Creation de classe : {}", obj.getName());
		//On recherche les attributs (>DtField) de cette classe(>Dt_DEFINITION)
		final String code = constFieldNameInSource ? StringUtil.constToUpperCamelCase(obj.getName().toUpperCase(Locale.ENGLISH)) : obj.getName();
		final String packageName = obj.getParent().getPackageName();
		final String stereotype = obj.getStereotype();

		final List<XmlAttribute> keyAttributes = new ArrayList<>();
		final List<XmlAttribute> fieldAttributes = new ArrayList<>();
		for (final EAXmiObject child : obj.getChildren()) {
			if (child.getType() == EAXmiType.Attribute) {
				LOG.debug("Attribut = {} isId = {}", child.getName(), Boolean.toString(child.getIsId()));
				if (child.getIsId()) {
					final XmlAttribute attributeXmi = createAttribute(child, true, constFieldNameInSource);
					keyAttributes.add(attributeXmi);
				} else {
					fieldAttributes.add(createAttribute(child, false, constFieldNameInSource));
				}
			}
		}
		return new XmlClass(code, packageName, stereotype, keyAttributes, fieldAttributes);
	}

	private static XmlAttribute createAttribute(final EAXmiObject obj, final boolean isPK, final boolean constFieldNameInSource) {
		final String code = obj.getName();
		Assertion.checkArgument(CODE_PATTERN.matcher(code).matches(), "Code {0} must use a simple charset a-z A-Z 0-9 or _", code);
		final String fieldName = constFieldNameInSource ? StringUtil.constToLowerCamelCase(code.toUpperCase(Locale.ENGLISH)) : code;
		final String domainName = constFieldNameInSource ? StringUtil.constToUpperCamelCase(obj.getDomain().toUpperCase(Locale.ENGLISH)) : obj.getDomain();
		final String label = obj.getLabel();
		final boolean persistent = true;

		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		// L'information de persistence ne peut pas être déduite du Xmi, tous les champs sont déclarés persistent de facto
		return new XmlAttribute(fieldName, label, persistent, notNull, domainName);
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association
	 */
	private XmlAssociation createAssociation(final EAXmiObject obj, final boolean constFieldNameInSource) {
		LOG.debug("Créer association : {}", obj.getName());
		//On recherche les objets référencés par l'association.
		final EAXmiObject objectB = map.get(obj.getClassB());
		final EAXmiObject objectA = map.get(obj.getClassA());

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}

		final String code = constFieldNameInSource ? StringUtil.constToUpperCamelCase(obj.getName().toUpperCase(Locale.ENGLISH)) : obj.getName();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();
		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String computedRoleA = constFieldNameInSource ? StringUtil.constToUpperCamelCase(objectA.getName()) : objectA.getName();
		final String computedRoleB = constFieldNameInSource ? StringUtil.constToUpperCamelCase(objectB.getName()) : objectB.getName();
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : computedRoleA;
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : computedRoleB;
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = constFieldNameInSource ? StringUtil.constToUpperCamelCase(objectA.getName().toUpperCase(Locale.ENGLISH)) : objectA.getName();
		final String codeB = constFieldNameInSource ? StringUtil.constToUpperCamelCase(objectB.getName().toUpperCase(Locale.ENGLISH)) : objectB.getName();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
		final boolean navigabilityA = obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability();
		return new XmlAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}

	@Override
	public String getType() {
		return "xmi";
	}

}
