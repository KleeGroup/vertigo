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

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;

public final class F {
	@Inject
	@ParamValue("a")
	private Object a;

	@Inject
	@ParamValue("param2")
	private String param2;

	private final String param1;
	private final Optional<String> param3;
	private final Optional<String> param4;

	@Inject
	public F(
			@ParamValue("param1") final String param1,
			@ParamValue("param3") final Optional<String> param3,
			@ParamValue("param4") final Optional<String> param4) {
		Assertion.check()
				.isNotNull(param1)
				.isNotNull(param3)
				.isNotNull(param4);
		//-----
		this.param1 = param1;
		this.param3 = param3;
		this.param4 = param4;
	}

	public A getA() {
		return (A) a;
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public Optional<String> getParam3() {
		return param3;
	}

	public Optional<String> getParam4() {
		return param4;
	}
}
