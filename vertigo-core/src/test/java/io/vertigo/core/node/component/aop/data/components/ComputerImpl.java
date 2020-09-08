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
package io.vertigo.core.node.component.aop.data.components;

import io.vertigo.core.node.component.aop.data.aspects.OneMore;
import io.vertigo.core.node.component.aop.data.aspects.TenMore;

/**
 * @author prahmoune
 */
public class ComputerImpl implements Computer {

	/**
	 * On ajoute un Modifier AOP pour fausser le calcul !
	 */
	@Override
	@OneMore
	public int sum(final int i, final int j) {
		return i + j;
	}

	@Override
	@TenMore
	@OneMore
	public int multi(final int i, final int j) {
		return i * j;
	}

	@Override
	public int no(final int i) {
		return i;
	}
}
