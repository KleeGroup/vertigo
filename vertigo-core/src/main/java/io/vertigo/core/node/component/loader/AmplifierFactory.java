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
package io.vertigo.core.node.component.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.amplifier.ProxyMethod;
import io.vertigo.core.node.component.amplifier.ProxyMethodAnnotation;
import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.component.aop.AspectMethodInvocation;

final class AmplifierFactory {

	private AmplifierFactory() {
		//private
	}

	static <C extends Amplifier> C createAmplifier(
			final Class<C> intf,
			final List<ProxyMethod> proxyMethods,
			final Map<Method, List<Aspect>> joinPoints) {
		Assertion.check()
				.isNotNull(intf)
				.isTrue(intf.isInterface(), "only interface can be amplified")
				.isNotNull(proxyMethods)
				.isNotNull(joinPoints);
		//---
		final InvocationHandler handler = new MyInvocationHandler(intf, proxyMethods, joinPoints);
		return (C) java.lang.reflect.Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class[] { intf },
				handler);
	}

	private static final class MyInvocationHandler implements InvocationHandler {
		private final Map<Method, ProxyMethod> proxyMethodsByMethod;
		private final Map<Method, List<Aspect>> aspectsByMethod;

		MyInvocationHandler(
				final Class<? extends CoreComponent> intf,
				final List<ProxyMethod> proxyMethods,
				final Map<Method, List<Aspect>> aspectsByMethod) {
			Assertion.check()
					.isNotNull(proxyMethods);
			//---
			proxyMethodsByMethod = Arrays.stream(intf.getDeclaredMethods())
					.collect(Collectors.toMap(Function.identity(),
							method -> findProxyMethod(method, proxyMethods)));
			this.aspectsByMethod = aspectsByMethod;
		}

		@Override
		public Object invoke(
				final Object instance,
				final Method method,
				final Object[] args) throws Throwable {
			Assertion.check()
					.isNotNull(instance)
					.isNotNull(method);
			//---
			return new MyMethodInvocation(method,
					aspectsByMethod.getOrDefault(method, Collections.emptyList()),
					proxyMethodsByMethod.get(method))
							.proceed(args);
		}
	}

	private static ProxyMethod findProxyMethod(
			final Method method,
			final List<ProxyMethod> proxyMethods) {
		final Annotation annotation = Arrays.stream(method.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(ProxyMethodAnnotation.class))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No way to find a proxy annotaion on method : " + method));

		return proxyMethods
				.stream()
				.filter(p -> annotation.annotationType().equals(p.getAnnotationType()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No way to find a proxy annotation on method : " + method));
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> aspects;
		private final Method method;
		private int index;
		private final ProxyMethod proxyMethod;

		private MyMethodInvocation(
				final Method method,
				final List<Aspect> aspects,
				final ProxyMethod proxyMethod) {
			Assertion.check()
					.isNotNull(method)
					.isNotNull(aspects)
					.isNotNull(proxyMethod);
			//-----
			this.method = method;
			this.aspects = aspects;
			this.proxyMethod = proxyMethod;
		}

		/** {@inheritDoc} */
		@Override
		public Object proceed(final Object[] args) {
			if (index < aspects.size()) {
				return aspects.get(index++).invoke(args, this);
			}
			return proxyMethod.invoke(method, args);
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}
}
