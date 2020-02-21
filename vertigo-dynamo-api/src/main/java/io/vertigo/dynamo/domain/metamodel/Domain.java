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

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.app.Home;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * A domain exists to enrich the primitive datatypes, giving them super powers.
 *
 * A domain has
 *  - a validator (executed by a list of constraints)
 *  - a formatter
 *
 * A domain is a shared object ; by nature it is immutable.
 *
 * A domain is a definition, its prefix is "DO_"
 *
 * Examples :
 *  A mail is not defined by a simple "String", but by a domain called 'Mail'.
 *  Weights, currencies, codes, labels...
 *
 *  An application is built with some dozens of domains.
 *
 * @author pchretien
 */
@DefinitionPrefix("Do")
public final class Domain implements Definition {
	public enum Scope {
		PRIMITIVE,
		VALUE_OBJECT,
		DATA_OBJECT;
		/**
		 * @return if the domain is a primitive type
		 */
		public boolean isPrimitive() {
			return this == Scope.PRIMITIVE;
		}

		/**
		 * @return if the domain is a value-object
		 */
		public boolean isValueObject() {
			return this == Scope.VALUE_OBJECT;
		}

		/**
		 * @return if the domain is a data-object
		 */
		public boolean isDataObject() {
			return this == Scope.DATA_OBJECT;
		}
	}

	private final String name;
	private final Scope scope;
	private final boolean multiple;
	private final DataType dataType;

	private final Class valueObjectClass;
	/**
	 * Name of the DtDefinition (for the DtObject or DtList)
	 */
	private final String dtDefinitionName;

	/** Formatter. */
	private final DefinitionReference<FormatterDefinition> formatterDefinitionRef;

	/** Validator composed by a list of constraints. */
	private final List<DefinitionReference<ConstraintDefinition>> constraintDefinitionRefs;

	/** List of property-value tuples */
	private final Properties properties;

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param scope the scope of the domain
	 * @param dataType the type of the domain
	 * @param formatterDefinition the formatter
	 * @param constraintDefinitions the list of constraints
	 * @param properties List of property-value tuples
	 */
	Domain(
			final String name,
			final Scope scope,
			final boolean multiple,
			final DataType dataType,
			final String dtDefinitionName,
			final Class valueObjectClass,
			final FormatterDefinition formatterDefinition,
			final List<ConstraintDefinition> constraintDefinitions,
			final Properties properties) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(scope);
		//---
		Assertion.when(scope == Scope.PRIMITIVE).check(() -> dataType != null, "a primitive domain must define a primitive type");
		Assertion.when(scope == Scope.PRIMITIVE).check(() -> dtDefinitionName == null && valueObjectClass == null, "a primitive domain can't have nor a data-object-definition nor a value-object class");
		//---
		Assertion.when(scope == Scope.DATA_OBJECT).check(() -> dtDefinitionName != null, "a data-object domain must define a data-object definition");
		Assertion.when(scope == Scope.DATA_OBJECT).check(() -> dataType == null && valueObjectClass == null, "a data-object domain can't have nor a primitive type nor a value-object class");
		//---
		Assertion.when(scope == Scope.VALUE_OBJECT).check(() -> valueObjectClass != null, "a value-object domain must define a value-object class");
		Assertion.when(scope == Scope.VALUE_OBJECT).check(() -> dataType == null && dtDefinitionName == null, "a value-object domain can't have nor a primitive type nor a data-object-definition");
		//formatterDefinition is nullable
		Assertion.checkNotNull(constraintDefinitions);
		Assertion.checkNotNull(properties);
		//-----
		this.name = name;
		this.scope = scope;
		this.multiple = multiple;
		//--- Primitive
		this.dataType = dataType;
		//---data-object
		this.dtDefinitionName = dtDefinitionName;
		//---
		this.valueObjectClass = valueObjectClass;
		//---
		formatterDefinitionRef = formatterDefinition == null ? null : new DefinitionReference<>(formatterDefinition);
		//---Constraints
		constraintDefinitionRefs = buildConstraintDefinitionRefs(constraintDefinitions);
		//---Properties
		this.properties = buildProperties(constraintDefinitions, properties);
	}

	/**
	 * Static method factory for DomainBuilder
	 * @param name the name of the domain
	 * @param dataType the dataType managed by the domain
	 * @param multiple if the domain is a list
	 * @return DomainBuilder
	 */
	public static DomainBuilder builder(final String name, final DataType dataType, final boolean multiple) {
		return new DomainBuilder(name, dataType, multiple);
	}

	/**
	 * Static method factory for DomainBuilder
	 * @param name the name of the domain
	 * @param dataType the dataType managed by the domain
	 * @return DomainBuilder
	 */
	public static DomainBuilder builder(final String name, final DataType dataType) {
		return new DomainBuilder(name, dataType, false);
	}

	/**
	 * Static method factory for DomainBuilder
	 * @param name the name of the domain
	 * @param dtDefinitionName the definition managed by the domain
	 * @param multiple if the domain is a list
	 * @return DomainBuilder
	 */
	public static DomainBuilder builder(final String name, final String dtDefinitionName, final boolean multiple) {
		return new DomainBuilder(name, dtDefinitionName, multiple);
	}

	private static List<DefinitionReference<ConstraintDefinition>> buildConstraintDefinitionRefs(final List<ConstraintDefinition> constraintDefinitions) {
		return constraintDefinitions
				.stream()
				.map(DefinitionReference::new)
				.collect(Collectors.toList());
	}

	private static Properties buildProperties(final List<ConstraintDefinition> constraintDefinitions, final Properties inputProperties) {
		final PropertiesBuilder propertiesBuilder = Properties.builder();
		for (final Property property : inputProperties.getProperties()) {
			propertiesBuilder.addValue(property, inputProperties.getValue(property));
		}

		//Properties are inferred from constraints
		for (final ConstraintDefinition constraintDefinition : constraintDefinitions) {
			propertiesBuilder.addValue(constraintDefinition.getProperty(), constraintDefinition.getPropertyValue());
		}
		return propertiesBuilder.build();
	}

	/**
	 * @return if the domain is a list of objects
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * Returns the dataType of the domain.
	 *
	 * @return the dataType.
	 */
	public DataType getDataType() {
		Assertion.checkState(scope == Scope.PRIMITIVE, "can only be used with primitives");
		//---
		return dataType;
	}

	/**
	 * Returns the formatter of the domain.
	 *
	 * @return the formatter.
	 */
	private FormatterDefinition getFormatter() {
		Assertion.checkNotNull(formatterDefinitionRef, "no formatter defined on {0}", this);
		//-----
		return formatterDefinitionRef.get();
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Chechs if the value is valid.
	 *
	 * @param value the value to check
	 */
	public void checkValue(final Object value) {
		if (getScope().isPrimitive()) {
			if (isMultiple()) {
				if (!(value instanceof List)) {
					throw new ClassCastException("Value " + value + " must be a list");
				}
				List.class.cast(value).forEach(element -> dataType.checkValue(element));
			} else {
				dataType.checkValue(value);
			}
		}
	}

	/**
	 * Chechs if
	 *  - the value is valid
	 *  - the constraints are ok.
	 *
	 * @param value the value to check
	 * @throws ConstraintException if a constraint has failed
	 */
	public void checkConstraints(final Object value) throws ConstraintException {
		checkValue(value);
		//---
		for (final DefinitionReference<ConstraintDefinition> constraintDefinitionRef : constraintDefinitionRefs) {
			//when a constraint fails, there is no validation
			if (!constraintDefinitionRef.get().checkConstraint(value)) {
				throw new ConstraintException(constraintDefinitionRef.get().getErrorMessage());
			}
		}
	}

	//==========================================================================
	//for these domains : DtList or DtObject
	//==========================================================================
	/**
	 * @return the dtDefinition for the domains DtList or DtObject.
	 */
	public DtDefinition getDtDefinition() {
		Assertion.checkState(scope == Scope.DATA_OBJECT, "can only be used with data-objects");
		//---
		return Home.getApp().getDefinitionSpace().resolve(dtDefinitionName, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the domain scope
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Gets the type managed by this domain.
	 * Warn : if the domain is a list, the return type is the one inside the list.
	 *
	 * Example :
	 *	Integer => Integer
	 * 	List<Integer> => Integer
	 * 	Car => Car
	 * 	DtList<Car> => Car
	 * @return the class of the object
	 */
	public Class getJavaClass() {
		switch (scope) {
			case PRIMITIVE:
				return dataType.getJavaClass();
			case DATA_OBJECT:
				return ClassUtil.classForName(getDtDefinition().getClassCanonicalName());
			case VALUE_OBJECT:
				return valueObjectClass;
			default:
				throw new IllegalStateException();
		}
	}

	public Class getTargetJavaClass() {
		if (isMultiple()) {
			switch (scope) {
				case PRIMITIVE:
					return List.class;
				case DATA_OBJECT:
					return DtList.class;
				case VALUE_OBJECT:
					return List.class;
				default:
					throw new IllegalStateException();
			}
		}
		return getJavaClass();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	public String getFormatterClassName() {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().getFormatterClassName();
	}

	public String valueToString(final Object objValue) {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().valueToString(objValue, dataType);
	}

	public Object stringToValue(final String strValue) throws FormatterException {
		Assertion.checkNotNull(dataType, "can only be used with primitives");
		//---
		return getFormatter().stringToValue(strValue, dataType);
	}

	public boolean isDtList() {
		return getScope().isDataObject() && isMultiple();
	}
}
