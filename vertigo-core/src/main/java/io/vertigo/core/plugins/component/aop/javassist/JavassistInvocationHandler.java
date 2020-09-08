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
package io.vertigo.core.plugins.component.aop.javassist;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.component.aop.AspectMethodInvocation;
import io.vertigo.core.util.ClassUtil;
import javassist.util.proxy.MethodHandler;

/**
 * @author pchretien
 */
final class JavassistInvocationHandler implements MethodHandler {
	private final Object instance;

	private final Map<Method, List<Aspect>> joinPoints;

	JavassistInvocationHandler(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(joinPoints);
		//-----
		this.instance = instance;
		this.joinPoints = joinPoints;
	}

	Object getInstance() {
		return instance;
	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(final Object proxy, final Method thisMethod, final Method proceed, final Object[] args) {
		if (!joinPoints.containsKey(thisMethod)) {
			//Si pas d'intercepteur sur la méthode.
			return ClassUtil.invoke(instance, thisMethod, args);
		}
		return new MyMethodInvocation(instance, thisMethod, joinPoints.get(thisMethod)).proceed(args);
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> aspects;
		private final Object instance;
		private final Method method;
		private int index;

		private MyMethodInvocation(final Object instance, final Method method, final List<Aspect> aspects) {
			Assertion.check()
					.isNotNull(instance)
					.isNotNull(method)
					.isNotNull(aspects);
			//-----
			this.instance = instance;
			this.method = method;
			this.aspects = aspects;
		}

		/** {@inheritDoc} */
		@Override
		public Object proceed(final Object[] args) {
			if (index < aspects.size()) {
				return aspects.get(index++).invoke(args, this);
			}
			return ClassUtil.invoke(instance, method, args);
		}

		/** {@inheritDoc} */
		@Override
		public Method getMethod() {
			return method;
		}
	}
}
