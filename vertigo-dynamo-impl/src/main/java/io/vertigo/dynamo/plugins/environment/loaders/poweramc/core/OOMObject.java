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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.lang.Assertion;

/**
 * Objets de la grammaire powerAMC.
 * Chaque objet de la grammaire possède une série d'attributs, ceux-ci sont présentés ci-dessous.
 * Il s'agit de faciliter la transposition du Modèle générique XML en un Modèle typé.
 * @author pchretien
 */
final class OOMObject {
	private static final String PROPERTY_CODE = "a:Code";
	private static final String PROPERTY_NAME = "a:Name";
	private static final String PROPERTY_COMMENT = "a:Comment";
	private static final String PROPERTY_PERSISTENT = "a:Persistent";
	private static final String PROPERTY_MULTIPLICITY = "a:Multiplicity";

	private static final String PROPERTY_ROLE_A_MULTIPLICITY = "a:RoleAMultiplicity";
	private static final String PROPERTY_ROLE_B_MULTIPLICITY = "a:RoleBMultiplicity";

	private static final String PROPERTY_ROLE_A_NAVIGABILITY = "a:RoleANavigability";
	private static final String PROPERTY_ROLE_B_NAVIGABILITY = "a:RoleBNavigability";

	//Dans l'OOM le nom du role correspond au libellé et non au code !!
	private static final String PROPERTY_ROLE_A_NAME = "a:RoleAName";
	private static final String PROPERTY_ROLE_B_NAME = "a:RoleBName";

	private static final String PROPERTY_STEREOTYPE = "a:Stereotype";

	private final XmlId id;
	private final OOMObject parent;
	private final OOMType type;
	private final List<OOMObject> children = new ArrayList<>();
	private final List<XmlId> refList = new ArrayList<>();

	//Données spécifiques
	private String code;
	private String name;
	private String stereotype;

	//=========Gestion des attributes============================
	private String label;
	private String multiplicity;
	private String persistent;
	//===========================================================

	//=========Gestion des associations==========================
	private String roleALabel;
	private String roleBLabel;

	private String roleAMultiplicity;
	private String roleBMultiplicity;

	private boolean roleANavigability = false;
	private boolean roleBNavigability = true;

	//============================================================
	private final OOMObject root;

	/**
	 * Constructeur de la racine
	 */
	private OOMObject() {
		id = null;
		type = null;
		parent = null;
		root = this;
	}

	private OOMObject(final OOMObject parent, final XmlId id, final OOMType type) {
		Assertion.checkNotNull(parent);
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(type);
		//-----
		this.parent = parent;
		this.id = id;
		this.type = type;
		root = parent.root;
	}

	/**
	 * @return OOMObject
	 */
	static OOMObject createdRoot() {
		return new OOMObject();
	}

	OOMObject createObjectOOM(final XmlId newId, final OOMType newType) {
		final OOMObject created = new OOMObject(this, newId, newType);
		children.add(created);
		return created;
	}

	List<XmlId> getRefList() {
		return refList;
	}

	List<OOMObject> getChildren() {
		return children;
	}

	OOMType getType() {
		return type;
	}

	OOMObject getParent() {
		return parent;
	}

	XmlId getId() {
		return id;
	}

	void addIdOOM(final XmlId idOOM) {
		refList.add(idOOM);
	}

	String getPersistent() {
		return persistent;
	}

	String getMultiplicity() {
		return multiplicity;
	}

	String getCode() {
		Assertion.checkNotNull(code);
		return code;
	}

	String getName() {
		Assertion.checkNotNull(name);
		return name;
	}

	String getLabel() {
		return label != null ? label : getName();
	}

	String getStereotype() {
		return stereotype;
	}

	//================================ASSOCIATIONS==============================

	/**
	 * @return Libellé du role du noeud A, null si non defini
	 */
	String getRoleALabel() {
		return roleALabel;
	}

	/**
	 * @return Libellé du role du noeud B, null si non defini
	 */
	String getRoleBLabel() {
		return roleBLabel;
	}

	/**
	 * @return Multiplicité du noeud A
	 */
	String getRoleAMultiplicity() {
		return roleAMultiplicity;
	}

	/**
	 * @return Multiplicité du noeud B
	 */
	String getRoleBMultiplicity() {
		return roleBMultiplicity;
	}

	/**
	 * @return Navigabilité du noeud A
	 */
	boolean getRoleANavigability() {
		return roleANavigability;
	}

	/**
	 * @return Navigabilité du noeud B
	 */
	boolean getRoleBNavigability() {
		return roleBNavigability;
	}

	void setProperty(final String propertyName, final String propertyValue) {
		Assertion.checkNotNull(propertyName);
		//-----
		switch (propertyName) {
			case PROPERTY_CODE:
				code = propertyValue;
				break;
			case PROPERTY_NAME:
				name = propertyValue;
				break;
			case PROPERTY_COMMENT:
				label = propertyValue;
				break;
			case PROPERTY_PERSISTENT:
				persistent = propertyValue;
				break;
			case PROPERTY_MULTIPLICITY:
				multiplicity = propertyValue;
				break;
			case PROPERTY_ROLE_A_MULTIPLICITY:
				roleAMultiplicity = propertyValue;
				break;
			case PROPERTY_ROLE_B_MULTIPLICITY:
				roleBMultiplicity = propertyValue;
				break;
			case PROPERTY_ROLE_A_NAVIGABILITY:
				roleANavigability = "1".equals(propertyValue);
				break;
			case PROPERTY_ROLE_B_NAVIGABILITY:
				roleBNavigability = "1".equals(propertyValue);
				break;
			case PROPERTY_ROLE_A_NAME:
				roleALabel = propertyValue;
				break;
			case PROPERTY_ROLE_B_NAME:
				roleBLabel = propertyValue;
				break;
			case PROPERTY_STEREOTYPE:
				stereotype = propertyValue;
				break;
			default:
				//On ne tient pas compte des autres propriétés
		}

	}

	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public String toString() {
		String s = type + "::" + name;
		if (type == OOMType.Association) {
			s += " [roleA=" + getRoleALabel() + ", roleB=" + getRoleBLabel() + ']';
		} else {
			s += " [label=" + label + ", multiplicity=" + multiplicity + ']';
		}
		return s;
	}

	/**
	 * @return package name
	 */
	String getPackageName() {
		//1. On vérifie que cet objet est bien un package
		Assertion.checkArgument(getType() == OOMType.Package, "père de l''objet {0} doit être un package", this);
		//-----
		//2. Si on arrive à la racine on s'arrète
		if (getParent().equals(root)) {
			return getName();
		}
		return getParent().getPackageName() + '.' + getName();
	}
}
