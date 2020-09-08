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
package io.vertigo.core.node.component.di.data;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.param.ParamValue;

public final class E {
	private final List<P2> p2Plugins;
	private final Optional<A> a;

	@Inject
	private P3 p3;

	@Inject
	private Optional<B> b;
	@Inject
	private List<P> pPlugins;

	@Inject
	public E(final Optional<A> a, final @ParamValue("pen") List<P2> p2Plugins) {
		this.a = a;
		this.p2Plugins = p2Plugins;
	}

	public Optional<A> getA() {
		return a;
	}

	public Optional<B> getB() {
		return b;
	}

	public List<P> getPPlugins() {
		return pPlugins;
	}

	public List<P2> getP2Plugins() {
		return p2Plugins;
	}

	public P3 getP3() {
		return p3;
	}
}
