/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.kernel.di.reactor;

import io.vertigo.kernel.di.DIException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Réacteur.
 * Objet dans lequel on met en vrac des composants.
 * Puis il suffit d'appeler la méthode  'proceed'  
 *   
 * @author pchretien
 */
public final class Reactor {
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

	public Reactor addComponent(final String id, final Class<?> implClass) {
		return addComponent(id, implClass, Collections.<String> emptySet(), Collections.<String> emptySet());
	}

	public Reactor addComponent(final String id, final Class<?> implClass, final Set<String> pluginIds, final Set<String> params) {
		final DIComponentInfo diComponentInfo = new DIComponentInfo(id, implClass, pluginIds, params);
		check(diComponentInfo.getId());
		allComponentInfos.add(diComponentInfo.getId());
		diComponentInfos.add(diComponentInfo);
		return this;
	}

	/**
	 * Ajout d'un comoposant identifié par son seul id.
	 */
	public Reactor addParent(final String id) {
		check(id);
		allComponentInfos.add(id);
		parentComponentInfos.add(id);
		return this;
	}

	/**
	 * Méthode clé permettant d'activer la réaction.
	 * @return Liste ordonnée des ids de composants, selon leur dépendances. 
	 */
	public List<String> proceed() {
		//-----------------------------------------------------------------------------------------
		//1.On vérifie si tous les composants définis par leurs ids existent
		final List<DIDependency> missing = new ArrayList<>();
		for (final DIComponentInfo componentInfo : diComponentInfos) {
			for (final DIDependency dependency : componentInfo.getDependencies()) {
				//Si une référence est optionnelle alors elle n'est jamais manquante.
				if (!dependency.isOptional() && !allComponentInfos.contains(dependency.getId())) {
					missing.add(dependency);
				}
			}
		}
		if (!missing.isEmpty()) {
			throw new DIException("Components not found :" + missing + "\n\tLoaded components : " + diComponentInfos);
		}
		//-----------------------------------------------------------------------------------------
		//2.On résout les dépendances
		final List<DIComponentInfo> unsorted = new ArrayList<>(diComponentInfos);
		//Niveaux de dépendances des composants 
		//		final List<List<String>> levels = new ArrayList<>();
		final List<String> sorted = new ArrayList<>();

		//. Par défaut on considére comme triés tous les parents
		while (!unsorted.isEmpty()) {
			final int countSorted = sorted.size();
			for (final Iterator<DIComponentInfo> iterator = unsorted.iterator(); iterator.hasNext();) {
				final DIComponentInfo componentInfo = iterator.next();
				boolean solved = true;
				for (final DIDependency dependency : componentInfo.getDependencies()) {
					//On vérifie si pour un composant 
					//TOUTES ses dépendances sont bien déjà résolues.
					if (allComponentInfos.contains(dependency.getId()) || !dependency.isOptional()) {
						//On doit résoudre toutes des références connues(y compris les référenes optionnelles) sans tenir compte des références inconnues et optionnelles.
						solved = solved && (sorted.contains(dependency.getId()) || parentComponentInfos.contains(dependency.getId()));
					}
					if (!solved) {
						//Si ce n'est pas le cas on passe au composant suivant.
						//On arréte de regarder les autres dépendances
						break;
					}
				}
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
				throw new DIException("Liste des composants non résolus :" + unsorted);
			}
		}
		//-----------------------------------------------------------------------------------------
		//3 On expose un liste de ids et non les composantInfos
		return Collections.unmodifiableList(sorted);

	}
}