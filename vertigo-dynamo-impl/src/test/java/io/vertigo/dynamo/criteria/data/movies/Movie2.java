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
package io.vertigo.dynamo.criteria.data.movies;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Movie.
 */
public final class Movie2 implements Entity {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Field(domain = "DoId", type = "ID", required = true, label = "id of the movie")
	private Long id;
	@Field(domain = "DoString", label = "title")
	private String title;
	@Field(domain = "DoInteger", label = "year")
	private Integer year;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	public final Integer getYear() {
		return year;
	}

	public final void setYear(final Integer year) {
		this.year = year;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}

	@Override
	public UID<Movie2> getUID() {
		return UID.of(Movie2.class, id);
	}
}
