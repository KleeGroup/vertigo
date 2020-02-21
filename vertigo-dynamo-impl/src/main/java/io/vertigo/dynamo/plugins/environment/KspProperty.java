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
package io.vertigo.dynamo.plugins.environment;

/**
 * Métadonnée liée à la grammaire.
 *
 * @author  pchretien
 */
public final class KspProperty {
	/**
	 * Nom de la classe java implémentant un concept tel que formatter, constraint...
	 */
	public static final String CLASS_NAME = "CLASS_NAME";

	/**
	 * Arguments initialisant la classe précédante.
	 */
	public static final String ARGS = "ARGS";

	//==============================
	//----Constraint
	//==============================
	/**
	 * Propriété standard : message d'erreur, valeur String.
	 */
	public static final String MSG = "MSG";

	//==============================
	//----Domain
	//==============================
	public static final String FRAGMENT_OF = "FRAGMENT_OF";
	/**
	 * If the domain is multiple or single.
	 * for data-object   : DtLis<Car>
	 * for primitives    : List<Integer>
	 * for value-objects : List<Point>
	 */
	public static final String MULTIPLE = "MULTIPLE";

	/**
	 * Propriété standard : libellé du champ, valeur String.
	 */
	public static final String LABEL = "LABEL";
	/**
	 * Propriété standard : champ obligatoire, valeur Boolean.
	 */
	public static final String REQUIRED = "REQUIRED";

	/**
	 * Propriété liée au dataStore : La donnée est-elle gérée en mode write par le dataSore.
	 */
	public static final String PERSISTENT = "PERSISTENT";

	/**
	 * Champ qui porte le trie par défaut. (Un au plus par DT)
	 */
	public static final String SORT_FIELD = "SORT_FIELD";

	/**
	 * Champ qui porte l'affichage par défaut. (Un au plus par DT)
	 */
	public static final String DISPLAY_FIELD = "DISPLAY_FIELD";

	/**
	 * Champ qui sert pour le handle. (Un au plus par DT)
	 */
	public static final String HANDLE_FIELD = "HANDLE_FIELD";

	/**
	 * Le Stereotype appliqué au DT : Data (defaut), MasterData ou KeyConcept
	 */
	public static final String STEREOTYPE = "STEREOTYPE";

	/**
	 * Nom du champ représentant la clé étrangére dans une association simple (non NN).
	 */
	public static final String FK_FIELD_NAME = "FK_FIELD_NAME";

	/**
	 * Cardinalité du noeud A dans une association : 1..* ou 0..* ou 1..1.
	 */
	public static final String MULTIPLICITY_A = "MULTIPLICITY_A";

	/**
	 * Navigabilité du noeud A dans une association.
	 */
	public static final String NAVIGABILITY_A = "NAVIGABILITY_A";

	/**
	 * Nom du réle du noeud A dans une association.
	 */
	public static final String ROLE_A = "ROLE_A";

	/**
	 * Label du noeud A dans une association.
	 */
	public static final String LABEL_A = "LABEL_A";

	/**
	 * Cardinalité du noeud B dans une association : 1..* ou 0..* ou 1..1.
	 */
	public static final String MULTIPLICITY_B = "MULTIPLICITY_B";

	/**
	 * Navigabilité du noeud B dans une association.
	 */
	public static final String NAVIGABILITY_B = "NAVIGABILITY_B";

	/**
	 * Label du noeud B dans une association.
	 */
	public static final String LABEL_B = "LABEL_B";

	/**
	 * Nom du réle du noeud B dans une association.
	 */
	public static final String ROLE_B = "ROLE_B";

	/**
	 * Nom de la table supportant l'association NN.
	 */
	public static final String TABLE_NAME = "TABLE_NAME";

	//==============================
	//----Task
	//==============================
	/**
	 * Requête ou plus générallement paramètre d'une tache.
	 */
	public static final String REQUEST = "REQUEST";

	/**
	 * Type in ou out d'un attribut de tache.
	 */
	public static final String IN_OUT = "IN_OUT";

	//==============================
	//----FileInfo
	//==============================
	/**
	 * Racine des éléments de cette définition.
	 */
	public static final String DATA_SPACE = "STORE_NAME";

	//==============================
	//----Mda
	//==============================
	/**
	 * Expression du champ computed.
	 */
	public static final String EXPRESSION = "EXPRESSION";

	/**
	 * Type de données en base.
	 */
	public static final String STORE_TYPE = "STORE_TYPE";

	//==============================
	//----Propriétés hérités de DoProperty
	//==============================

	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	public static final String MAX_LENGTH = "MAX_LENGTH";

	/**
	 * Propriété standard : Type des définitions.
	 */
	public static final String TYPE = "TYPE";

	/**
	 * Proriété Regex de type String.
	 */
	public static final String REGEX = "REGEX";

	//==============================
	//-----Style
	//==============================
	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	public static final String UNIT = "UNIT";

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	public static final String INDEX_TYPE = "INDEX_TYPE";

	private KspProperty() {
		//private
	}
}
