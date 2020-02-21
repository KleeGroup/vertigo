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
package io.vertigo.core.component.aop;

import java.lang.reflect.Method;

/**
 * This class allows you to define the behavior of an aspect on a specific method.
 *
 * You can add some code before running the method, or after.
 * You can catch some exceptions and log them...
 * You can ever change the result of a method.
 *
 * @author pchretien
 */
public interface AspectMethodInvocation {
	/**
	 * @return method concerned by the advice
	 */
	Method getMethod();

	/**
	 *
	 * @param args Args
	 * @return the result of the method.
	 */
	Object proceed(Object[] args);
}
