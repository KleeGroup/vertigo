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
package io.vertigo.app.config;

import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;

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
		Assertion.checkNotNull(aspectClass);
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
