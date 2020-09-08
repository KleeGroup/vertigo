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
package io.vertigo.core.node.config.yaml;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.BootConfigBuilder;
import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.node.config.LogConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.node.config.yaml.YamlAppConfig.YamlModuleConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.FileUtil;
import io.vertigo.core.util.Selector;
import io.vertigo.core.util.Selector.MethodConditions;

/**
 * Builds the NodeConfig for the current node based on the provided YamlAppConfig.
 * @author mlaroche
 *
 */
public final class YamlNodeConfigBuilder implements Builder<NodeConfig> {

	private static final String FLAGS = "__flags__";

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder();
	private final BootConfigBuilder bootConfigBuilder = BootConfig.builder();
	private final List<String> activeFlags;
	private final YamlConfigParams params;

	public YamlNodeConfigBuilder(final Properties params) {
		Assertion.check().isNotNull(params);
		//---
		if (params.containsKey("boot.activeFlags")) {
			activeFlags = Arrays.asList(params.getProperty("boot.activeFlags").split(";"));
			params.remove("boot.activeFlags");
		} else {
			activeFlags = Collections.emptyList();
		}
		this.params = new YamlConfigParams(params);
	}

	/**
	* Appends Config of a set of modules.
	* @param relativeRootClass Class used to access files in a relative way.
	* @param jsonFileNames fileNames of the different json files
	*
	* @return this builder
	*/
	public YamlNodeConfigBuilder withFiles(final Class relativeRootClass, final String... jsonFileNames) {
		Assertion.check()
				.isNotNull(relativeRootClass)
				.isNotNull(jsonFileNames);
		//---
		Stream.of(jsonFileNames)
				.map(xmlModulesFileName -> createURL(xmlModulesFileName, relativeRootClass))
				.forEach(this::handleJsonFileConfig);
		return this;
	}

	private void handleJsonFileConfig(final URL yamlConfigURL) {

		final Yaml yaml = new Yaml(new Constructor(YamlAppConfig.class));
		final YamlAppConfig yamlNodeConfig = yaml.loadAs(FileUtil.read(yamlConfigURL), YamlAppConfig.class);
		//--- node
		handleNodeConfig(yamlNodeConfig);
		//--- boot
		handleBoot(yamlNodeConfig);
		//--- modules
		yamlNodeConfig.modules
				.entrySet()
				.stream()
				.forEach(entry -> handleJsonModuleConfig(entry.getKey(), entry.getValue()));
		//--- initializers
		yamlNodeConfig.initializers
				.forEach(initializerConfig -> {
					Assertion.check()
							.isTrue(initializerConfig.size() == 1, "an initializer is defined by it's class");
					// ---
					final Map.Entry<String, Map<String, Object>> initializerEntry = initializerConfig.entrySet().iterator().next();
					if (isEnabledByFlag(getFlagsOfMapParams(initializerEntry.getValue()))) {
						nodeConfigBuilder.addInitializer(ClassUtil.classForName(initializerEntry.getKey(), ComponentInitializer.class));
					}
				});
	}

	private void handleNodeConfig(final YamlAppConfig yamlAppConfig) {
		if (yamlAppConfig.node != null) {
			final String appName = yamlAppConfig.node.appName;
			final String nodeId = yamlAppConfig.node.nodeId;
			final String endPoint = yamlAppConfig.node.endPoint;
			if (appName != null) {
				nodeConfigBuilder.withAppName(evalParamValue(appName));
			}
			if (nodeId != null) {
				nodeConfigBuilder.withNodeId(evalParamValue(nodeId));
			}
			if (endPoint != null) {
				nodeConfigBuilder.withEndPoint(evalParamValue(endPoint));
			}
		}
	}

	private void handleBoot(final YamlAppConfig yamlAppConfig) {
		if (yamlAppConfig.boot != null) {
			if (yamlAppConfig.boot.params != null) {
				final String locales = yamlAppConfig.boot.params.get("locales");
				final String defaultZoneId = yamlAppConfig.boot.params.get("defaultZoneId");
				if (locales != null) {
					if (defaultZoneId == null) {
						bootConfigBuilder
								.withLocales(locales);
					} else {
						bootConfigBuilder
								.withLocalesAndDefaultZoneId(locales, defaultZoneId);
					}
				}
			}
			yamlAppConfig.boot.plugins.forEach(
					plugin -> {
						Assertion.check()
								.isTrue(plugin.size() == 1, "a plugin is defined by it's class");
						// ---
						final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
						if (isEnabledByFlag(getFlagsOfMapParams(pluginEntry.getValue()))) {
							bootConfigBuilder
									.addPlugin(
											ClassUtil.classForName(pluginEntry.getKey(), Plugin.class),
											pluginEntry.getValue().entrySet().stream()
													.filter(entry -> !FLAGS.equals(entry.getKey()))
													.map(entry -> Param.of(entry.getKey(), evalParamValue(String.valueOf(entry.getValue()))))
													.toArray(Param[]::new));
						}
					});
		}
	}

	private void handleJsonModuleConfig(final String featuresClassName, final YamlModuleConfig yamlModuleConfig) {
		if (yamlModuleConfig == null) {
			// we have no params so no flag
			// just a simple module
			nodeConfigBuilder.addModule(ClassUtil.newInstance(featuresClassName, Features.class).build());
		} else {
			// more complexe module with flags and flipped features
			if (isEnabledByFlag(yamlModuleConfig.__flags__)) {
				final Features moduleConfigByFeatures = ClassUtil.newInstance(featuresClassName, Features.class);
				final Map<String, Method> featureMethods = Selector
						.from(moduleConfigByFeatures.getClass())
						.filterMethods(MethodConditions.annotatedWith(Feature.class))
						.findMethods()
						.stream()
						.map(Tuple::getVal2)
						.collect(Collectors.toMap(method -> method.getAnnotation(Feature.class).value(), Function.identity()));

				if (yamlModuleConfig.features != null) {
					yamlModuleConfig.features
							.forEach(featureConfig -> {
								Assertion.check()
										.isTrue(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.check()
										.isNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findMethodParameters(paramsMap, methodForFeature));
								}
							});
				}

				if (yamlModuleConfig.featuresConfig != null) {
					yamlModuleConfig.featuresConfig
							.forEach(featureConfig -> {
								Assertion.check()
										.isTrue(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.check()
										.isNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findMethodParameters(paramsMap, methodForFeature));
								}
							});
				}

				yamlModuleConfig.plugins.forEach(
						plugin -> {
							Assertion.check()
									.isTrue(plugin.size() == 1, "a plugin is defined by it's class");
							// ---
							final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
							final String pluginClassName = pluginEntry.getKey();
							final Map<String, Object> paramsMap = pluginEntry.getValue();
							if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
								moduleConfigByFeatures
										.addPlugin(
												ClassUtil.classForName(pluginClassName, Plugin.class),
												plugin.get(pluginClassName).entrySet().stream()
														.filter(entry -> !FLAGS.equals(entry.getKey()))
														.map(entry -> Param.of(entry.getKey(), String.valueOf(entry.getValue())))
														.toArray(Param[]::new));
							}
						});
				nodeConfigBuilder.addModule(moduleConfigByFeatures.build());
			}
		}
	}

	private static List<String> getFlagsOfMapParams(final Map<String, Object> paramsMap) {
		if (paramsMap == null || !paramsMap.containsKey(FLAGS)) {
			return Collections.emptyList();
		}
		// if contains we check we have a list
		Assertion.check()
				.isTrue(List.class.isAssignableFrom(paramsMap.get(FLAGS).getClass()), "flags are array of strings");
		return (List<String>) paramsMap.get(FLAGS);
	}

	private boolean isEnabledByFlag(final List<String> flags) {
		Assertion.check()
				.isNotNull(flags);
		//---
		if (flags.isEmpty()) {
			return true;// no flags declared means always
		}
		return flags.stream()
				.anyMatch(flag -> {
					Assertion.check()
							.isNotBlank(flag, "A flag cannot be empty");
					if (flag.charAt(0) == '!') {
						return !activeFlags.contains(flag.substring(1));
					}
					return activeFlags.contains(flag);
				});
	}

	private static Object[] findMethodParameters(final Map<String, Object> paramsConfig, final Method method) {
		Assertion.check()
				.isTrue(method.getParameterCount() <= 1, "A feature method can have 0 parameter or a single Param... parameter");
		if (method.getParameterCount() == 1) {
			if (paramsConfig == null) {
				return new Object[] { new Param[0] };
			}
			final Param[] params = paramsConfig.entrySet()
					.stream()
					.filter(paramEntry -> !FLAGS.equals(paramEntry.getKey()))
					.map(paramEntry -> Param.of(paramEntry.getKey(), String.valueOf(paramEntry.getValue())))
					.toArray(Param[]::new);
			return new Object[] { params };
		}
		return EMPTY_ARRAY;

	}

	/**
	 * @param logConfig Config of logs
	 * @return  this builder
	 */
	public YamlNodeConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.check().isNotNull(logConfig);
		//-----
		bootConfigBuilder.withLogConfig(logConfig);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public NodeConfig build() {
		return nodeConfigBuilder
				.withBoot(bootConfigBuilder.build())
				.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URL non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.check().isNotBlank(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.check().isNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	private String evalParamValue(final String paramValue) {
		if (paramValue.startsWith("${boot.") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return params.getParam(property);
		}
		return paramValue;
	}

}
