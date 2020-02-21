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

import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.util.ListBuilder;

/**
 *
 * @author pchretien
 */
public final class Movie2DataBase {
	private final List<Movie2> movies = new ListBuilder<Movie2>()
			.add(createMovie2(1933, "king kong"))
			.add(createMovie2(1951, "pandora"))
			.add(createMovie2(1959, "vertigo"))
			.add(createMovie2(1979, "alien"))
			.add(createMovie2(1980, "shining"))
			.add(createMovie2(1984, "amadeus"))
			.add(createMovie2(1984, "1984"))
			.add(createMovie2(1984, "terminator"))
			.add(createMovie2(1985, "porco rosso"))
			.add(createMovie2(2000, "gladiator"))
			.add(createMovie2(2014, "interstellar"))
			.add(createMovie2(2014, "mommy"))
			.add(createMovie2(null, "ordet"))
			.unmodifiable()
			.build();

	private static Movie2 createMovie2(final Integer year, final String title) {
		final Movie2 movie = new Movie2();
		movie.setYear(year);
		movie.setTitle(title);
		return movie;
	}

	public final DtList<Movie2> getAllMovies() {
		return movies
				.stream()
				.collect(VCollectors.toDtList(Movie2.class));
	}
}
