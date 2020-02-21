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
package io.vertigo.vega.webservice.data.ws;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.FileAttachment;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.HeaderParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

//bas� sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class FileDownloadWebServices implements WebServices {

	@Inject
	private ResourceManager resourcetManager;
	@Inject
	private FileManager fileManager;

	@GET("/downloadFile")
	public VFile testDownloadFile(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final File imageFile = asFile(imageUrl);
		final VFile imageVFile = fileManager.createFile("image" + id + ".png", "image/png", imageFile);
		return imageVFile;
	}

	@AnonymousAccessAllowed
	@GET("/downloadEmbeddedFile")
	@FileAttachment(false)
	public VFile testDownloadEmbeddedFile(final @QueryParam("id") Integer id) {
		return testDownloadFile(id);
	}

	@GET("/downloadNotModifiedFile")
	public VFile testDownloadNotModifiedFile(final @QueryParam("id") Integer id, final @HeaderParam("If-Modified-Since") Optional<Date> ifModifiedSince, final HttpServletResponse response) {
		final VFile imageFile = testDownloadFile(id);
		if (ifModifiedSince.isPresent() && imageFile.getLastModified().compareTo(Instant.ofEpochMilli(ifModifiedSince.get().getTime())) <= 0) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
			//this service must declared VFile as return type because it should return VFile when file was modified
		}
		return imageFile;
	}

	@AnonymousAccessAllowed
	@GET("/downloadFileContentType")
	public VFile testDownloadFileContentType(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final File imageFile = asFile(imageUrl);
		return fileManager.createFile("image" + id + generateSpecialChars(id) + ".png", "image/png", imageFile);
	}

	private static String generateSpecialChars(final Integer id) {
		switch (id) {
			case 1:
				return "ÔÙæóñ";
			case 2:
				return "µ°«/";
			case 3:
				return "ÔÙæ óñµ°«/";
			case 4:
				return "€;_~";
			case 5:
				return "你好abcABCæøåÆØÅäöüïëêîâéíáóúýñ½§!#¤%&()=`@£$€{[]}+´¨^~'-_,;";
			default:
				return "";
		}
	}

	private static File asFile(final URL url) {
		try {
			return new File(url.toURI());
		} catch (final URISyntaxException e) {
			return new File(url.getPath());
		}
	}
}
