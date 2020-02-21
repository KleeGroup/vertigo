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
package io.vertigo.dynamo.plugins.store.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;

/**
 * Class abstraite des Stores de données static et immutable.
 * @author npiedeloup
 */
public abstract class AbstractStaticDataStorePlugin implements DataStorePlugin {

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E create(final DtDefinition dtDefinition, final E entity) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtDefinition dtDefinition, final Entity entity) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final DtDefinition dtDefinition, final UID uri) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForNNAssociation uri) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation uri) {
		throw new UnsupportedOperationException();
	}
}
