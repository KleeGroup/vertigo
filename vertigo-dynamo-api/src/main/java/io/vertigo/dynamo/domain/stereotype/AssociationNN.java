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
package io.vertigo.dynamo.domain.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gestion des associations NN.
 *
 * @author  dchallas
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface AssociationNN {
	/**
	 * Nom logique de l'association.
	 */
	String name();

	/**
	 * Nom de la table de jointure.
	 */
	String tableName();

	/**
	 * Nom du DT possédant la table A utilisée (n).
	 */
	String dtDefinitionA();

	/**
	 * Nom du DT possédant la table A utilisée (n).
	 */
	String dtDefinitionB();

	/**
	 * Si le noeud représentant la table A est navigable.
	 */
	boolean navigabilityA();

	/**
	 * Si le noeud représentant la table B est navigable.
	 */
	boolean navigabilityB();

	/**
	 * Label du role associé au noeud représentant la table A.
	 */
	String labelA();

	/**
	 * Label du role associé au noeud représentant la table B.
	 */
	String labelB();

	/**
	 * Nom du role associé au noeud représentant la table A.
	 */
	String roleA();

	/**
	 * Nom du role associé au noeud représentant la table B.
	 */
	String roleB();

}
