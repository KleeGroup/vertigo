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
package io.vertigo.core.component.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Centralisation des accès aux composants et aux plugins.
 *
 * Les composants et leur initializers sont instanciés par injection
 *  - des paramètres déclarés sur le scope composant.
 *  - des autres composants
 *
 * Les plugins sont instanciés par injection
 *  - des paramètres déclarés sur le scope plugin.
 *  - des autres composants
 *
 * Donc un plugin ne peut pas être injecté dans un plugin, il ne peut être injecté que dans LE composant pour lequel il est prévu.
 * En revanche les composants (à ne pas réaliser de dépendances cycliques) peuvent être injecter dans les composants, les plugins et les initializers.
 *
 * @author pchretien
 */
public final class ComponentSpaceWritable implements ComponentSpace, Activeable {

	private static final Logger LOGGER = LogManager.getLogger(ComponentSpaceWritable.class);
	/**
	 * Components (sorted by creation)
	 */
	private final Map<String, Component> components = new LinkedHashMap<>();
	/**
	 * Started components are sublist of components.values(). They are added after the start call of a component.
	 */
	private final List<Component> startedComponents = new ArrayList<>();
	private final AtomicBoolean locked = new AtomicBoolean(false);

	public ComponentSpaceWritable() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		startComponents();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		stopComponents();
		clear();
	}

	/**
	 * Register a component with its id.
	 * @param componentId id of the component
	 * @param component instance of the component
	 */
	void registerComponent(final String componentId, final Component component) {
		Assertion.checkState(!locked.get(), "Registration is now closed. A component can be registerd only during the boot phase");
		Assertion.checkArgNotEmpty(componentId);
		Assertion.checkNotNull(component);
		//-----
		final Object previous = components.put(componentId, component);
		Assertion.checkState(previous == null, "component '{0}' already registered", componentId);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkArgNotEmpty(id);
		//-----
		final String normalizedId = StringUtil.first2LowerCase(id);
		return components.containsKey(normalizedId);
	}

	/** {@inheritDoc} */
	@Override
	public <C> C resolve(final String id, final Class<C> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(id);
		Assertion.checkArgument(contains(normalizedId), "Aucun composant enregistré pour id = {0} parmi {1}", normalizedId, keySet());
		//-----
		return componentClass.cast(components.get(normalizedId));
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return components.keySet();
	}

	private static void startComponent(final Component component) {
		if (component instanceof Activeable) {
			Activeable.class.cast(component).start();
		}
	}

	private static void stopComponent(final Component component) {
		if (component instanceof Activeable) {
			Activeable.class.cast(component).stop();
		}
	}

	private void clear() {
		components.clear();
	}

	private void startComponents() {
		for (final Component component : components.values()) {
			startedComponents.add(component);
			startComponent(component);
		}
	}

	private void stopComponents() {
		/* Fermeture de tous les gestionnaires.*/
		//On fait les fermetures dans l'ordre inverse des enregistrements.
		//On se limite aux composants qui ont été démarrés.
		final List<Component> reversedComponents = new ArrayList<>(startedComponents);
		java.util.Collections.reverse(reversedComponents);

		for (final Component component : reversedComponents) {
			try {
				stopComponent(component);
			} catch (final Exception e) {
				LOGGER.error("Failed stopping component " + component, e);
			}
		}
	}

	/**
	 * Close registration of components.
	 * After calling this method no more components are added to the componentSpace.
	 */
	void closeRegistration() {
		//registration is now closed.
		locked.set(true);
	}
}
