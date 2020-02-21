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
package io.vertigo.commons.eventbus.data.aspects;

import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;

/**
 * @author pchretien
 */
public final class FlipAspect implements Aspect {
	private static boolean state = false;

	@Override
	public Integer invoke(final Object[] args, final AspectMethodInvocation methodInvocation) {
		flip();
		return (Integer) methodInvocation.proceed(args);
	}

	@Override
	public Class<Flip> getAnnotationType() {
		return Flip.class;
	}

	private static synchronized void flip() {
		state = !state;

	}

	public static synchronized boolean isOn() {
		return state == true;
	}

	public static synchronized boolean isOff() {
		return state == false;
	}
}
