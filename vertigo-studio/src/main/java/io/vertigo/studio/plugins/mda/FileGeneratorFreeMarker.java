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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.studio.mda.MdaResultBuilder;

/**
 * Génération des fichiers avec FreeMarker.
 *
 * @author dchallas
 */
final class FileGeneratorFreeMarker implements FileGenerator {
	private static final Logger LOG = LogManager.getLogger(FileGeneratorFreeMarker.class);
	private static final String EOL = System.getProperty("line.separator");

	/**
	 * Répertoire des fichiers TOUJOURS générés
	 * Doit être renseigné dans le fichier properties [targetDir]
	 */
	private final Map<String, Object> model;
	private final String filePath;
	private final String templateName;
	private final String encoding;
	private final Configuration configuration;

	/**
	 * Constructeur.
	 *
	 * @param model the model to use with the template
	 * @param filePath the name of the path + file including extension (.sql, .java, .js...)
	 * @param templateName the name of the template
	 * @param encoding Encoding use
	 * @param referenceClass ReferenceClass for ftl loading
	 */
	FileGeneratorFreeMarker(final Map<String, Object> model, final String filePath, final String templateName, final String encoding, final Class<?> referenceClass) {
		Assertion.checkNotNull(model);
		Assertion.checkNotNull(filePath);
		Assertion.checkNotNull(templateName);
		Assertion.checkArgNotEmpty(encoding);
		Assertion.checkNotNull(referenceClass);
		//-----
		this.model = model;
		this.filePath = filePath;
		this.templateName = templateName;
		this.encoding = encoding;
		configuration = initConfiguration(referenceClass);
	}

	/**
	 * @param referenceClass Class de référence du template
	 * @return Configuration de FreeMarker
	 */
	private static Configuration initConfiguration(final Class<?> referenceClass) {
		final Configuration config = new Configuration();
		setTemplateLoading(config, referenceClass);
		config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		return config;
	}

	private static void setTemplateLoading(final Configuration config, final Class<?> referenceClass) {
		config.setClassForTemplateLoading(referenceClass, "");
	}

	/** {@inheritDoc} */
	@Override
	public void generateFile(final MdaResultBuilder mdaResultBuilder) {
		final File file = new File(filePath);
		try {
			generateFile(mdaResultBuilder, file);
		} catch (final Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	private void generateFile(final MdaResultBuilder mdaResultBuilder, final File file2create) throws IOException, TemplateException {
		// On crée le répertoire
		final File directory2create = file2create.getParentFile();
		directory2create.mkdirs();
		// Cette ligne doit se trouver avant le writer car si le fichier est
		// deja ouvert en ecriture il y a pb, on ne peut pas lire le code
		// deja existant
		final String content = buildContentFile();
		// pour optimisation de l'écriture et de la compilation,
		// on vérifie qu'on ne réécrit pas exactement la même chose que ce
		// qu'il y a déjà
		final String currentContent = readContentFile(file2create, encoding);
		if (content.equals(currentContent)) {
			// Les deux fichiers sont identiques
			mdaResultBuilder.addIdenticalFile(file2create);
		} else {
			// Si le contenu est différent on réécrit le fichier.
			final boolean success = writeFile(file2create, content, encoding);

			if (success) {
				if (currentContent == null) {
					mdaResultBuilder.addCreatedFile(file2create);
				} else {
					mdaResultBuilder.addUpdatedFile(file2create);
				}
			} else {
				mdaResultBuilder.addErrorFile(file2create);
			}
		}
	}

	/**
	 * Crée le contenu d'un fichier.
	 *
	 * @return Contenu du fichier
	 */
	private String buildContentFile() throws IOException, TemplateException {
		// Si le fichier existe on le remplace par le fichier créé.
		// Si le fichier n'existe pas on en crée un.
		final StringWriter writer = new StringWriter(); // Il est inutile de fermer une StringWriter.
		// Génération du contenu du fichier.
		// Vertigo étant en UTF-8, les fichiers ftl doivent être lu en UTF-8
		final Template template = configuration.getTemplate(templateName, "UTF-8");
		template.process(model, writer);
		return writer.toString();
	}

	/**
	 * Writes a file.
	 *
	 * @param file Fichier.
	 * @param content Contenu à écrire
	 * @param encoding encoding du fichier à écrire
	 * @return Si l'écriture s'est bien passée
	 */
	private static boolean writeFile(final File file, final String content, final String encoding) {
		try (final OutputStream fos = Files.newOutputStream(file.toPath());
				final OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
				final Writer writer = new BufferedWriter(osw)) {
			writer.write(content);
			return true;
		} catch (final IOException e) {
			LOG.error("Can't writeFile", e);
			return false;
		}
	}

	/**
	 * Reads a file.
	 *
	 * @param file Fichier
	 * @param encoding encoding du fichier à lire
	 * @return Contenu
	 * @throws IOException Erreur d'entrée/sortie
	 */
	private static String readContentFile(final File file, final String encoding) throws IOException {
		if (!file.exists()) {
			return null;
		}
		final StringBuilder currentContent = new StringBuilder();
		try (final InputStream fis = Files.newInputStream(file.toPath());
				final InputStreamReader isr = new InputStreamReader(fis, encoding);
				final BufferedReader myReader = new BufferedReader(isr)) {
			String line = myReader.readLine();
			while (line != null) {
				currentContent.append(line);
				currentContent.append(EOL);
				line = myReader.readLine();
			}
		}
		return currentContent.toString();
	}
}
