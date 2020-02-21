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
package io.vertigo.app.config.yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.app.config.yaml.YamlAppConfig.YamlModuleConfig;
import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Tuple;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.MethodConditions;

public final class YamlAppConfigBuilder implements Builder<NodeConfig> {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder();

	private final List<String> activeFlags;
	private final YamlConfigParams params;

	public YamlAppConfigBuilder(final Properties params) {
		Assertion.checkNotNull(params);
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
	 * Begin the boot config of the app.
	 * @return the bootConfig builder
	 */
	public BootConfigBuilder beginBoot() {
		return nodeConfigBuilder.beginBoot();
	}

	/**
	* Append Config of a set of modules.
	 * @param relativeRootClass Class used to access files in a relative way.
	* @param params properties used to configure the app
	* @param jsonFileNames fileNames of the different json files
	*
	* @return this builder
	*/
	public YamlAppConfigBuilder withFiles(final Class relativeRootClass, final String... jsonFileNames) {
		Assertion.checkNotNull(relativeRootClass);
		Assertion.checkNotNull(jsonFileNames);
		//---
		Stream.of(jsonFileNames)
				.map(xmlModulesFileName -> createURL(xmlModulesFileName, relativeRootClass))
				.forEach(jsonConfigUrl -> handleJsonFileConfig(jsonConfigUrl));
		return this;
	}

	private void handleJsonFileConfig(final URL yamlConfigURL) {

		final Yaml yaml = new Yaml(new Constructor(YamlAppConfig.class));
		final YamlAppConfig yamlNodeConfig = yaml.loadAs(parseFile(yamlConfigURL), YamlAppConfig.class);
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
					Assertion.checkState(initializerConfig.size() == 1, "an initializer is defined by it's class");
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
						nodeConfigBuilder
								.beginBoot()
								.withLocales(locales);
					} else {
						nodeConfigBuilder
								.beginBoot()
								.withLocalesAndDefaultZoneId(locales, defaultZoneId);
					}
				}
			}
			yamlAppConfig.boot.plugins.forEach(
					plugin -> {
						Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
						// ---
						final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
						if (isEnabledByFlag(getFlagsOfMapParams(pluginEntry.getValue()))) {
							nodeConfigBuilder.beginBoot()
									.addPlugin(
											ClassUtil.classForName(pluginEntry.getKey(), Plugin.class),
											pluginEntry.getValue().entrySet().stream()
													.filter(entry -> !"__flags__".equals(entry.getKey()))
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
				final Map<String, Method> featureMethods = new Selector().from(moduleConfigByFeatures.getClass())
						.filterMethods(MethodConditions.annotatedWith(Feature.class))
						.findMethods()
						.stream()
						.map(Tuple::getVal2)
						.collect(Collectors.toMap(method -> method.getAnnotation(Feature.class).value(), Function.identity()));

				if (yamlModuleConfig.features != null) {
					yamlModuleConfig.features
							.forEach(featureConfig -> {
								Assertion.checkState(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.checkNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findmethodParameters(paramsMap, methodForFeature, featureClassName, featuresClassName));
								}
							});
				}

				if (yamlModuleConfig.featuresConfig != null) {
					yamlModuleConfig.featuresConfig
							.forEach(featureConfig -> {
								Assertion.checkState(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.checkNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findmethodParameters(paramsMap, methodForFeature, featureClassName, featuresClassName));
								}
							});
				}

				yamlModuleConfig.plugins.forEach(
						plugin -> {
							Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
							// ---
							final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
							final String pluginClassName = pluginEntry.getKey();
							final Map<String, Object> paramsMap = pluginEntry.getValue();
							if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
								moduleConfigByFeatures
										.addPlugin(
												ClassUtil.classForName(pluginClassName, Plugin.class),
												plugin.get(pluginClassName).entrySet().stream()
														.filter(entry -> !"__flags__".equals(entry.getKey()))
														.map(entry -> Param.of(entry.getKey(), String.valueOf(entry.getValue())))
														.toArray(Param[]::new));
							}
						});
				nodeConfigBuilder.addModule(moduleConfigByFeatures.build());
			}
		}
	}

	private static List<String> getFlagsOfMapParams(final Map<String, Object> paramsMap) {
		if (paramsMap == null || !paramsMap.containsKey("__flags__")) {
			return Collections.emptyList();
		}
		// if contains we check we have a list
		Assertion.checkState(List.class.isAssignableFrom(paramsMap.get("__flags__").getClass()), "flags are array of strings");
		return (List<String>) paramsMap.get("__flags__");
	}

	private boolean isEnabledByFlag(final List<String> flags) {
		Assertion.checkNotNull(flags);
		//---
		if (flags.isEmpty()) {
			return true;// no flags declared means always
		}
		return flags.stream()
				.anyMatch(flag -> {
					Assertion.checkArgNotEmpty(flag, "A flag cannot be empty");
					if (flag.charAt(0) == '!') {
						return !activeFlags.contains(flag.substring(1));
					}
					return activeFlags.contains(flag);
				});
	}

	private static Object[] findmethodParameters(final Map<String, Object> paramsConfig, final Method method, final String featureName, final String featuresClassName) {
		Assertion.checkState(method.getParameterCount() <= 1, "A feature method can have 0 parameter or a single Param... parameter");
		if (method.getParameterCount() == 1) {
			if (paramsConfig == null) {
				return new Object[] { new Param[0] };
			}
			final Param[] params = paramsConfig.entrySet()
					.stream()
					.filter(paramEntry -> !"__flags__".equals(paramEntry.getKey()))
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
	public YamlAppConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		nodeConfigBuilder.beginBoot().withLogConfig(logConfig).endBoot();
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public NodeConfig build() {
		return nodeConfigBuilder.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URL non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	private static String parseFile(final URL url) {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error reading json file : '{0}'", url);
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
