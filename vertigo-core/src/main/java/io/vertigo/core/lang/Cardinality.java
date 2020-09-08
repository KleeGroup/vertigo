/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

/** Cardinalities. */
public enum Cardinality {
	/** 
	 * card   : 0  or 1
	 * symbol : ?
	 */
	OPTIONAL_OR_NULLABLE,
	/** 
	 * card   : 1 
	 * symbol : 1
	 */
	ONE,
	/**
	 * card   :  0..n 
	 * symbol : *
	 */
	MANY;

	public boolean isOptionalOrNullable() {
		return OPTIONAL_OR_NULLABLE == this;
	}

	public boolean hasOne() {
		return ONE == this;
	}

	public boolean hasMany() {
		return MANY == this;
	}

	public static Cardinality fromSymbol(final String sCardinality) {
		Assertion.check().isNotBlank(sCardinality);
		//---
		switch (sCardinality) {
			case "?":
				return OPTIONAL_OR_NULLABLE;
			case "1":
				return ONE;
			case "*":
				return MANY;
			default:
				throw new VSystemException("Unknown cardinality symbol : '{0}'. Supported cardinalities are '?' for optional, '1' for one and '*' for many ", sCardinality);
		}
	}

	public String toSymbol() {
		switch (this) {
			case OPTIONAL_OR_NULLABLE:
				return "?";
			case ONE:
				return "1";
			case MANY:
				return "*";
			default:
				throw new VSystemException("Unknown cardinality : '{0}'. Supported cardinalities are optional, one and many ", this);
		}
	}

}
