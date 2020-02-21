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

import io.vertigo.studio.mda.MdaResultBuilder;

/**
 * Interface des generateurs de fichier.
 *
 * @author dchallas
 */
public interface FileGenerator {
	/**
	 * Génèration d'un fichier.
	 * Si le fichier existe déjà, il est regénéré
	 *
	 * @param mdaResultBuilder Builder
	 */
	void generateFile(final MdaResultBuilder mdaResultBuilder);

	/**
	 * Static method factory for FileGeneratorBuilder
	 * @param fileGeneratorConfig the config of the file generator
	 * @return FileGeneratorBuilder
	 */
	static FileGeneratorBuilder builder(final FileGeneratorConfig fileGeneratorConfig) {
		return new FileGeneratorBuilder(fileGeneratorConfig);
	}
}
