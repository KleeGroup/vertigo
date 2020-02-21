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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

/**
 *
 * @author pchretien
 */
final class OOMTag {
	private final OOMTag parent;
	private final OOMObject currentOOM;
	private final OOMObject parentOOM;

	private OOMTag(final OOMTag parent, final OOMObject parentOOM, final OOMObject currentOOM) {
		this.parent = parent;
		this.currentOOM = currentOOM;
		this.parentOOM = parentOOM;
	}

	OOMTag getParent() {
		return parent;
	}

	OOMObject getCurrentOOM() {
		return currentOOM;
	}

	OOMObject getParentOOM() {
		return parentOOM;
	}

	//Le parent change et il y a un Tag courant
	OOMTag createTag(final OOMObject newCurrentOOM) {
		return new OOMTag(this, newCurrentOOM, newCurrentOOM);
	}

	//Le parent ne change pas et il n'y a plus de courant
	OOMTag createTag() {
		return new OOMTag(this, this.parentOOM, null);
	}

	static OOMTag createRootTag(final OOMObject root) {
		return new OOMTag(null, root, null);
	}
}
