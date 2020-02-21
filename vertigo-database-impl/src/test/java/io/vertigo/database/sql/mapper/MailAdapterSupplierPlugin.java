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
package io.vertigo.database.sql.mapper;

import java.util.List;

import io.vertigo.database.impl.sql.SqlAdapterSupplierPlugin;
import io.vertigo.database.sql.data.Mail;
import io.vertigo.util.ListBuilder;

public final class MailAdapterSupplierPlugin implements SqlAdapterSupplierPlugin {

	@Override
	public List<SqlAdapter> getAdapters() {
		return new ListBuilder<SqlAdapter>()
				.add(new SqlAdapter<Mail, String>() {
					@Override
					public Mail toJava(final String sqlValue) {
						return sqlValue == null ? null : new Mail(sqlValue);
					}

					@Override
					public String toSql(final Mail mail) {
						return mail != null ? mail.toString() : null;
					}

					@Override
					public Class<Mail> getJavaDataType() {
						return Mail.class;
					}

					@Override
					public Class<String> getSqlDataType() {
						return String.class;
					}
				}).build();
	}
}
