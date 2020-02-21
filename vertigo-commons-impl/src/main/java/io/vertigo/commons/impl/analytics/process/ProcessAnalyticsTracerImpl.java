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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.analytics.process.AProcess;
import io.vertigo.commons.analytics.process.AProcessBuilder;
import io.vertigo.commons.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.lang.Assertion;

/**
 * A tracer collectes information durint the execution of a process.
 * @author npiedeloup
 */
final class ProcessAnalyticsTracerImpl implements ProcessAnalyticsTracer, AutoCloseable {
	private final Logger logger;

	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Consumer<AProcess> consumer;
	private final Supplier<Optional<ProcessAnalyticsTracerImpl>> parentOptSupplier;
	private final AProcessBuilder processBuilder;

	/**
	 * Constructor.
	 * @param parentOpt Optional Parent of this tracer
	 * @param category the category where the process is stored
	 * @param name the name that identified the process
	 * @param consumer Consumer of this process after closing
	 */
	ProcessAnalyticsTracerImpl(
			final String category,
			final String name,
			final Consumer<AProcess> consumer,
			final Supplier<Optional<ProcessAnalyticsTracerImpl>> parentOptSupplier) {
		Assertion.checkArgNotEmpty(category);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(consumer);
		//---
		logger = LogManager.getLogger(category);
		this.consumer = consumer;
		this.parentOptSupplier = parentOptSupplier;

		processBuilder = AProcess.builder(category, name);
		if (logger.isDebugEnabled()) {
			logger.debug("Start {}", name);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer incMeasure(final String name, final double value) {
		processBuilder.incMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer setMeasure(final String name, final double value) {
		processBuilder.setMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer addTag(final String name, final String value) {
		processBuilder.addTag(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		if (succeeded != null) {
			setMeasure("success", succeeded ? 100 : 0);
		}
		if (causeException != null) {
			addTag("exception", causeException.getClass().getName());
		}
		final AProcess process = processBuilder.build();

		final Optional<ProcessAnalyticsTracerImpl> parentOpt = parentOptSupplier.get();
		if (parentOpt.isPresent()) {
			//when the current process is a subProcess, it's finished and must be added to the parent
			parentOpt.get().processBuilder.addSubProcess(process);
		} else {
			//when the current process is the root process, it's finished and must be sent to the connector
			consumer.accept(process);
		}
		logProcess(process);
	}

	private void logProcess(final AProcess process) {
		if (logger.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder()
					.append("Finish ")
					.append(process.getName())
					.append(succeeded != null ? (succeeded ? " successfully" : " with error") : "with internal error")
					.append(" in ( ")
					.append(process.getDurationMillis())
					.append(" ms)");
			if (!process.getMeasures().isEmpty()) {
				sb.append(" measures:").append(process.getMeasures());
			}
			if (!process.getTags().isEmpty()) {
				sb.append(" metaData:").append(process.getTags());
			}
			logger.info(sb.toString());
		}
	}

	/**
	 * Marks this tracer as succeeded.
	 * @return this tracer
	 */
	ProcessAnalyticsTracer markAsSucceeded() {
		//the last mark wins
		//so we prefer to reset causeException
		causeException = null;
		succeeded = true;
		return this;
	}

	/**
	 * Marks this tracer as Failed.
	 * @return this tracer
	 */
	ProcessAnalyticsTracer markAsFailed(final Throwable t) {
		//We don't check the nullability of e
		//the last mark wins
		//so we prefer to put the flag 'succeeded' to false
		succeeded = false;
		causeException = t;
		return this;
	}
}
