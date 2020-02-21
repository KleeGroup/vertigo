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
package io.vertigo.vega.engines.webservice.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonParseException;

public final class UTCDateUtil {

	private static final String[] INPUT_DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", //ISO8601
			"yyyy-MM-dd'T'HH:mm:ss'Z'", //ISO8601
			"EEE, dd MMM yyyy HH:mm:ss zzz", // RFC 822, updated by RFC 1123
			"EEEE, dd-MMM-yy HH:mm:ss zzz", // RFC 850, obsoleted by RFC 1036
			//not supported : "EEE MMM d HH:mm:ss yyyy", // ANSI C's asctime() format
	};

	private UTCDateUtil() {
		//private for util classes
	}

	/**
	 * Format date to utc date string.
	 * @param date Date to format
	 * @return Utc date string
	 */
	public static String format(final Date date) {
		//Use INPUT_DATE_FORMATS[0] => ISO8601 format
		return createDateFormat(INPUT_DATE_FORMATS[0], isTruncatedDate(date)).format(date);
	}

	/**
	 * Parse Utc date string to date
	 * @param inputDate Utc date string
	 * @return date
	 */
	public static Date parse(final String inputDate) {
		final boolean isTruncatedDate = isTruncatedDate(inputDate);
		for (final String format : INPUT_DATE_FORMATS) {
			try {
				return createDateFormat(format, isTruncatedDate).parse(inputDate);
			} catch (final ParseException e) {
				//nothing
			}
		}
		throw new JsonParseException("Unsupported Date format " + inputDate);
	}

	/**
	 * Parse Date string to Instant
	 * @param inputDate date string
	 * @return Instant
	 */
	public static Instant parseInstant(final String inputDate) {
		for (final String format : INPUT_DATE_FORMATS) {
			try {
				return Instant.from(DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC")).parse(inputDate));
			} catch (final DateTimeParseException e) {
				//nothing
			}
		}
		throw new JsonParseException("Unsupported Instant format " + inputDate);
	}

	private static DateFormat createDateFormat(final String dateFormat, final boolean isTruncatedDate) {
		final DateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
		if (!isTruncatedDate) {
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return simpleDateFormat;
	}

	private static boolean isTruncatedDate(final Date date) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0;
	}

	private static boolean isTruncatedDate(final String dateStr) {
		return dateStr.endsWith("T00:00:00.000Z");
	}
}
