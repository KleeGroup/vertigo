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
package io.vertigo.studio.plugins.mda;

import java.io.File;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class provides a way to create a FileFenerator.
 * @author pchretien
 */
public final class FileGeneratorBuilder implements Builder<FileGenerator> {
	private final FileGeneratorConfig fileGeneratorConfig;
	private Map<String, Object> myModel;
	private String myFileName;
	private String myPackageName;
	private String myTemplateName;
	private String myGenSubDir;

	/**
	 * @param fileGeneratorConfig the config of the file generator
	*/
	FileGeneratorBuilder(final FileGeneratorConfig fileGeneratorConfig) {
		Assertion.checkNotNull(fileGeneratorConfig);
		//---
		this.fileGeneratorConfig = fileGeneratorConfig;
	}

	/**
	 * @param model the model
	 * @return this builder
	 */
	public FileGeneratorBuilder withModel(final Map<String, Object> model) {
		Assertion.checkNotNull(model);
		//---
		myModel = model;
		return this;
	}

	/**
	 * @param fileName the name of the file including extension
	 * @return this builder
	 */
	public FileGeneratorBuilder withFileName(final String fileName) {
		Assertion.checkNotNull(fileName);
		//---
		myFileName = fileName;
		return this;
	}

	/**
	 * @param packageName the name of the package
	 * @return this builder
	 */
	public FileGeneratorBuilder withPackageName(final String packageName) {
		Assertion.checkNotNull(packageName);
		//---
		myPackageName = packageName;
		return this;
	}

	/**
	 * @param templateName the name of the template
	 * @return this builder
	 */
	public FileGeneratorBuilder withTemplateName(final String templateName) {
		Assertion.checkNotNull(templateName);
		//---
		myTemplateName = templateName;
		return this;
	}

	/**
	 * @param genSubDir Nom subdir de génération
	 * @return this builder
	 */
	public FileGeneratorBuilder withGenSubDir(final String genSubDir) {
		Assertion.checkNotNull(genSubDir);
		//---
		myGenSubDir = genSubDir;
		return this;
	}

	@Override
	public FileGenerator build() {
		Assertion.checkNotNull(myModel, "a model is required");
		Assertion.checkNotNull(myFileName, "a file name is required");
		Assertion.checkNotNull(myPackageName, "a package is required");
		Assertion.checkNotNull(myTemplateName, "a template is required");
		Assertion.checkNotNull(myGenSubDir, "a sub directory is required");
		//---
		final String filePath = buildFilePath();
		return new FileGeneratorFreeMarker(myModel, filePath, myTemplateName, fileGeneratorConfig.getEncoding(), fileGeneratorConfig.getClass());
	}

	private String buildFilePath() {
		final String directoryPath = fileGeneratorConfig.getTargetGenDir() + myGenSubDir + File.separatorChar + package2directory(myPackageName) + File.separatorChar;
		return directoryPath + myFileName;
	}

	private static String package2directory(final String packageName) {
		return packageName.replace('.', File.separatorChar).replace('\\', File.separatorChar);
	}
}
