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

import java.util.Collections;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.Domain.Scope;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class must be used to build a Domain.
 * @author pchretien
 */
public final class DomainBuilder implements Builder<Domain> {
	private final String myName;
	private final Domain.Scope myScope;
	private final boolean myMultiple;

	private final DataType myDataType;
	private final String myDtDefinitionName;
	private final Class myValueObjectClass;

	/** Formatter. */
	private FormatterDefinition myformatterDefinition;

	/** list of constraints */
	private List<ConstraintDefinition> myConstraintDefinitions;

	/** List of property-value tuples */
	private Properties myProperties;

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param dataType the dataType of the domain
	 */
	DomainBuilder(final String name, final DataType dataType, final boolean multiple) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dataType);
		//---
		myName = name;
		myScope = Domain.Scope.PRIMITIVE;
		myMultiple = multiple;

		myDataType = dataType;
		myDtDefinitionName = null;
		myValueObjectClass = null;
	}

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param dtDefinitionName the data-object definition of the domain
	 */
	DomainBuilder(final String name, final String dtDefinitionName, final boolean multiple) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dtDefinitionName);
		//---
		myName = name;
		myScope = Scope.DATA_OBJECT;
		myMultiple = multiple;

		myDataType = null;
		myDtDefinitionName = dtDefinitionName;
		myValueObjectClass = null;
	}

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param valueObjectClass the value-object class of the domain
	 */
	DomainBuilder(final String name, final Class valueObjectClass, final boolean multiple) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(valueObjectClass);
		//---
		myName = name;
		myScope = Domain.Scope.VALUE_OBJECT;
		myMultiple = multiple;

		myDataType = null;
		myDtDefinitionName = null;
		myValueObjectClass = valueObjectClass;
	}

	/**
	 * @param formatterDefinition the FormatterDefinition
	 * @return this builder
	 */
	public DomainBuilder withFormatter(final FormatterDefinition formatterDefinition) {
		Assertion.checkNotNull(formatterDefinition);
		//---
		myformatterDefinition = formatterDefinition;
		return this;
	}

	/**
	 * @param constraintDefinitions the list of constraintDefinitions
	 * @return this builder
	 */
	public DomainBuilder withConstraints(final List<ConstraintDefinition> constraintDefinitions) {
		Assertion.checkNotNull(constraintDefinitions);
		//---
		myConstraintDefinitions = constraintDefinitions;
		return this;
	}

	/**
	* @param properties the properties
	* @return this builder
	*/
	public DomainBuilder withProperties(final Properties properties) {
		Assertion.checkNotNull(properties);
		//---
		myProperties = properties;
		return this;
	}

	@Override
	public Domain build() {
		return new Domain(
				myName,
				myScope,
				myMultiple,
				myDataType,
				myDtDefinitionName,
				myValueObjectClass,
				myformatterDefinition,
				myConstraintDefinitions == null ? Collections.emptyList() : myConstraintDefinitions,
				myProperties == null ? Properties.builder().build() : myProperties);
	}
}
