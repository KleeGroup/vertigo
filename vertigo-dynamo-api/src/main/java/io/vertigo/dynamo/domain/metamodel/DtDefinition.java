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
package io.vertigo.dynamo.domain.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.lang.Assertion;

/**
 * The DtDefinition class defines the definition of data.
 *
 * @author pchretien
 */
@DefinitionPrefix("Dt")
public final class DtDefinition implements Definition {
	/** the dataSpace must match this pattern. */
	public static final Pattern REGEX_DATA_SPACE = Pattern.compile("[a-z][a-zA-Z0-9]{3,60}");

	/** if the definition is a fragment. */
	private final Optional<DefinitionReference<DtDefinition>> fragmentOpt;

	/** name of the definition. */
	private final String name;

	/** name of the package. */
	private final String packageName;

	/** List of fields.  */
	private final List<DtField> fields = new ArrayList<>();

	/** Map. (fieldName, DtField). */
	private final Map<String, DtField> mappedFields = new HashMap<>();

	private final DtStereotype stereotype;

	/** id Field */
	private final Optional<DtField> idFieldOpt;

	private final Optional<DtField> sortFieldOpt;
	private final Optional<DtField> displayFieldOpt;
	private final Optional<DtField> handleFieldOpt;

	private final String dataSpace;

	/**
	 * Constructor.
	 */
	DtDefinition(
			final String name,
			final Optional<DefinitionReference<DtDefinition>> fragment,
			final String packageName,
			final DtStereotype stereotype,
			final List<DtField> dtFields,
			final String dataSpace,
			final Optional<DtField> sortField,
			final Optional<DtField> displayField,
			final Optional<DtField> handleField) {
		DefinitionUtil.checkName(name, DtDefinition.class);
		Assertion.checkNotNull(fragment);
		Assertion.checkNotNull(stereotype);
		Assertion.checkNotNull(dtFields);
		Assertion.checkArgNotEmpty(dataSpace);
		Assertion.checkState(REGEX_DATA_SPACE.matcher(dataSpace).matches(), "dataSpace {0} must match pattern {1}", dataSpace, REGEX_DATA_SPACE);
		Assertion.checkNotNull(sortField);
		Assertion.checkNotNull(displayField);
		Assertion.checkNotNull(handleField);
		//-----
		this.name = name;
		//
		fragmentOpt = fragment;
		//
		this.stereotype = stereotype;
		this.packageName = packageName;
		DtField id = null;

		sortFieldOpt = sortField;
		displayFieldOpt = displayField;
		handleFieldOpt = handleField;

		for (final DtField dtField : dtFields) {
			Assertion.when(stereotype.isPersistent() && dtField.isPersistent())
					.check(() -> dtField.getDomain().getScope().isPrimitive() && !dtField.getDomain().isMultiple(),
							"Only non multiple primitives are allowed in entity '{0}'", name);
			if (dtField.getType().isId()) {
				Assertion.checkState(id == null, "Only one ID Field is allowed : {0}", name);
				id = dtField;
			}
			doRegisterDtField(dtField);
		}
		idFieldOpt = Optional.ofNullable(id);
		this.dataSpace = dataSpace;
		//-----
		Assertion.when(fragment.isPresent())
				.check(() -> DtStereotype.Fragment == stereotype, "Error on {0} with sterotype {1}, If an object is a fragment then it must have this stereotype", name, stereotype);
		//Persistent => ID
		Assertion.when(stereotype.isPersistent())
				.check(idFieldOpt::isPresent, "Error on {0}, If an object is persistent then it must have an ID", name);
		Assertion.when(!stereotype.isPersistent())
				.check(() -> !idFieldOpt.isPresent(), "Error on {0}, If an object is not persistent then it must have no ID", name);
	}

	/**
	 * Static method factory for DtDefinitionBuilder
	 * @param name the name of the dtDefinition
	 * @return DtDefinitionBuilder
	 */
	public static DtDefinitionBuilder builder(final String name) {
		return new DtDefinitionBuilder(name);
	}

	//TODO A fermer
	void registerDtField(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!dtField.getType().isId(), "interdit d'ajouter les champs ID ");
		//-----
		doRegisterDtField(dtField);
	}

	private void doRegisterDtField(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		Assertion.checkArgument(!mappedFields.containsKey(dtField.getName()), "Field {0} déjà enregistré sur {1}", dtField.getName(), this);
		//-----
		fields.add(dtField);
		mappedFields.put(dtField.getName(), dtField);
	}

	public Optional<DtDefinition> getFragment() {
		return fragmentOpt.map(DefinitionReference::get);
	}

	/**
	 * @return Stereotype du Dt
	 */
	public DtStereotype getStereotype() {
		return stereotype;
	}

	/**
	 * @return Nom canonique (i.e. avec le package) de la classe d'implémentation du DtObject
	 */
	public String getClassCanonicalName() {
		return getPackageName() + '.' + getClassSimpleName();
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return getLocalName();
	}

	/**
	 * @return the name of the package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public DtField getField(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		final DtField dtField = mappedFields.get(fieldName);
		//-----
		Assertion.checkNotNull(dtField, "field '{0}' not found on '{1}'. Available fields are :{2}", fieldName, this, mappedFields.keySet());
		return dtField;
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public DtField getField(final DtFieldName fieldName) {
		return getField(fieldName.name());
	}

	/**
	 * @param fieldName FieldName
	 * @return if this field exists in this DtDefinition
	 */
	public boolean contains(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		return mappedFields.containsKey(fieldName);
	}

	/**
	 * @return Collection des champs.
	 */
	public List<DtField> getFields() {
		return Collections.unmodifiableList(fields);
	}

	/**
	 * @return Champ identifiant l'identifiant
	 */
	public Optional<DtField> getIdField() {
		return idFieldOpt;
	}

	/**
	 * Gestion de la persistance.
	 * @return Si la définition est persistée.
	 */
	public boolean isPersistent() {
		return stereotype.isPersistent();
	}

	/**
	 * @return Nom de la définition sans prefix (XxxYyyy).
	 */
	public String getLocalName() {
		return DefinitionUtil.getLocalName(name, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Champ représentant l'affichage
	 */
	public Optional<DtField> getDisplayField() {
		return displayFieldOpt;
	}

	/**
	 * @return Champ représentant le tri
	 */
	public Optional<DtField> getSortField() {
		return sortFieldOpt;
	}

	/**
	 * @return Champ représentant le handle
	 */
	public Optional<DtField> getHandleField() {
		return handleFieldOpt;
	}

	/**
	 * @return the dataSpace
	 */
	public String getDataSpace() {
		return dataSpace;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
