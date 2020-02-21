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
import java.util.List;
import java.util.Optional;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.util.StringUtil;

/**
 * This class must be used to build a DtDefinition.
 *
 * Each dtDefinition must have a name following this pattern DT_XXX_YYYY
 *
 * @author pchretien
 */
public final class DtDefinitionBuilder implements Builder<DtDefinition> {

	private static class MessageKeyImpl implements MessageKey {
		private static final long serialVersionUID = 6959551752755175151L;

		private final String name;

		MessageKeyImpl(final String name) {
			this.name = name;
		}

		/** {@inheritDoc} */
		@Override
		public String name() {
			return name;
		}
	}

	private DtDefinition dtDefinition;
	private final String myName;
	private DefinitionReference<DtDefinition> myFragmentRef;
	private String myPackageName;
	private DtStereotype myStereotype;
	private DtField myIdField;
	private final List<DtField> myFields = new ArrayList<>();
	private String myDataSpace;
	private String mySortFieldName;
	private String myDisplayFieldName;
	private String myHandleFieldName;

	/**
	 * Constructor.
	 * @param name the name of the dtDefinition
	 */
	DtDefinitionBuilder(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
	}

	/**
	 * Sets packageName
	 * @param packageName the name of the package (nullable)
	 * @return this builder
	 */
	public DtDefinitionBuilder withPackageName(final String packageName) {
		//the packageName can be null
		//-----
		myPackageName = packageName;
		return this;
	}

	/**
	 * Sets fragment
	 * @param fragment Persistent root DtDefinition for this fragment
	 * @return this builder
	 */
	public DtDefinitionBuilder withFragment(final DtDefinition fragment) {
		Assertion.checkNotNull(fragment);
		//---
		myStereotype = DtStereotype.Fragment;
		myFragmentRef = new DefinitionReference<>(fragment);
		return this;
	}

	/**
	 * Sets the stereotype of the dtDefinition.
	 *
	 * @param stereotype the stereotype of the dtDefinition
	 * @return this builder
	 */
	public DtDefinitionBuilder withStereoType(final DtStereotype stereotype) {
		Assertion.checkNotNull(stereotype);
		//-----
		myStereotype = stereotype;
		return this;
	}

	/**
	 * Adds a field linked to another dtDefinition (aka foreign key).
	 *
	 * @param fieldName the name of the field
	 * @param fkDtDefinitionName the name of the linked definition
	 * @param label the label of the field
	 * @param domain the domain of the field
	 * @param required if the field is required
	 * @return this builder
	 */
	public DtDefinitionBuilder addForeignKey(
			final String fieldName,
			final String label,
			final Domain domain,
			final boolean required,
			final String fkDtDefinitionName) {
		//Pour l'instant on ne gère pas les chamsp computed dynamiques
		final boolean persistent = true;
		final DtField dtField = createField(
				fieldName,
				DtField.FieldType.FOREIGN_KEY,
				domain,
				label,
				required,
				persistent,
				fkDtDefinitionName,
				null);
		//On suppose que le build est déjà effectué.
		dtDefinition.registerDtField(dtField);
		return this;
	}

	/**
	 * Adds a computed field.
	 *
	 * @param fieldName the name of the field
	 * @param label the label of the field
	 * @param domain the domain of the field
	 * @param computedExpression the expression use to compute the field
	 * @return this builder
	 */
	public DtDefinitionBuilder addComputedField(
			final String fieldName,
			final String label,
			final Domain domain,
			final ComputedExpression computedExpression) {
		final boolean persistent = false;
		final DtField dtField = createField(
				fieldName,
				DtField.FieldType.COMPUTED,
				domain,
				label,
				false,
				persistent,
				null,
				computedExpression);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Adds a common data field.
	 *
	 * @param fieldName the name of the field
	 * @param domain the domain of the field
	 * @param label the label of the field
	 * @param required if the field is required
	 * @param persistent if the fiels is persistent
	 * @return this builder
	 */
	public DtDefinitionBuilder addDataField(
			final String fieldName,
			final String label,
			final Domain domain,
			final boolean required,
			final boolean persistent) {
		//the field is dynamic if and only if the dtDefinition is dynamic
		final DtField dtField = createField(
				fieldName,
				DtField.FieldType.DATA,
				domain,
				label,
				required,
				persistent,
				null,
				null);
		myFields.add(dtField);
		return this;
	}

	/**
	 * Adds an ID field.
	 * This field is required.
	 *
	 * @param fieldName the name of the field
	 * @param domain the domain of the field
	 * @param label the label of the field
	 * @return this builder
	 */
	public DtDefinitionBuilder addIdField(
			final String fieldName,
			final String label,
			final Domain domain) {
		Assertion.checkArgument(myIdField == null, "only one ID per Entity is permitted, error on {0}", myPackageName);
		//---
		//le champ ID est tjrs required
		final boolean required = true;
		//le champ ID est persistant SSI la définition est persitante.
		final boolean persistent = true;
		//le champ  est dynamic SSI la définition est dynamique
		final DtField dtField = createField(
				fieldName,
				DtField.FieldType.ID,
				domain,
				label,
				required,
				persistent,
				null,
				null);
		myIdField = dtField;
		myFields.add(dtField);
		return this;
	}

	private DtField createField(
			final String fieldName,
			final DtField.FieldType type,
			final Domain domain,
			final String strLabel,
			final boolean required,
			final boolean persistent,
			final String fkDtDefinitionName,
			final ComputedExpression computedExpression) {

		final String shortName = DefinitionUtil.getLocalName(myName, DtDefinition.class);
		//-----
		// Le DtField vérifie ses propres règles et gère ses propres optimisations
		final String id = DtField.PREFIX + shortName + '$' + fieldName;

		//2. Sinon Indication de longueur portée par le champ du DT.
		//-----
		final MessageText labelMsg = MessageText.ofDefaultMsg(strLabel, new MessageKeyImpl(id));
		// Champ CODE_COMMUNE >> getCodeCommune()
		//Un champ est persisanty s'il est marqué comme tel et si la définition l'est aussi.
		return new DtField(
				id,
				fieldName,
				type,
				domain,
				labelMsg,
				required,
				persistent,
				fkDtDefinitionName,
				computedExpression);
	}

	/**
	 * Sets the dataSpace to which the dtDefinition belongs.
	 * @param dataSpace the dataSpace to which the DtDefinition is mapped.
	 * @return this builder
	 */
	public DtDefinitionBuilder withDataSpace(final String dataSpace) {
		//the dataSpace can be null, in this case the default dataSpace will be chosen.
		//-----
		myDataSpace = dataSpace;
		return this;
	}

	/**
	 * Specifies which field to be used for sorting
	 * @param sortFieldName fieldName to use
	 * @return this builder
	 */
	public DtDefinitionBuilder withSortField(final String sortFieldName) {
		mySortFieldName = sortFieldName;
		return this;
	}

	/**
	 * Specifies which field to be used for display
	 * @param displayFieldName fieldName to use
	 * @return this builder
	 */
	public DtDefinitionBuilder withDisplayField(final String displayFieldName) {
		myDisplayFieldName = displayFieldName;
		return this;
	}

	/**
	 * Specifies which field to be used for handle
	 * @param handleFieldName fieldName to use
	 * @return this builder
	 */
	public DtDefinitionBuilder withHandleField(final String handleFieldName) {
		myHandleFieldName = handleFieldName;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition build() {
		Assertion.checkState(dtDefinition == null, "build() already executed");
		//-----
		if (myStereotype == null) {
			myStereotype = myIdField == null ? DtStereotype.ValueObject : DtStereotype.Entity;
		}

		final DtField sortField;
		if (mySortFieldName != null) {
			sortField = findFieldByName(mySortFieldName)
					.orElseThrow(() -> new IllegalStateException(StringUtil.format("Sort field '{0}' not found on '{1}'", mySortFieldName, dtDefinition.getName())));
		} else if (myStereotype == DtStereotype.Fragment) {
			sortField = myFragmentRef.get().getSortField().orElse(null);
		} else {
			sortField = null;
		}

		final DtField displayField;
		if (myDisplayFieldName != null) {
			displayField = findFieldByName(myDisplayFieldName)
					.orElseThrow(() -> new IllegalStateException(StringUtil.format("Display field '{0}' not found on '{1}'", myDisplayFieldName, dtDefinition.getName())));
		} else if (myStereotype == DtStereotype.Fragment) {
			displayField = myFragmentRef.get().getDisplayField().orElse(null);
		} else {
			displayField = null;
		}

		final DtField handleField;
		if (myHandleFieldName != null) {
			handleField = findFieldByName(myHandleFieldName)
					.orElseThrow(() -> new IllegalStateException(StringUtil.format("Handle field '{0}' not found on '{1}'", myHandleFieldName, dtDefinition.getName())));
		} else if (myStereotype == DtStereotype.Fragment) {
			handleField = myFragmentRef.get().getHandleField().orElse(null);
		} else {
			handleField = null;
		}

		dtDefinition = new DtDefinition(
				myName,
				Optional.ofNullable(myFragmentRef),
				myPackageName,
				myStereotype,
				myFields,
				myDataSpace == null ? StoreManager.MAIN_DATA_SPACE_NAME : myDataSpace,
				Optional.ofNullable(sortField),
				Optional.ofNullable(displayField),
				Optional.ofNullable(handleField));
		return dtDefinition;
	}

	private Optional<DtField> findFieldByName(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		return myFields
				.stream()
				.filter(dtField -> fieldName.equals(dtField.getName()))
				.findFirst();
	}

}
