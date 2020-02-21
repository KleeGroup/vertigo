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
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Par nature un formatter est une ressource partagée et non modifiable.
 *
 * @author pchretien
 */
@DefinitionPrefix("Fmt")
public final class FormatterDefinition implements Formatter, Definition {
	/**
	* Nom de la contrainte.
	* On n'utilise pas les génériques car problémes.
	*/
	private final String name;

	private final Formatter formatter;

	/**
	 * Constructor.
	 * @param name the name of the formatter
	 * @param formatterClassName the class to be used for formatting the value
	 * @param args args to configure the formatter
	 */
	public FormatterDefinition(final String name, final String formatterClassName, final String args) {
		Assertion.checkArgNotEmpty(formatterClassName);
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
		//-----
		formatter = createFormatter(formatterClassName, args);
	}

	private static Formatter createFormatter(final String formatterClassName, final String args) {
		final Class<? extends Formatter> formatterClass = ClassUtil.classForName(formatterClassName, Formatter.class);
		final Constructor<? extends Formatter> constructor = ClassUtil.findConstructor(formatterClass, new Class[] { String.class });
		return ClassUtil.newInstance(constructor, new Object[] { args });
	}

	public String getFormatterClassName() {
		return formatter.getClass().getName();
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

	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		return formatter.valueToString(objValue, dataType);
	}

	@Override
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		return formatter.stringToValue(strValue, dataType);
	}

}
