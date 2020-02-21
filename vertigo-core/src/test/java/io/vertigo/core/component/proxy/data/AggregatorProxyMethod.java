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
package io.vertigo.core.component.proxy.data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import io.vertigo.core.component.proxy.ProxyMethod;

public final class AggregatorProxyMethod implements ProxyMethod {

	@Override
	public Class<AggregatorAnnotation> getAnnotationType() {
		return AggregatorAnnotation.class;
	}

	@Override
	public Object invoke(final Method method, final Object[] args) {
		final AggregatorAnnotation aggregatorAnnotation = method.getAnnotation(AggregatorAnnotation.class);

		final Stream<Integer> stream = Arrays.stream(args)
				.map(arg -> Integer.class.cast(arg));

		switch (aggregatorAnnotation.operation()) {
			case max:
				return stream.max(Integer::compare).orElse(null);
			case min:
				return stream.min(Integer::compare).orElse(null);
			case count:
				return stream.count();
			default:
				throw new IllegalStateException();
		}
	}
}
