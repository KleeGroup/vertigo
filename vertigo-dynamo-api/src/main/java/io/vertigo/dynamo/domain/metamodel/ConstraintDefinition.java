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

import java.lang.reflect.Constructor;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.locale.MessageText;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Par nature une contrainte est une ressource partagée et non modifiable.
 *
 * @author pchretien
 */
@DefinitionPrefix("Ck")
public final class ConstraintDefinition implements Constraint, Definition {
	/**
	 * Nom de la contrainte.
	 * On n'utilise pas les génériques car problémes.
	 */
	private final String name;

	/**
	 * Message d'erreur surchargé.
	 */
	private final MessageText msg;

	private final Constraint constraint;

	/**
	 * Constructor
	 * @param name the name of the constraint
	 * @param constraintClassName the class for checking the constraint
	 * @param msg the message in case of error
	 * @param args the args to configure the constraint checker
	 */
	public ConstraintDefinition(final String name, final String constraintClassName, final String msg, final String args) {
		Assertion.checkArgNotEmpty(constraintClassName);
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
		this.msg = msg == null ? null : MessageText.of(msg);
		//-----
		constraint = createConstraint(constraintClassName, args);
	}

	private static Constraint createConstraint(final String constraintClassName, final String args) {
		final Class<? extends Constraint> constraintClass = ClassUtil.classForName(constraintClassName, Constraint.class);
		final Constructor<? extends Constraint> constructor = ClassUtil.findConstructor(constraintClass, new Class[] { String.class });
		return ClassUtil.newInstance(constructor, new Object[] { args });
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return Message d'erreur (Nullable)
	 */
	@Override
	public MessageText getErrorMessage() {
		return msg != null ? msg : constraint.getErrorMessage();
	}

	/** {@inheritDoc} */
	@Override
	public Property getProperty() {
		return constraint.getProperty();
	}

	/** {@inheritDoc} */
	@Override
	public Object getPropertyValue() {
		return constraint.getPropertyValue();
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final Object value) {
		return constraint.checkConstraint(value);
	}
}
