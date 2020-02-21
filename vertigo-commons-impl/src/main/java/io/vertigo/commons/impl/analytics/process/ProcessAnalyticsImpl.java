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
package io.vertigo.commons.impl.analytics.process;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vertigo.commons.analytics.process.AProcess;
import io.vertigo.commons.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.lang.Assertion;

public class ProcessAnalyticsImpl {

	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Stack<ProcessAnalyticsTracerImpl>> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	public void trace(final String category, final String name, final Consumer<ProcessAnalyticsTracer> consumer, final Consumer<AProcess> onCloseConsumer) {
		try (ProcessAnalyticsTracerImpl tracer = createTracer(category, name, onCloseConsumer)) {
			try {
				consumer.accept(tracer);
				tracer.markAsSucceeded();
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	public <O> O traceWithReturn(final String category, final String name, final Function<ProcessAnalyticsTracer, O> function, final Consumer<AProcess> onCloseConsumer) {
		try (ProcessAnalyticsTracerImpl tracer = createTracer(category, name, onCloseConsumer)) {
			try {
				final O result = function.apply(tracer);
				tracer.markAsSucceeded();
				return result;
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	public Optional<ProcessAnalyticsTracer> getCurrentTracer() {
		// When collect feature is enabled
		return doGetCurrentTracer().map(Function.identity()); // convert impl to api
	}

	private static Optional<ProcessAnalyticsTracerImpl> doGetCurrentTracer() {
		if (THREAD_LOCAL_PROCESS.get() == null || THREAD_LOCAL_PROCESS.get().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(THREAD_LOCAL_PROCESS.get().peek());
	}

	private static void push(final ProcessAnalyticsTracerImpl analyticstracer) {
		Assertion.checkNotNull(analyticstracer);
		//---
		if (THREAD_LOCAL_PROCESS.get() == null) {
			THREAD_LOCAL_PROCESS.set(new Stack<>());
		}
		Assertion.checkState(THREAD_LOCAL_PROCESS.get().size() < 100, "More than 100 process deep. All processes must be closed.");
		THREAD_LOCAL_PROCESS.get().push(analyticstracer);
	}

	private Optional<ProcessAnalyticsTracerImpl> removeCurrentAndGetParentTracer() {
		THREAD_LOCAL_PROCESS.get().pop();
		final Optional<ProcessAnalyticsTracerImpl> parentOpt = doGetCurrentTracer();
		if (!parentOpt.isPresent()) {
			THREAD_LOCAL_PROCESS.remove();
		}
		return parentOpt;
	}

	private ProcessAnalyticsTracerImpl createTracer(final String category, final String name, final Consumer<AProcess> onCloseConsumer) {
		final ProcessAnalyticsTracerImpl analyticsTracer = new ProcessAnalyticsTracerImpl(category, name, onCloseConsumer, this::removeCurrentAndGetParentTracer);
		push(analyticsTracer);
		return analyticsTracer;
	}

}
