/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.definition;

import java.util.regex.Pattern;

/**
 * This interface defines a Definition.
 *
 * Each element that defines a part of the business (or tech.) model is a definition.
 *
 * A definition 
 *  - has a unique name, starting with a specific prefix
 *  - is immutable
 *  - is not serializable.
 *  - is loaded at the boot.
 *
 * @author  pchretien
 */
public interface Definition {
	/**
	 * A definition must have a unique name, which matches the following patterns : 
	 * PrefixAaaaBbbb123
	 * or
	 * PrefixAaaaBbbb123$abcAbc123
	 */
	Pattern REGEX_DEFINITION_NAME = Pattern.compile("[A-Z][a-zA-Z0-9]{2,60}([$][a-z][a-zA-Z0-9]{2,60})?");

	/**
	 * @return The name of the definition
	 */
	String getName();

	/** 
	 * @return The short name of the definition without prefix
	 */
	String getLocalName();
}
