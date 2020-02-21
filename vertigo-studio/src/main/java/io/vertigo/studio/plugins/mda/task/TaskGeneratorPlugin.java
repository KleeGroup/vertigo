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
package io.vertigo.studio.plugins.mda.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.task.model.DAOModel;
import io.vertigo.studio.plugins.mda.task.model.PAOModel;
import io.vertigo.studio.plugins.mda.task.model.TaskAttributeModel;
import io.vertigo.studio.plugins.mda.task.model.TaskDefinitionModel;
import io.vertigo.studio.plugins.mda.util.MdaUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Task.
 *
 * @author pchretien
 */
public final class TaskGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDirOpt Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public TaskGeneratorPlugin(@ParamValue("targetSubDir") final Optional<String> targetSubDirOpt) {
		//-----
		targetSubDir = targetSubDirOpt.orElse("javagen");
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generatePaos(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		generateDaos(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	/**
	 * Génération de tous les PAOs.
	 */
	private static void generatePaos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		//On liste des taches regroupées par Package.
		for (final Entry<String, List<TaskDefinition>> entry : buildPackageMap().entrySet()) {
			final Collection<TaskDefinition> taskDefinitionCollection = entry.getValue();
			if (!taskDefinitionCollection.isEmpty()) {
				final String packageName = entry.getKey();
				generatePao(targetSubDir, fileGeneratorConfig, mdaResultBuilder, taskDefinitionCollection, packageName);
			}
		}
	}

	/**
	 * Génération de tous les DAOs.
	 */
	private static void generateDaos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		for (final Entry<DtDefinition, List<TaskDefinition>> entry : builDtDefinitiondMap().entrySet()) {
			final DtDefinition dtDefinition = entry.getKey();
			if (dtDefinition.isPersistent()) {
				//Si DAO est persitant on génère son CRUD.
				generateDao(targetSubDir, fileGeneratorConfig, mdaResultBuilder, dtDefinition, entry.getValue());
			}
		}
	}

	/**
	 * Génération d'un DAO c'est à dire des taches afférentes à un objet.
	 */
	private static void generateDao(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final DtDefinition dtDefinition,
			final Collection<TaskDefinition> taskDefinitions) {
		final DAOModel daoModel = new DAOModel(fileGeneratorConfig, dtDefinition, taskDefinitions);

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("dao", daoModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(daoModel.getClassSimpleName() + ".java")
				.withGenSubDir(targetSubDir)
				.withPackageName(daoModel.getPackageName())
				.withTemplateName("task/template/dao.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 *  Génération d'un PAO c'est à dire des taches afférentes à un package.
	 */
	private static void generatePao(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final Collection<TaskDefinition> taskDefinitionCollection,
			final String packageName) {
		final PAOModel paoModel = new PAOModel(fileGeneratorConfig, taskDefinitionCollection, packageName);

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("pao", paoModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(paoModel.getClassSimpleName() + ".java")
				.withGenSubDir(targetSubDir)
				.withPackageName(paoModel.getPackageName())
				.withTemplateName("task/template/pao.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 * Stratégie pour savoir si une tache est PAO ou DAO.
	 * Si la DT est non null DAO sinon PAO.
	 */
	private static DtDefinition getDtDefinition(final TaskDefinitionModel templateTaskDefinition) {
		if (templateTaskDefinition.isOut()) {
			//si out on regarde si en sortie on a un DTO ou une DTC typé.
			final Domain outDomain = templateTaskDefinition.getOutAttribute().getDomain();
			if (outDomain.getScope().isDataObject()) {
				return outDomain.getDtDefinition();
			}
			return null;
		}
		//there is no OUT param
		//We are searching igf there is an no-ambiguous IN param defined as a DataObject(DTO or DTC)
		final List<Domain> candidates = templateTaskDefinition.getInAttributes()
				.stream()
				.map(TaskAttributeModel::getDomain)
				.filter(domain -> domain.getScope().isDataObject())
				.collect(Collectors.toList());
		//There MUST be only ONE candidate
		if (candidates.size() == 1) {
			return candidates.get(0).getDtDefinition();
		}
		//Ambiguosity => PAO
		return null;
	}

	private static Map<String, List<TaskDefinition>> buildPackageMap() {
		final Collection<TaskDefinition> taskDefinitions = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<String, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();
		//---
		for (final TaskDefinition taskDefinition : taskDefinitions) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);
			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			// Correction bug : task avec retour DtObject (non persistant) non générée
			//Les taches sont générées dans les pao
			// - si il n'esxiste pas de définition associées à la tache
			// - ou si la définition est considérée comme non persistante.
			final boolean pao = dtDefinition == null || !dtDefinition.isPersistent();
			if (pao) {
				//La tache est liée au package. (PAO)
				final List<TaskDefinition> list = taskDefinitionsMap
						.computeIfAbsent(taskDefinition.getPackageName(), k -> new ArrayList<>());
				//on ajoute la tache aux taches du package.
				list.add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}

	private static Map<DtDefinition, List<TaskDefinition>> builDtDefinitiondMap() {
		final Collection<TaskDefinition> taskDefinitions = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<DtDefinition, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();

		//---
		//Par défaut, On crée pour chaque DT une liste vide des taches lui étant associées.
		final Collection<DtDefinition> dtDefinitions = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class);
		for (final DtDefinition dtDefinition : dtDefinitions) {
			taskDefinitionsMap.put(dtDefinition, new ArrayList<TaskDefinition>());
		}
		//---
		for (final TaskDefinition taskDefinition : taskDefinitions) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);

			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			final boolean dao = dtDefinition != null;
			if (dao) {
				//Dans le cas d'un DTO ou DTC en sortie on considère que la tache est liée au DAO.
				taskDefinitionsMap.get(dtDefinition).add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}

	@Override
	public void clean(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		MdaUtil.deleteFiles(new File(fileGeneratorConfig.getTargetGenDir() + targetSubDir), mdaResultBuilder);
	}
}
