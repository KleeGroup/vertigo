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
package io.vertigo.commons.peg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.vertigo.util.StringUtil;

/**
 * Exception levée lorsque la règle n'est pas respectée.
 * @author pchretien
 */
public final class PegNoMatchFoundException extends Exception {
	private static final long serialVersionUID = 6096295235950712319L;
	private static final int CONTEXT_AROUND_ERROR_WIDTH = 150; //number of char before and after an error to extract context
	private final int index;
	private final String s;
	private final String comment;
	private final Serializable[] commentValues;

	/**
	 * Constructor.
	 * @param s Texte parsé
	 * @param index Index dans le texte
	 * @param rootException Cause
	 * @param comment Explication de l'erreur au format MessageFormat si il y a des valeurs
	 * @param commentValues Valeurs pour le MessageFormat de comment (il n'est pas conseillé de les fusionner au préalable dans comment)
	 */
	public PegNoMatchFoundException(final String s, final int index, final PegNoMatchFoundException rootException, final String comment, final Serializable... commentValues) {
		super(rootException);
		this.index = index;
		this.s = s;
		this.comment = comment;
		this.commentValues = commentValues != null ? commentValues.clone() : null;
	}

	/**
	 * Ligne la plus avancée lors de de l'évaluation de la règle.
	 * Cette ligne indique l'endroit vraisemblable de l'erreur SI IL s'agit de la bonne règle !!
	 * @return Index probable de l'erreur.
	 */
	private int getLine() {
		//On compte le nombre de lignes
		int line = 0;
		int next = 0;
		while (next != -1 && next < index) {
			line++;
			next = s.indexOf('\n', next + 1);
			if (next >= s.length()) {
				throw new IllegalStateException("Calcul du nombre de lignes erroné");
			}
		}
		return line;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return displayPosition() + "\n\t" + displayRule();
	}

	/**
	 * @return Retourne le message complet de l'erreur avec la pile inversée des causes.
	 */
	public String getFullMessage() {
		final StringBuilder sb = new StringBuilder()
				.append(displayPosition())
				.append("\n");
		final List<String> errorRuleList = new ArrayList<>();
		Throwable cause = this;
		while (cause instanceof PegNoMatchFoundException) {
			errorRuleList.add(0, ((PegNoMatchFoundException) cause).displayRule());
			cause = cause.getCause();
		}
		String sep = "- ";
		int i = 1;
		for (final String msg : errorRuleList) {
			sb.append('\t')
					.append(i++)
					.append(sep)
					.append(msg)
					.append('\n');
			sep = "- dans ";
		}
		return sb.toString();
	}

	/**
	 * @return Chaîne de caractère pour la regle en question.
	 */
	private String displayRule() {
		final StringBuilder sb = new StringBuilder();
		sb.append("regle : ");
		if (commentValues != null && commentValues.length != 0) {
			sb.append(StringUtil.format(comment, (Object[]) commentValues));
		} else {
			sb.append(comment);
		}
		return sb.toString();
	}

	/**
	 * @return Chaîne de caractère pour la position de l'erreur (ligne, colonne, extrait).
	 */
	private String displayPosition() {
		int start = index > 1 ? (s.lastIndexOf('\n', index - 1) + 1) : 0;
		start = Math.max(index - CONTEXT_AROUND_ERROR_WIDTH, start);
		int end = s.indexOf('\n', index + 1);
		if (end == -1) {
			end = s.length();
		}
		end = Math.min(index + CONTEXT_AROUND_ERROR_WIDTH, end);
		final int pos = index - start;
		final StringBuilder pointeur = new StringBuilder(pos);
		for (int i = 0; i < pos; i++) {
			pointeur.append(' ');
		}
		pointeur.append("^^^");

		return new StringBuilder("Erreur à la ligne ")
				.append(getLine())
				.append(" colonne ")
				.append(index - start)
				.append("\n\tvers : ")
				.append(s.substring(start, end).replace('\t', ' '))
				.append("\n\t      ")
				.append(pointeur)
				.toString();
	}

	/**
	 * @return Position du curseur au lancement de l'erreur
	 */
	public int getIndex() {
		return index;
	}
}
