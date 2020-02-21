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
package io.vertigo.core.component;

/**
 * Composant.
 * Tout composant doit être ThreadSafe.
 * Un module possède un paramétrage et un état interne,
 * ce qui lui permet d'offrir des services.
 *
 * L'usage du module permet d'enrichir des statistiques.
 *
 * Le module permet de représenter
 * - ce qu'il est, ce qu'il fait (ex : module de cache permet de...)
 * - les statistiques d'usage (ex: 99 % d'utilisation du cache)
 * - son état interne (ex : 153 Mo utilisé dont 3 Mo sur disque)
 * - son paramétrage. (ex : Implémentation eh cache avec les paramètres suivants ...)
 *
 * Les statistiques et l'état varient au fil du temps.
 * Le paramétrage doit être stable et nécessite une reconfiguration. (Nouvelle version d'une application)
 * Le contrat du composant (ce qu'il est, ce qu'il fait) doit évidemment être très stable.
 *
 * Lors du démarrage du composant. (méthode Start)
 *  - Vérification de la configuration avec les méthodes register.
 * Lors de l'arrét du composant
 *  - Libération des ressources consommées par le composant lors du undeploy.
 * 		Exemples : connexions, thread, flux
 * @author pchretien
 */
public interface Component {
	//Comportements ajoutables : Activeable,  Describable
}
