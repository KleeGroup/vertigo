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
package io.vertigo.studio.plugins.masterdata.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import com.google.gson.Gson;

import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.studio.impl.masterdata.MasterDataValueProviderPlugin;
import io.vertigo.studio.masterdata.MasterDataValues;

/**
 * Plugin for retrieving masterdata values from a json file.
 * @author mlaroche
 *
 */
public class JsonMasterDataValueProvider implements MasterDataValueProviderPlugin {

	private final ResourceManager resourceManager;

	private final String fileName;
	private final Gson gson = new Gson();

	/**
	 * Constructor
	 * @param resourceManager resourceManager
	 * @param fileName the json file where masterdata values are stored
	 */
	@Inject
	public JsonMasterDataValueProvider(
			final ResourceManager resourceManager,
			final @ParamValue("fileName") String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		//---
		this.resourceManager = resourceManager;
		this.fileName = fileName;
	}

	/** {@inheritDoc} */
	@Override
	public MasterDataValues getValues() {
		final String jsonFileAsString = parseFile(resourceManager.resolve(fileName));
		return gson.fromJson(jsonFileAsString, MasterDataValues.class);

	}

	private static String parseFile(final URL url) {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error reading json file : '{0}'", url);
		}
	}

}
