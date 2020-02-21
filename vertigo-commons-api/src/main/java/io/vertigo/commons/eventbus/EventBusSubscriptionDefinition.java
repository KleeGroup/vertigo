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

import java.util.function.Consumer;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * This defintion defines a subscripter in the eventbus pattern.
 * A endpoint is  :
 * 			- a type of event
 * 			- a way to consume the event.
 * @author pchretien
 *
 * @param <E> type of event
 */
@DefinitionPrefix("Evt")
public final class EventBusSubscriptionDefinition<E extends Event> implements Definition {

	private final String name;
	private final Class<E> eventType;
	private final Consumer<E> eventListener;

	/**
	 * Constructor
	 * @param name the name (must be unique)
	 * @param eventType the type of event subscribed
	 * @param eventListener the consumer of the event (what will be done with it)
	 */
	public EventBusSubscriptionDefinition(final String name, final Class<E> eventType, final Consumer<E> eventListener) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(eventType);
		Assertion.checkNotNull(eventListener);
		//-----
		this.name = name;
		this.eventType = eventType;
		this.eventListener = eventListener;
	}

	/**
	 * Return if an event matches the event type of the actual subscription
	 * @param event the type of event to test
	 * @return true if it matches
	 */
	public boolean match(final Event event) {
		Assertion.checkNotNull(event);
		//-----
		return eventType.isInstance(event);
	}

	/**
	 * Return the consumer of the event
	 * @return the consumer
	 */
	public Consumer<E> getListener() {
		return eventListener;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	public Class<E> getEventType() {
		return eventType;
	}
}
