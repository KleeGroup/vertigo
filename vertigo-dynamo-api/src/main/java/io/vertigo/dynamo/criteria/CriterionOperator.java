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

enum CriterionOperator {
	IS_NULL(0),
	IS_NOT_NULL(0),
	EQ(1), //or is null
	NEQ(1),
	GT(1),
	GTE(1),
	LT(1),
	LTE(1),
	STARTS_WITH(1),
	BETWEEN(2),
	IN(-1);// -1 is unbounded

	private final int arity;

	private CriterionOperator(final int arity) {
		this.arity = arity;
	}

	int getArity() {
		return arity;
	}

}
