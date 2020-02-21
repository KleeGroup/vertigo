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
package io.vertigo.database.sql.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.vertigo.lang.DataStream;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

public final class Movies {
	public static final String TITLE_MOVIE_1 = "citizen kane"; //1 May 1941, ?
	public static final String TITLE_MOVIE_2 = "vertigo"; //9 May 1958
	public static final String TITLE_MOVIE_3 = "gone girl";
	public static final String TITLE_MOVIE_4 = "Jurassic Park";

	private Movies() {
		//nothing
	}

	public static Movie createMovie(
			final long id,
			final String title,
			final Mail mail,
			final Double fps,
			final Double income,
			final Boolean color,
			final Date date,
			final LocalDate localDate,
			final Instant releaseInstant) {
		final Movie movie = new Movie();
		movie.setId(id);
		movie.setTitle(title);
		movie.setMail(mail);
		movie.setReleaseDate(date);
		movie.setReleaseLocalDate(localDate);
		movie.setReleaseInstant(releaseInstant);
		movie.setFps(fps);
		if (income != null) {
			movie.setIncome(new BigDecimal(income));
		}
		movie.setColor(color);
		movie.setIcon(buildIcon());
		return movie;
	}

	public static DataStream buildIcon() {
		return new DataStream() {

			@Override
			public long getLength() {
				try {
					return new File(Movie.class.getResource("icons.png").toURI()).length();
				} catch (final URISyntaxException e) {
					throw WrappedException.wrap(e);
				}
			}

			@Override
			public InputStream createInputStream() throws IOException {
				return Movie.class.getResourceAsStream("icons.png");
			}
		};
	}

	public static void checkMovie(final Movie movie) {
		final LocalDateTime ldt = movie.getReleaseInstant() == null ? null : LocalDateTime.ofInstant(movie.getReleaseInstant(), ZoneId.of("UTC"));
		switch (movie.getId().intValue()) {
			case 1:
				Assertions.assertEquals(TITLE_MOVIE_1, movie.getTitle());
				//java.util.Date
				Assertions.assertEquals("01-05-1941 16:30", new SimpleDateFormat("dd-MM-YYYY HH:mm").format(movie.getReleaseDate()));
				//LocalDate
				Assertions.assertEquals(1, movie.getReleaseLocalDate().getDayOfMonth());
				Assertions.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assertions.assertEquals(1941, movie.getReleaseLocalDate().getYear());
				//Instant
				Assertions.assertEquals(1, ldt.getDayOfMonth());
				Assertions.assertEquals(5, ldt.getMonthValue());
				Assertions.assertEquals(1941, ldt.getYear());
				Assertions.assertEquals(16, ldt.getHour());
				Assertions.assertEquals(30, ldt.getMinute());
				Assertions.assertEquals(0, ldt.getSecond());

				break;
			case 2:
				Assertions.assertEquals(TITLE_MOVIE_2, movie.getTitle());
				//java.util.Date
				Assertions.assertEquals("09-05-1958 16:30", new SimpleDateFormat("dd-MM-YYYY HH:mm").format(movie.getReleaseDate()));
				//LocalDate
				Assertions.assertEquals(9, movie.getReleaseLocalDate().getDayOfMonth());
				Assertions.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assertions.assertEquals(1958, movie.getReleaseLocalDate().getYear());
				//Instant
				Assertions.assertEquals(9, ldt.getDayOfMonth());
				Assertions.assertEquals(5, ldt.getMonthValue());
				Assertions.assertEquals(1958, ldt.getYear());
				Assertions.assertEquals(16, ldt.getHour());
				Assertions.assertEquals(30, ldt.getMinute());
				Assertions.assertEquals(0, ldt.getSecond());

				break;
			case 3:
				Assertions.assertEquals(TITLE_MOVIE_3, movie.getTitle());
				//java.util.Date
				Assertions.assertEquals(null, movie.getReleaseDate());
				//LocalDate
				Assertions.assertEquals(null, movie.getReleaseLocalDate());
				//Instant
				Assertions.assertEquals(null, ldt);
				break;
			default:
				Assertions.fail();
		}
	}

	private static Movie createMovie(
			final long id,
			final String title,
			final String mailAsString,
			final Double fps,
			final Double income,
			final Boolean color,
			final Integer year,
			final Integer month,
			final Integer dayOfMonth) {

		final Calendar calendar = new GregorianCalendar();
		calendar.set(year, month, dayOfMonth);

		final LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
		return Movies.createMovie(
				id,
				title,
				new Mail(mailAsString),
				fps,
				income,
				color,
				calendar.getTime(),
				localDate,
				LocalDateTime.of(year, month, dayOfMonth, 16, 30).toInstant(ZoneOffset.UTC));
	}

	public static final List<Movie> bondMovies() {
		return new ListBuilder<Movie>()
				.add(createMovie(1, "Doctor No", "drNo@jbonds.com", 23.976, 59.600, true, 1962, 10, 5))
				.add(createMovie(2, "From Russia with Love", null, 23.976, 78.900, true, 1963, 10, 10))
				.add(createMovie(3, "Goldfinger", null, 23.976, 124.900, true, 1964, 9, 17))
				.add(createMovie(4, "Thunderball", null, 23.976, 141.200, true, 1965, 12, 17))
				.add(createMovie(5, "You only live twice", null, 23.976, 111.600, true, 1967, 6, 12))
				.add(createMovie(6, "On her majesty's secret service", null, 23.976, 87.400, true, 1969, 12, 18))
				.add(createMovie(7, "Diamonds are forever", null, 23.976, 116.000, true, 1971, 12, 14))
				.add(createMovie(8, "Live and let die", null, 23.976, 161.800, true, 1973, 6, 27))
				.add(createMovie(9, "The man with the golden gun", null, 23.976, 97.600, true, 1974, 12, 19))
				.add(createMovie(10, "The spy who loved me", null, 23.976, 185.400, true, 1977, 7, 7))
				.add(createMovie(11, "Moonraker", null, 23.976, 210.300, true, 1979, 6, 26))
				.add(createMovie(12, "For your eyes only", null, 23.976, 195.300, true, 1981, 6, 24))
				.add(createMovie(13, "Octopussy", null, 23.976, 187.500, true, 1983, 6, 6))
				.add(createMovie(14, "A view to a kill", null, 23.976, 152.400, true, 1985, 5, 22))
				.add(createMovie(15, "The living daylights", null, 23.976, 191.200, true, 1987, 6, 27))
				.build();
	}
}
