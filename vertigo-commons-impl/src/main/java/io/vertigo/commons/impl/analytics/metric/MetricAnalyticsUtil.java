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
package io.vertigo.commons.impl.analytics.metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.analytics.metric.MetricDefinition;
import io.vertigo.commons.analytics.metric.Metrics;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * Implémentation de ReportingManager.
 *
 * @author pchretien
 */
public final class MetricAnalyticsUtil {

	private static final String pluginCounterChar = "#";// char used in plugins for counting them plugin#1, plugin#2

	private MetricAnalyticsUtil() {
		//private
	}

	/**
	 * Registers all methods annotated with @Metrics
	 */
	public static List<MetricDefinition> createMetricDefinitions(final String componentId, final Component component, final AopPlugin aopPlugin) {
		Assertion.checkNotNull(component);

		//-- we construct a map of feature by componentId
		final Map<String, String> featureByComponentId = new HashMap<>();
		Home.getApp().getNodeConfig().getModuleConfigs()
				.forEach(moduleConfig -> moduleConfig.getComponentConfigs()
						.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), moduleConfig.getName())));
		//-----
		//1. search all methods
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(Metrics.class))
				.map(method -> {
					Assertion.checkArgument(List.class.isAssignableFrom(method.getReturnType()), "metrics supplier methods of class {0} must return a List of Metric instead of {1}", component.getClass(), method.getReturnType());
					Assertion.checkArgument(method.getParameterTypes().length == 0, "metrics supplier methods of class {0} must not have any parameter", component.getClass());
					//-----
					//2. For each method register a listener
					// we remove # because it doesn't comply with definition naming rule
					final String metricDefinitionName = "Met" + StringUtil.first2UpperCase(componentId.replaceAll(pluginCounterChar, "")) + "$" + method.getName();
					return new MetricDefinition(
							metricDefinitionName,
							() -> (List<Metric>) ClassUtil.invoke(component, method));
				})
				.collect(Collectors.toList());

	}

	public static List<Metric> getMetrics() {
		return Home.getApp().getDefinitionSpace().getAll(MetricDefinition.class).stream()
				.flatMap(metricDefinition -> metricDefinition.getMetricSupplier().get().stream())
				.collect(Collectors.toList());
	}

}
