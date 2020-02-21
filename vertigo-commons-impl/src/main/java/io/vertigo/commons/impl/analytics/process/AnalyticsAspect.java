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

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.process.Analytics;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * * Intercepteur de performance des composant.
 *
 * @author jmforhan
 */
public final class AnalyticsAspect implements Aspect {
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager the component responsible of managing analytics
	 */
	@Inject
	public AnalyticsAspect(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//---
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public Class<Analytics> getAnnotationType() {
		return Analytics.class;
	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation invocation) {
		//Aspect must be declared on methods or on the class.
		final Analytics analytics = invocation.getMethod().getAnnotation(Analytics.class) == null ? invocation.getMethod().getDeclaringClass().getAnnotation(Analytics.class)
				: invocation.getMethod().getAnnotation(Analytics.class);

		final String name = StringUtil.isEmpty(analytics.name()) ? invocation.getMethod().getDeclaringClass().getSimpleName() + "::" + invocation.getMethod().getName() : analytics.name();
		return analyticsManager.traceWithReturn(
				analytics.category(),
				name,
				tracer -> invocation.proceed(args));
	}
}
