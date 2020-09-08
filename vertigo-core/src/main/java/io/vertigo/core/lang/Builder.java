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
package io.vertigo.core.lang;

/**
 * The Builder interface allows you to build an object step by step using a fluent style.
 *
 * It's better to build an immutable object to be able to share your object without using synchronized.
 *
 * @author pchretien
 * @param <T> type of the object to build
 */
public interface Builder<T> {
	/**
	 * @return the built object
	 */
	T build();
}
