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
package io.vertigo.commons.eventbus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.eventbus.data.BlueColorEvent;
import io.vertigo.commons.eventbus.data.DummyEvent;
import io.vertigo.commons.eventbus.data.MySubscriber;
import io.vertigo.commons.eventbus.data.RedColorEvent;
import io.vertigo.commons.eventbus.data.WhiteColorEvent;
import io.vertigo.commons.eventbus.data.aspects.FlipAspect;

/**
 * @author pchretien
 */
public final class EventBusManagerTest extends AbstractTestCaseJU5 {

	@Inject
	private EventBusManager eventBusManager;

	@Inject
	private MySubscriber mySubscriber;
	private int deadEvents = 0;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(ModuleConfig.builder("myAspects")
						.addAspect(FlipAspect.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(MySubscriber.class)
						.build())
				.build();
	}

	@Override
	protected void doSetUp() {
		eventBusManager.registerDead(event -> deadEvents++);
	}

	@Test
	public void testSimple() {
		assertEquals(0, mySubscriber.getBlueCount());
		assertEquals(0, mySubscriber.getRedCount());
		assertEquals(0, mySubscriber.getCount());

		eventBusManager.post(new BlueColorEvent());
		eventBusManager.post(new WhiteColorEvent());
		eventBusManager.post(new RedColorEvent());
		eventBusManager.post(new RedColorEvent());

		assertEquals(1, mySubscriber.getBlueCount());
		assertEquals(2, mySubscriber.getRedCount());
		assertEquals(4, mySubscriber.getCount());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testWithAspects() {
		/*
		 * We want to check that aspects are used.
		 */
		Assertions.assertTrue(FlipAspect.isOff());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		Assertions.assertTrue(FlipAspect.isOn());

		eventBusManager.post(new RedColorEvent()); //there is no aspect
		Assertions.assertTrue(FlipAspect.isOn());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		Assertions.assertTrue(FlipAspect.isOff());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testDeadEvent() {
		assertEquals(0, deadEvents);
		eventBusManager.post(new DummyEvent());
		assertEquals(1, deadEvents);
	}
}
