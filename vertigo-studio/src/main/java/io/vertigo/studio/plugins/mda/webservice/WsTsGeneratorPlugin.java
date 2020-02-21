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
package io.vertigo.studio.plugins.mda.webservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.util.MdaUtil;
import io.vertigo.studio.plugins.mda.webservice.model.WebServiceDefinitionModelTs;
import io.vertigo.studio.plugins.mda.webservice.model.WebServiceInitializerModelTs;
import io.vertigo.util.MapBuilder;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

/**
 * Generation des objets relatifs au module Vega.
 * @author npiedeloup
 */
public final class WsTsGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDirOpt Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public WsTsGeneratorPlugin(@ParamValue("targetSubDir") final Optional<String> targetSubDirOpt) {
		//-----
		targetSubDir = targetSubDirOpt.orElse("tsgen");
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generateRoute(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	private static Collection<WebServiceDefinition> getWebServiceDefinitions() {
		return Home.getApp().getDefinitionSpace().getAll(WebServiceDefinition.class);
	}

	private static void generateRoute(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final Collection<WebServiceDefinition> webServiceDefinitions = getWebServiceDefinitions();
		if (!webServiceDefinitions.isEmpty()) {
			final Map<String, List<WebServiceDefinitionModelTs>> webServicesPerFacades = new HashMap<>();
			for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
				final String facadeName = webServiceDefinition.getMethod().getDeclaringClass().getSimpleName().replaceAll("WebServices", "");
				List<WebServiceDefinitionModelTs> facadeWebServiceDefinitions = webServicesPerFacades.get(facadeName);
				if (facadeWebServiceDefinitions == null) {
					facadeWebServiceDefinitions = new ArrayList<>();
					webServicesPerFacades.put(facadeName, facadeWebServiceDefinitions);
				}
				facadeWebServiceDefinitions.add(new WebServiceDefinitionModelTs(webServiceDefinition));
			}

			final Map<String, List<WebServiceInitializerModelTs>> facadeByPackage = new HashMap<>();
			for (final Map.Entry<String, List<WebServiceDefinitionModelTs>> entry : webServicesPerFacades.entrySet()) {
				final String packageName = entry.getValue().get(0).getFunctionnalPackageName();
				final String simpleClassName = entry.getKey();
				final String jsFileNameWithoutExtension = JsFileNameUtil.convertCamelCaseToJsCase(simpleClassName);
				final Set<String> importList = new HashSet<>();
				final List<WebServiceDefinitionModelTs> routeList = entry.getValue();
				for (final WebServiceDefinitionModelTs route : routeList) {
					importList.addAll(route.getImportList());
				}

				if (!facadeByPackage.containsKey(packageName)) {
					facadeByPackage.put(packageName, new ArrayList<WebServiceInitializerModelTs>());
				}
				facadeByPackage.get(packageName).add(new WebServiceInitializerModelTs(jsFileNameWithoutExtension, simpleClassName));

				final Map<String, Object> model = new MapBuilder<String, Object>()
						.put("routes", entry.getValue())
						.put("importList", importList)
						.build();

				FileGenerator.builder(fileGeneratorConfig)
						.withModel(model)
						.withFileName(jsFileNameWithoutExtension + ".ts")
						.withGenSubDir(targetSubDir)
						.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + packageName + ".services.generated")
						.withTemplateName("webservice/template/routejsts.ftl")
						.build()
						.generateFile(mdaResultBuilder);

			}

			for (final Map.Entry<String, List<WebServiceInitializerModelTs>> entry : facadeByPackage.entrySet()) {
				final Map<String, Object> model = new MapBuilder<String, Object>()
						.put("serviceList", entry.getValue())
						.build();

				FileGenerator.builder(fileGeneratorConfig)
						.withModel(model)
						.withFileName("service-gen-initializer.ts")
						.withGenSubDir(targetSubDir)
						.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + entry.getKey() + ".initializer.generated")
						.withTemplateName("webservice/template/service-initializer.ftl")
						.build()
						.generateFile(mdaResultBuilder);

				FileGenerator.builder(fileGeneratorConfig)
						.withModel(model)
						.withFileName("service-type.ts")
						.withGenSubDir(targetSubDir)
						.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + entry.getKey() + ".services.generated")
						.withTemplateName("webservice/template/service-type.ftl")
						.build()
						.generateFile(mdaResultBuilder);
			}
		}
	}

	@Override
	public void clean(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		MdaUtil.deleteFiles(new File(fileGeneratorConfig.getTargetGenDir() + targetSubDir), mdaResultBuilder);
	}
}
