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
package io.vertigo.studio.plugins.mda.task.test;

import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;

/**
 * Représente un attribut de task.
 *
 * @author sezratty, mlaroche
 */
public final class TemplateTaskAttribute {
	private final TaskAttribute taskAttribute;
	private final String value;

	TemplateTaskAttribute(final TaskAttribute taskAttribute) {
		Assertion.checkNotNull(taskAttribute);
		//-----
		this.taskAttribute = taskAttribute;
		value = create(this.taskAttribute.getDomain(), this.taskAttribute.isRequired());
	}

	/**
	 * @return Nom de l'attribut.
	 */
	public String getName() {
		return taskAttribute.getName();
	}

	/**
	 * @return Type de la donnée en string
	 */
	public String getDataType() {
		return String.valueOf(DomainUtil.buildJavaType(taskAttribute.getDomain()));
	}

	/**
	 * @return L'expression de la valeur factice.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return Domain.
	 */
	Domain getDomain() {
		return taskAttribute.getDomain();
	}

	private static String create(final Domain domain, final boolean isRequired) {
		final String dumFunction;
		if (domain.isMultiple()) {
			if (domain.getScope().isDataObject()) {
				dumFunction = "dumDtList";
			} else {
				dumFunction = "dumList";
			}
		} else {
			dumFunction = "dum";
		}
		//we don't have generated classes right now... so we need to only we the domain info and can't use domain.getJavaClass() for this case
		final String javaClassName = domain.getScope().isDataObject() ? domain.getDtDefinition().getClassCanonicalName() : domain.getJavaClass().getCanonicalName();
		//---
		final String rawExpression = "dum()." + dumFunction + "(" + javaClassName + ".class)";
		final String expression = isRequired ? rawExpression : Optional.class.getCanonicalName() + ".ofNullable(" + rawExpression + ")";
		return expression;
	}
}
