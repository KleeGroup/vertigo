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
package io.vertigo.dynamo.criteria;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

final class CriteriaExpression<E extends Entity> extends Criteria<E> {
	private static final long serialVersionUID = 8301054336845536973L;

	enum CriteriaOperator {
		OR, AND
	}

	private final CriteriaOperator operator;
	private final Criteria<E>[] operands;

	CriteriaExpression(final CriteriaOperator operator, final Criteria<E>[] leftOperands, final Criteria<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperands);
		//---
		this.operator = operator;
		final int size = leftOperands.length + 1;
		this.operands = new Criteria[size];
		for (int i = 0; i < leftOperands.length; i++) {
			this.operands[i] = leftOperands[i];
		}
		this.operands[size - 1] = rightOperand;
	}

	CriteriaExpression(final CriteriaOperator operator, final Criteria<E> leftOperand, final Criteria<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperand);
		//---
		this.operator = operator;
		this.operands = new Criteria[] { leftOperand, rightOperand };
	}

	CriteriaOperator getOperator() {
		return operator;
	}

	Criteria<E>[] getOperands() {
		return operands;
	}

	@Override
	public Predicate<E> toPredicate() {
		final BinaryOperator<Predicate<E>> accumulator;
		if (operator == CriteriaOperator.OR) {
			accumulator = Predicate::or;
		} else if (operator == CriteriaOperator.AND) {
			accumulator = Predicate::and;
		} else {
			throw new IllegalAccessError();
		}

		return Arrays.stream(operands)
				.map(Criteria::toPredicate)
				.reduce(accumulator)
				.orElseThrow(IllegalAccessError::new);
	}

	@Override
	String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect) {
		return Arrays.stream(operands)
				.map(operand -> operand.toSql(ctx, sqlDialect))
				.collect(Collectors.joining(" " + operator.name() + " ", "(", ")"));
	}
}
