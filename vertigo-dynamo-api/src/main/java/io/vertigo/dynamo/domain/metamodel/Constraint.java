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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.core.locale.MessageText;

/**
 * Interface de base pour la définition de contrainte
 * s'appliquant à un champ d'une structure de données.
 * Lors de l'utilisation d'une structure de données dans un formulaire,
 * une vérification automatique des valeurs saisies est réalisée.
 * Le contrôle porte en particulier sur les contraintes définies pour un champ
 * via la notion de domaine.
 * En cas d'erreur, la méthode getErrorMessage retourne la description de l'erreur.
 *
 * La déclaration des contraintes peut être réalisée :
 *  - dans le fichier ksp/xml
 *  - en java directement.
 *
 * @author plepaisant
 * @param <J> Type java de la propriété associée à la contrainte
 * @param <D> Type java de la valeur à contrôler
 */
public interface Constraint<J, D> {
	/**
	 * Cette méthode permet de définir la propriété(le comportement générique) que la contrainte implémente.
	 *
	 * @return Propriété implémentée par la contrainte
	 */
	Property getProperty();

	/**
	 * Cette méthode permet de définir la valeur de la propriété(le comportement spécifique) que la contrainte implémente.
	 *
	 * @return Valeur de la propriété implémentée par la contrainte
	 */
	J getPropertyValue();

	/**
	 * Réalise la validation d'une valeur.
	 * Dans le cas où la méthode renvoie false,
	 * l'appel de checkConstraint() sera suivi de l'appel de getErrorMessage().<br>
	 *
	 * @param value Valeur à évaluer.
	 * @return Résultat du test : true si value est admissible, false en cas d'erreur.
	 */
	boolean checkConstraint(D value);

	/**
	 * Retourne le message d'erreur concernant le dernier appel de la méthode
	 * checkConstraint() ayant renvoyé false. Le message doit commencer par un caractère
	 * en lettre minuscule et décrire l'action correctrice pour la contrainte
	 * n'ayant pas été respectée.
	 *
	 * Exemple : "le champ doit être renseigné"
	 *
	 * @return Message d'erreur
	 */
	MessageText getErrorMessage();
}
