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
package io.vertigo.dynamo.environment.loader;

import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.Boolean;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.Double;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.Integer;
import static io.vertigo.dynamo.plugins.environment.dsl.entity.DslPropertyType.String;

import java.util.List;

import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author npiedeloup
 */
public final class PersonGrammar implements DslGrammar {
	static final DslEntity ADDRESS_ENTITY;

	static final String NAME = "name";
	static final String FIRST_NAME = "firstName";
	static final String AGE = "age"; //,
	static final String HEIGHT = "height"; //.Double);
	static final String MALE = "male";
	static final DslEntity PERSON_ENTITY;

	static final String MAIN_ADDRESS = "mainAddress";
	static final String SECOND_ADDRESS = "secondaryAddress";
	static final String STREET = "street";
	static final String POSTAL_CODE = "postalCode";
	static final String CITY = "city";

	static {
		ADDRESS_ENTITY = DslEntity.builder("address")
				.addRequiredField(STREET, String)
				.addOptionalField(POSTAL_CODE, String)
				.addOptionalField(CITY, String)
				.build();
		PERSON_ENTITY = DslEntity.builder("person")
				.addRequiredField(NAME, String)
				.addRequiredField(FIRST_NAME, String)
				.addOptionalField(AGE, Integer)
				.addOptionalField(HEIGHT, Double)
				.addRequiredField(MALE, Boolean)
				.addRequiredField(MAIN_ADDRESS, ADDRESS_ENTITY.getLink())
				.addOptionalField("secondaryAddress", ADDRESS_ENTITY.getLink())
				.build();
	}

	@Override
	public List<DslEntity> getEntities() {
		return new ListBuilder<DslEntity>()
				.add(PERSON_ENTITY)
				.unmodifiable()
				.build();
	}
}
