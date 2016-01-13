/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.work;

/**
 * Work processor used for composing multiple WorkEngine.
 * Example : F o G o H (x) composition mean : F(G(H(x)))
 * @author pchretien
 *
 * @param <R> result
 * @param <W> work
 */
public interface WorkProcessor<R, W> {
	/**
	 * Add a task to the current processor to build a new processor
	 * @param workEngineProvider WorkEngine provider
	 * @param <S> WorkEngine result
	 * @return new WorkProcessor
	 */
	<S> WorkProcessor<S, W> then(final WorkEngineProvider<S, R> workEngineProvider);

	/**
	 * Add a task to the current processor to build a new processor
	 * @param clazz Class of workEngine
	 * @param <S> WorkEngine result
	 * @return new WorkProcessor
	 */
	<S> WorkProcessor<S, W> then(final Class<? extends WorkEngine<S, R>> clazz);

	/**
	 * Execute processor composed of tasks.
	 * @param input Input param
	 * @return output
	 */
	R exec(W input);
}
