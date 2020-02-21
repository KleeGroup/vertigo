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
package io.vertigo.dynamo.domain.util;

import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.lang.Assertion;

/**
 * Classe utilitaire permettant de travailler sur les associations.
 *
 * @author pchretien
 */
public final class AssociationUtil {

	private AssociationUtil() {
		//private constructor
	}

	/**
	 * Liste des cardinalitées.
	 *
	 */
	private enum Cardinality {
		/** Cardinalité 0-1. */
		CARD_0_1("0..1", false, false),
		/** Cardinalité 0-N. */
		CARD_0_N("0..*", false, true),
		/** Cardinalité 1-1. */
		CARD_1_1("1..1", true, false),
		/** Cardinalité 1-N. */
		CARD_1_N("1..*", true, true);

		private final boolean multiple;
		private final boolean notNull;
		private final String multiplicity;

		Cardinality(final String multiplicity, final boolean notNull, final boolean multiple) {
			this.multiple = multiple;
			this.notNull = notNull;
			this.multiplicity = multiplicity;
		}

		static Cardinality valueOfMultiplicity(final String multiplicity) {
			for (final Cardinality cardinality : values()) {
				if (cardinality.multiplicity.equals(multiplicity)) {
					return cardinality;
				}
			}

			final StringBuilder msg = new StringBuilder("Multiplicité '").append(multiplicity).append("' non reconnue parmi : ");
			for (final Cardinality cardinality : values()) {
				msg.append(cardinality.multiplicity).append(' ');
			}
			throw new IllegalArgumentException(msg.toString());
		}

		boolean isMultiple() {
			return multiple;
		}

		boolean isNotNull() {
			return notNull;
		}
	}

	/**
	 *
	 * @param multiplicityA Mutiplicité A
	 * @param multiplicityB Multiplicité B
	 * @return Si le noeud est primaire
	 */
	public static boolean isAPrimaryNode(final String multiplicityA, final String multiplicityB) {
		return isAPrimaryNode(isMultiple(multiplicityA), isNotNull(multiplicityA), isMultiple(multiplicityB), isNotNull(multiplicityB));
	}

	/**
	 * Permet de savoir si la node A est bien la node primare et la node B est la node étrangére.
	 * @param isAMultiple isAMultiple
	 * @param isANotNull isANotNull
	 * @param isBMultiple isBMultiple
	 * @param isBNotNull isBNotNull
	 * @return Si A primary node
	 */
	public static boolean isAPrimaryNode(
			final boolean isAMultiple,
			final boolean isANotNull,
			final boolean isBMultiple,
			final boolean isBNotNull) {
		if (isAMultiple && isBMultiple) {
			//relation n-n
			throw new IllegalAccessError();
		}

		if (!isAMultiple && !isBMultiple) {
			//relation simple 1-1
			if (isANotNull && isBNotNull) {
				throw new IllegalArgumentException("Les relations 1..1--1..1 ne sont pas gérées.");
				//Il est impossible de gérer ce type de relation dans un SGBD.
			}
			return isANotNull && !isBNotNull;
			// dans le cas d'une relation simple A(1..1)--B(0..1),
			// A possède la clé primaire
			// B possède la clé étrangère.
			//Exemple
			// Une personne possède 0 ou 1 voiture
			// Une voiture est possédée obligatoirement par une personne.
			//Personne possède la clé primaire,
			//et voiture référence via la clé étrangère cette clé primaire.
		}

		//relation multiple 1-n
		//A XOR B est multiple.
		return isBMultiple;
		//Si B est multiple
		//Il faut donc copier la référence de A dans B.
		//Exemple une personne possède n livre, tout livre appartient à une et une seule personne.
		// A personne 1..1
		// B livre 1..*
		// la clé de personne va être injectée dans Livre
	}

	/**
	 * Retourne une cardinalité sous forme de chaîne de caractères à partir des entrées.
	 * La structure de la multiplicité est la suivante x..y
	 * x est égal à 0 ou 1
	 * y est égal à 1 ou *
	 * @param notNull boolean
	 * @param isMultiple boolean
	 * @return Cardinalité
	 */
	public static String getMultiplicity(final boolean notNull, final boolean isMultiple) {
		return (notNull ? "1" : "0") + ".." + (isMultiple ? "*" : "1");
	}

	/**
	 * Teste si la chaîne de caractères en entrée est une cardinalité multiple.
	 * @param multiplicity Cardinalité en chaîne de caractères.
	 * @return boolean
	 */
	public static boolean isMultiple(final String multiplicity) {
		return Cardinality.valueOfMultiplicity(multiplicity).isMultiple();
	}

	/**
	 * Teste si la chaîne de caractères en entrée est une cardinalité non null (1).
	 * @param multiplicity Cardinalité en chaîne de caractères.
	 * @return boolean
	 */
	public static boolean isNotNull(final String multiplicity) {
		return Cardinality.valueOfMultiplicity(multiplicity).isNotNull();
	}

	/**
	 * Noeud de l'association correspondant à un rôle donné.
	 * @param associationDefinition the definition where to look for the node
	 * @param roleNameSource Nom du Role du noeud source
	 * @return AssociationNode
	 * TODO : voir si passer en arg une URI de collection et/ou d'objet ?
	 */
	public static AssociationNode getAssociationNode(final AssociationDefinition associationDefinition, final String roleNameSource) {
		Assertion.checkNotNull(associationDefinition);
		Assertion.checkNotNull(roleNameSource);
		//-----
		if (roleNameSource.equals(associationDefinition.getAssociationNodeA().getRole())) {
			return associationDefinition.getAssociationNodeA();
		} else if (roleNameSource.equals(associationDefinition.getAssociationNodeB().getRole())) {
			return associationDefinition.getAssociationNodeB();
		}
		throw new IllegalArgumentException("La source " + roleNameSource + " n'existe pas dans l'association " + associationDefinition.getName());
	}

	/**
	 * Noeud de l'association correspondant à la dtDéfinition passé en paramètre.
	 * @param associationDefinition the definition where to look for the node
	 * @param roleNameSource Nom du Role du noeud cible
	 * @return AssociationNode
	 */
	public static AssociationNode getAssociationNodeTarget(final AssociationDefinition associationDefinition, final String roleNameSource) {
		Assertion.checkNotNull(roleNameSource);
		//-----
		if (roleNameSource.equals(associationDefinition.getAssociationNodeA().getRole())) {
			return associationDefinition.getAssociationNodeB();
		} else if (roleNameSource.equals(associationDefinition.getAssociationNodeB().getRole())) {
			return associationDefinition.getAssociationNodeA();
		}
		throw new IllegalArgumentException("La source " + roleNameSource + " n'existe pas dans l'association " + associationDefinition.getName());
	}

}
