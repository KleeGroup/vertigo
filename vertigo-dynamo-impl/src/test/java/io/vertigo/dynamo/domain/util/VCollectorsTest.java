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
package io.vertigo.dynamo.domain.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.domain.data.domain.Artist;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

/**
 *
 * @author xdurand
 *
 */
public class VCollectorsTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.withLocales("fr_FR")
				.endBoot()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/domain/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.domain.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	/**
	 * Test du VCollectors.toDtList sur une liste vide
	 */
	@Test
	public void testCollectDtListEmpty() {
		final DtList<Artist> emptyDtList = new DtList<>(Artist.class);
		final DtList<Artist> listCollected = emptyDtList.stream().collect(VCollectors.toDtList(Artist.class));

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty());
		assertEquals(0, listCollected.size());
	}

	private static Artist createArtist(final long id, final String name) {
		final Artist m = new Artist();
		m.setId(id);
		m.setName(name);
		return m;
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide sans filtrage
	 */
	@Test
	public void testCollectDtList() {
		final Artist m1 = createArtist(1, "David Bowie");
		final Artist m2 = createArtist(2, "Joe Strummer");

		final DtList<Artist> dtList = DtList.of(m1, m2);
		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.sorted( (art1, art2) -> art1.getId().compareTo(art2.getId()))
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(2, listCollected.size());
		assertEquals(listCollected.get(0), m1);
		assertEquals(listCollected.get(1), m2);
		assertEquals(2, dtList.size());
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide avec filtrage
	 */
	@Test
	public void testFilterCollectDtList() {
		final Artist m1 = createArtist(1, "Louis Armstrong");
		final Artist m2 = createArtist(2, "Duke Ellington");
		final Artist m3 = createArtist(3, "Jimmy Hendricks");

		final DtList<Artist> dtList = DtList.of(m1, m2, m3);

		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.filter( m -> m.getId() % 2 == 0)
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on
		assertNotNull(listCollected);
		Assertions.assertFalse(listCollected.isEmpty());
		assertEquals(1, listCollected.size());
		assertEquals(listCollected.get(0), m2);
		assertEquals(3, dtList.size());
	}

}
