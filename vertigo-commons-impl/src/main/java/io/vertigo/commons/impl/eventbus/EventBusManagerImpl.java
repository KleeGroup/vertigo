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
package io.vertigo.commons.impl.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.commons.eventbus.Event;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.commons.eventbus.EventBusSubscriptionDefinition;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * @author pchretien, npiedeloup
 */
public final class EventBusManagerImpl implements EventBusManager, Activeable, SimpleDefinitionProvider {
	private final List<EventBusSubscriptionDefinition> subscriptions = new ArrayList<>();
	private final List<Consumer<Event>> deadEventListeners = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public EventBusManagerImpl() {
		Home.getApp().registerPreActivateFunction(this::registerAllSubscribers);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Home.getApp().getNodeConfig().getBootConfig().getAopPlugin();
		return Home.getApp().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createEventSubscriptions(id, Home.getApp().getComponentSpace().resolve(id, Component.class), aopPlugin).stream())
				.collect(Collectors.toList());
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	private static List<EventBusSubscriptionDefinition> createEventSubscriptions(final String componentId, final Component subscriberInstance, final AopPlugin aopPlugin) {
		Assertion.checkNotNull(subscriberInstance);
		//-----
		//1. search all methods
		return Stream.of(aopPlugin.unwrap(subscriberInstance).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(EventBusSubscribed.class))
				.map(method -> {
					Assertion.checkArgument(void.class.equals(method.getReturnType()), "subscriber's methods  of class {0} must be void instead of {1}", subscriberInstance.getClass(), method.getReturnType());
					Assertion.checkArgument(method.getName().startsWith("on"), "subscriber's methods of class {0} must start with on", subscriberInstance.getClass());
					Assertion.checkArgument(method.getParameterTypes().length == 1, "subscriber's methods of class {0} must be void onXXX(Event e)", subscriberInstance.getClass());
					Assertion.checkArgument(Event.class.isAssignableFrom(method.getParameterTypes()[0]), "subscriber's methods of class {0} must be 'void onXXX(E extends Event)'", subscriberInstance.getClass());
					//-----
					//2. For each method register a listener
					final Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
					final String subscriptionName = "Evt" + StringUtil.first2UpperCase(componentId) + "$" + StringUtil.first2LowerCase(eventType.getSimpleName());
					return new EventBusSubscriptionDefinition<>(subscriptionName, eventType, event -> ClassUtil.invoke(subscriberInstance, method, event));
				})
				.collect(Collectors.toList());

	}

	@Override
	public void start() {
		// nothing
	}

	@Override
	public void stop() {
		subscriptions.clear();
		deadEventListeners.clear();
	}

	private void registerAllSubscribers() {
		subscriptions.addAll(Home.getApp().getDefinitionSpace().getAll(EventBusSubscriptionDefinition.class));

	}

	/** {@inheritDoc} */
	@Override
	public void post(final Event event) {
		Assertion.checkNotNull(event);
		//-----
		final long emitted = subscriptions.stream()
				.filter(subscription -> subscription.match(event))
				.peek(subscription -> subscription.getListener().accept(event))
				.count();

		//manages dead event
		if (emitted == 0) {
			deadEventListeners
					.forEach(deadEventlistener -> deadEventlistener.accept(event));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void registerDead(final Consumer<Event> eventConsumer) {
		Assertion.checkNotNull(eventConsumer);
		//-----
		deadEventListeners.add(eventConsumer);
	}
}
