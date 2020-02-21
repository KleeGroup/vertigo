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
package io.vertigo.dynamo.task.x;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.component.Component;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.dynamo.task.proxy.TaskAnnotation;
import io.vertigo.dynamo.task.proxy.TaskInput;
import io.vertigo.dynamo.task.proxy.TaskOutput;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;

@Transactional
public interface SuperHeroDao extends Component {
	@TaskAnnotation(
			name = "TkCarCount",
			request = "select count(*) from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DoInteger")
	int count();

	@TaskAnnotation(
			name = "TkSuperHeroLoadByName",
			request = "<%if (name !=null) {%>select * from super_hero where name = #name# <%} else {%>"
					+ "select * from super_hero <%}%>",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DoDtSuperHeroDtc")
	DtList<SuperHero> findAll(
			@TaskInput(name = "name", domain = "DoString") Optional<String> nameOpt);

	@TaskAnnotation(
			name = "TkSuperHeroCountByName",
			request = "select count(*) from super_hero where name=#name# ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DoInteger")
	int count(
			@TaskInput(name = "name", domain = "DoString") String manufacturer);

	@TaskAnnotation(
			name = "TkLoadSuperHeroNames",
			request = "select distinct name from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DoStrings")
	List<String> names();

	@TaskAnnotation(
			name = "TkUpdateSuperHeroNames",
			request = "update  super_hero set name =#newName# where name=#oldName#",
			taskEngineClass = TaskEngineProc.class)
	void update(
			@TaskInput(name = "oldName", domain = "DoString") String oldName,
			@TaskInput(name = "newName", domain = "DoString") String newName);

}
