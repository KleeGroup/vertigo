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
package io.vertigo.dynamo.task;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;

/**
 * Manages the execution of local (transactional) tasks.
 * @author pchretien
 */
public interface TaskManager extends Manager {
	/**
	 * Execution of a task.
	 * This execution is done in the current thread.
	 * So this execution can be transactional.
	 *
	 * @param task Task
	 * @return TaskResult
	 */
	TaskResult execute(Task task);
}
