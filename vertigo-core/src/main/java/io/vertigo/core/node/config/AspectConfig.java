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
package io.vertigo.core.node.config;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.aop.Aspect;

/**
 * The AspectConfig class defines an aspect.
 * An aspect is comoposed of
 *  - an interception point defined by an annotation
 *  - an interceptor (advice) defined by a component
 *
 * @author pchretien
 */
public final class AspectConfig {
	private final Class<? extends Aspect> aspectClass;

	/**
	 * Constructor.
	 */
	AspectConfig(final Class<? extends Aspect> aspectClass) {
		Assertion.check().isNotNull(aspectClass);
		//-----
		this.aspectClass = aspectClass;
	}

	/**
	 * @return The implementation class of the comoponent which is responsible for the interception
	 */
	public Class<? extends Aspect> getAspectClass() {
		return aspectClass;
	}

}
