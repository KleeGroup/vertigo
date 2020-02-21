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
package io.vertigo.core.component.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Reactor.
 * - add components in any order with their id, their class
 * - add ids for components that are already existing.
 * - then 'proceed' and you obtain an ORDERED list of components, taking account of their dependencies.
 *
 * @author pchretien
 */
public final class DIReactor {
	//Map des composants et de leurs parents
	private final Set<String> allComponentInfos = new HashSet<>();
	private final List<DIComponentInfo> diComponentInfos = new ArrayList<>();
	private final Set<String> parentComponentInfos = new HashSet<>();

	private void check(final String id) {
		//On vérifie que l'on n'insère pas deux composants avec le même id.
		if (allComponentInfos.contains(id)) {
			throw new DIException("deux composants insérés avec le même id :'" + id + "'");
		}
	}

	/**
	 * Add a component
	 * @param id ID f the component
	 * @param implClass Impl class of the component
	 * @return this reactor
	 */
	public DIReactor addComponent(final String id, final Class<?> implClass) {
		return addComponent(id, implClass, Collections.emptySet());
	}

	/**
	 *
	 * Add a component
	 * @param id ID f the component
	 * @param implClass Impl class of the component
	 * @param params List of ID of all local params - which will be automatically injected-
	 * @return this reactor
	 */
	public DIReactor addComponent(final String id, final Class<?> implClass, final Set<String> params) {
		final DIComponentInfo diComponentInfo = new DIComponentInfo(id, implClass, params);
		check(diComponentInfo.getId());
		allComponentInfos.add(diComponentInfo.getId());
		diComponentInfos.add(diComponentInfo);
		return this;
	}

	/**
	 * Add a component identified by its ID.
	 * This component is ready to be injected in other components (and it does not need to be resolved).
	 * @param id ID of the component
	 * @return this reactor
	 */
	public DIReactor addParent(final String id) {
		check(id);
		allComponentInfos.add(id);
		parentComponentInfos.add(id);
		return this;
	}

	/**
	 * Process the 'digital' reaction in a way to obtain an ordered list of components, taking account of their dependencies.
	 * @return Ordered list of comoponent's Ids.
	 */
	public List<String> proceed() {
		//-----
		//1.On vérifie si tous les composants définis par leurs ids existent
		final StringBuilder missing = new StringBuilder();
		for (final DIComponentInfo componentInfo : diComponentInfos) {
			for (final DIDependency dependency : componentInfo.getDependencies()) {
				//Si une référence est requise
				//et qu'elle est absente, c'est qu'elle est manquante !
				if (dependency.isRequired() && !allComponentInfos.contains(dependency.getName())) {
					missing.append(dependency).append(" (referenced by ").append(componentInfo).append("), ");
				}
			}
		}
		if (missing.length() > 0) {
			throw new DIException("Components or params not found :" + missing.toString() + "\n\tLoaded components/params : " + diComponentInfos);
		}
		//-----
		//2.On résout les dépendances
		final List<DIComponentInfo> unsorted = new ArrayList<>(diComponentInfos);
		//Niveaux de dépendances des composants
		final List<String> sorted = new ArrayList<>();

		//. Par défaut on considére comme triés tous les parents
		//On va trier les nouveaux composants.
		while (!unsorted.isEmpty()) {
			final int countSorted = sorted.size();
			for (final Iterator<DIComponentInfo> iterator = unsorted.iterator(); iterator.hasNext();) {
				final DIComponentInfo componentInfo = iterator.next();
				final boolean solved = isSolved(componentInfo, parentComponentInfos, allComponentInfos, sorted);
				if (solved) {
					//Le composant est résolu
					// - On l'ajoute sa clé à la liste des clés de composants résolus
					// - On le supprime de la liste des composants à résoudre
					sorted.add(componentInfo.getId());
					iterator.remove();
				}
			}
			// Si lors d'une itération on ne fait rien c'est qu'il y a une dépendance cyclique
			if (countSorted == sorted.size()) {
				// On a une dépendance cyclique !
				throw new DIException("Dependencies can't be solved on components (maybe a cyclic dependency) :" + unsorted);
			}
		}
		//-----
		//3 On expose un liste de ids et non les composantInfos
		return Collections.unmodifiableList(sorted);

	}

	private static boolean isSolved(final DIComponentInfo componentInfo, final Set<String> parentComponentInfos, final Set<String> allComponentInfos, final List<String> sorted) {
		//Un composant est résolu si
		// les dépendances obligatoires sont déjà résolues
		// les dépendantes facultatives
		for (final DIDependency dependency : componentInfo.getDependencies()) {
			if (!isSolved(dependency, parentComponentInfos, allComponentInfos, sorted)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSolved(final DIDependency dependency, final Set<String> parentComponentInfos, final Set<String> allComponentInfos, final List<String> sorted) {
		//Une dépendace est résolue si tous les ids concernés sont résolus.
		//Si la dépendance est déjà résolue et bien c'est bon on pass à la dépendances suivante

		if (dependency.isRequired()) {
			return parentComponentInfos.contains(dependency.getName()) || sorted.contains(dependency.getName());
		} else if (dependency.isOption()) {
			//Si l'objet fait partie de la liste alors il doit être résolu.
			if (allComponentInfos.contains(dependency.getName())) {
				return sorted.contains(dependency.getName());
			}
			//Sinon comme il est optionnel c'est ok.
			return true;
		} else if (dependency.isList()) {
			//Si l'objet fait partie de la liste alors il doit être résolu.
			for (final String id : allComponentInfos) {
				final boolean match = id.equals(dependency.getName()) || id.startsWith(dependency.getName() + '#');
				if (match && !sorted.contains(id)) {
					//L'objet id fait partie de la liste
					return sorted.contains(id);
				}
			}
			return true;
		}
		throw new IllegalStateException();
	}
}
