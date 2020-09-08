/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
/**
 *
 */
package io.vertigo.core.impl.analytics.health;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.analytics.health.definitions.HealthCheckDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.StringUtil;

/**
 * HealthAnalyticsUtil.
 *
 * @author jmforhan, mlaroche
 */
public final class HealthAnalyticsUtil {

	private static final String PLUGIN_COUNTER_CHAR = "#";// char used in plugins for counting them plugin#1, plugin#2

	private HealthAnalyticsUtil() {
		//private constructor for util classes
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param componentId componentId to check
	 * @param component Component to check
	 * @param aopPlugin Aop plugin use for unwrap
	 * @return List of HealthCheckDefinition
	 */
	public static List<HealthCheckDefinition> createHealthCheckDefinitions(final String componentId, final CoreComponent component, final AopPlugin aopPlugin) {
		Assertion.check().isNotNull(component);

		//-- we construct a map of feature by componentId
		final Map<String, String> featureByComponentId = new HashMap<>();
		Node.getNode().getNodeConfig().getBootConfig().getComponentConfigs()
				.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), "vertigo-boot"));

		Node.getNode().getNodeConfig().getModuleConfigs()
				.forEach(moduleConfig -> moduleConfig.getComponentConfigs()
						.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), moduleConfig.getName())));
		//-----
		//1. search all methods
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(HealthChecked.class))
				.map(method -> {
					final HealthChecked healthChecked = method.getAnnotation(HealthChecked.class);
					Assertion.check()
							.isTrue(HealthMeasure.class.equals(method.getReturnType()), "health check methods of class {0} must return a HealthMeasure instead of {1}", component.getClass(), method.getReturnType())
							.isTrue(method.getName().startsWith("check"), "health check methods of class {0} must start with check", component.getClass())
							.isTrue(method.getParameterTypes().length == 0, "health check methods of class {0} must not have any parameter", component.getClass());
					//-----
					//2. For each method register a listener
					// we remove # because it doesn't comply with definition naming rule
					final String healthCheckDefinitionName = "Hchk" + StringUtil.first2UpperCase(componentId.replaceAll(PLUGIN_COUNTER_CHAR, "")) + "$" + method.getName();
					return new HealthCheckDefinition(
							healthCheckDefinitionName,
							healthChecked.name(),
							componentId,
							featureByComponentId.get(componentId),
							healthChecked.feature(),
							() -> (HealthMeasure) ClassUtil.invoke(component, method));
				})
				.collect(Collectors.toList());

	}

	public static List<HealthCheck> getHealthChecks() {
		return Node.getNode().getDefinitionSpace().getAll(HealthCheckDefinition.class).stream()
				.map(HealthAnalyticsUtil::buildHealthCheck)
				.collect(Collectors.toList());
	}

	private static HealthCheck buildHealthCheck(final HealthCheckDefinition healthCheckDefinition) {
		HealthMeasure healthMeasure;
		try {
			healthMeasure = healthCheckDefinition.getCheckMethod().get();
		} catch (final Exception e) {
			healthMeasure = HealthMeasure.builder()
					.withRedStatus("Impossible to get status", e)
					.build();
		}
		return new HealthCheck(
				healthCheckDefinition.getHealthCheckName(),
				healthCheckDefinition.getChecker(),
				healthCheckDefinition.getModule(),
				healthCheckDefinition.getFeature(),
				Instant.now(),
				healthMeasure);
	}

	public static HealthStatus aggregate(final List<HealthCheck> healthChecks) {
		Assertion.check().isNotNull(healthChecks);
		//---
		int nbGreen = 0;
		int nbYellow = 0;
		int nbRed = 0;
		for (final HealthCheck healthCheck : healthChecks) {
			switch (healthCheck.getMeasure().getStatus()) {
				case GREEN:
					nbGreen++;
					break;
				case YELLOW:
					nbYellow++;
					break;
				case RED:
					nbRed++;
					break;
				default:
					break;
			}
		}
		return generateStatus(nbGreen, nbYellow, nbRed);
	}

	private static HealthStatus generateStatus(
			final int nbGreen,
			final int nbYellow,
			final int nbRed) {
		if (nbRed > 0) {
			return HealthStatus.RED;
		} else if (nbYellow > 0) {
			return HealthStatus.YELLOW;
		}
		return HealthStatus.GREEN;
	}
}
