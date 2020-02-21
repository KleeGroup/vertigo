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
package io.vertigo.core.component.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectAnnotation;
import io.vertigo.lang.Assertion;

/**
 * Classe permettant d'injecter les intercepteurs sur les points d'exécutions définis dans les aspects.
 * La liste des intercepteurs est construite en amont.
 *
 * Les points d'interceptions sont définis par des annotations placées au niveau
 *  - des classes
 *  - des méthodes.
 *
 * @author pchretien
 */
final class ComponentAspectUtil {
	private ComponentAspectUtil() {
		//
	}

	/**
	 * create all "join points" for a component.
	 * Join points are identifed by a method
	 *
	 * @return Map des aspects par méthode
	 */
	static Map<Method, List<Aspect>> createAspectsByMethod(
			final Class<?> implClass,
			final Collection<Aspect> aspects) {
		Assertion.checkNotNull(implClass);
		Assertion.checkNotNull(aspects);
		//-----
		//1 - Annotated class
		final List<Aspect> classBasedInterceptors = Stream.of(implClass.getAnnotations())
				//we consider class based annotations  : AspectAnnotation
				.filter(annotation -> annotation.annotationType().isAnnotationPresent(AspectAnnotation.class))
				//for all this kind of AspectAnnotation we search THE corresponding aspect
				.map(annotation -> findAspect(annotation, aspects))
				.collect(Collectors.toList());

		//2 - Annotated methods
		final Map<Method, List<Aspect>> joinPoints = new HashMap<>();
		for (final Method method : implClass.getMethods()) {
			final List<Aspect> methodBasedInterceptors = Stream.of(method.getAnnotations())
					//we consider all methods annotated with AspectAnnotation
					.filter(annotation -> annotation.annotationType().isAnnotationPresent(AspectAnnotation.class))
					//for all this kind of AspectAnnotation we search THE corresponding aspect
					.map(annotation -> findAspect(annotation, aspects))
					.collect(Collectors.toList());

			if (!classBasedInterceptors.isEmpty()
					&& !Object.class.equals(method.getDeclaringClass())) {
				//we add all class based interceptors on "no-object" methods
				methodBasedInterceptors.addAll(classBasedInterceptors);
			}
			if (!methodBasedInterceptors.isEmpty()) {
				//there is at least on aspect on this method
				joinPoints.put(method, methodBasedInterceptors);
			}
		}
		return joinPoints;

	}

	private static Aspect findAspect(final Annotation annotation, final Collection<Aspect> aspects) {
		Assertion.checkNotNull(annotation);
		Assertion.checkNotNull(aspects);
		// --
		return aspects
				.stream()
				.filter(aspect -> annotation.annotationType().equals(aspect.getAnnotationType()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("An aspect may be missing : Unresolved methods join points on aspect : " + annotation));

	}

}
