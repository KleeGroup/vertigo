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
package io.vertigo.account.authorization.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * Une authorization est un droit sur une fonction de l'application.
 * Ou sur une opération sur une entite.
 * Sous condition d'un ensemble de règles.
 *
 * @author prahmoune, npiedeloup
 */
@DefinitionPrefix("Atz")
public final class Authorization implements Definition {
	public static final String PREFIX = "Atz";
	//soit authorization globale (sans règle)
	//soit authorization = une opération sur une entity
	private final Optional<String> comment;
	private final String name;
	private final String label;

	private final Set<String> overrides;
	private final Set<Authorization> grants;
	private final List<RuleMultiExpression> rules; //empty -> always true

	private final Optional<DtDefinition> entityOpt;
	private final Optional<String> operationOpt;

	/**
	 * Constructor.
	 *
	 * @param code Code de l'authorization
	 * @param label Label
	 * @param comment Comment
	 */
	public Authorization(final String code, final String label, final Optional<String> comment) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkNotNull(comment);
		//-----
		name = PREFIX + code;
		this.label = label;

		overrides = Collections.emptySet();
		grants = Collections.emptySet();
		entityOpt = Optional.empty();
		operationOpt = Optional.empty();
		rules = Arrays.asList();
		this.comment = comment;
	}

	/**
	 * Constructor.
	 *
	 * @param operation Nom de l'opération
	 * @param label Label
	 * @param entityDefinition Entity definition
	 * @param overrides Liste des opérations overridé par cette opération
	 * @param grants Liste des opérations données par cette opération
	 * @param rules Règles d'évaluation
	 * @param comment Comment
	 */
	public Authorization(
			final String operation,
			final String label,
			final Set<String> overrides,
			final Set<Authorization> grants,
			final DtDefinition entityDefinition,
			final List<RuleMultiExpression> rules,
			final Optional<String> comment) {
		Assertion.checkArgNotEmpty(operation);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkNotNull(overrides);
		Assertion.checkNotNull(grants);
		Assertion.checkNotNull(entityDefinition);
		Assertion.checkNotNull(rules);
		Assertion.checkNotNull(comment);
		//-----
		name = PREFIX + entityDefinition.getLocalName() + '$' + operation;
		this.label = label;
		this.overrides = new HashSet<>(overrides);
		this.grants = new HashSet<>(grants);
		entityOpt = Optional.of(entityDefinition);
		operationOpt = Optional.of(operation);
		this.rules = new ArrayList<>(rules);
		this.comment = comment;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Label de la authorization
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Comment de la authorization
	 */
	public Optional<String> getComment() {
		return comment;
	}

	/**
	 * @return Overrides for this authorization
	 */
	public Set<String> getOverrides() {
		return overrides;
	}

	/**
	 * @return Grants for this authorization
	 */
	public Set<Authorization> getGrants() {
		return grants;
	}

	/**
	 * @return Rules used to check authorization (empty->Always true)
	 */
	public List<RuleMultiExpression> getRules() {
		return rules;
	}

	/**
	 * @return entity definition
	 */
	public Optional<DtDefinition> getEntityDefinition() {
		return entityOpt;
	}

	/**
	 * @return Operation
	 */
	public Optional<String> getOperation() {
		return operationOpt;
	}

}
