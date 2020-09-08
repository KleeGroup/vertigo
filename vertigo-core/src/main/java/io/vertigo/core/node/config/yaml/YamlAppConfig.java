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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlAppConfig {

	public YamlNodeConfig node;
	public YamlBootConfig boot;
	public final Map<String, YamlModuleConfig> modules = new LinkedHashMap<>();
	public final List<YamlInitializerConfig> initializers = new ArrayList<>();

	public static final class YamlNodeConfig {
		public String appName;
		public String nodeId;
		public String endPoint;
	}

	public static final class YamlBootConfig {
		public YamlParamsConfig params;
		public final List<YamlPluginConfig> plugins = new ArrayList<>();
	}

	public static class YamlModuleConfig {
		public List<YamlFeatureConfig> features;
		public List<YamlFeatureConfig> featuresConfig;
		public final List<String> __flags__ = new ArrayList<>();
		public final List<YamlPluginConfig> plugins = new ArrayList<>();
	}

	public static class YamlFeatureConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 2215302676236317592L;
		//nothing more
	}

	public static class YamlPluginConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 5960474098385665743L;
		//nothing more
	}

	public static class YamlParamsConfig extends LinkedHashMap<String, String> {
		private static final long serialVersionUID = -3049054122944956757L;
		// nothing more
	}

	public static class YamlInitializerConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = -9154787055203850947L;
		// nothing more
	}
}
