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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlAppConfig {

	public YamlNodeConfig node;
	public YamlBootConfig boot;
	public final LinkedHashMap<String, YamlModuleConfig> modules = new LinkedHashMap<>();
	public final List<YamlInitializerConfig> initializers = new ArrayList<>();

	public static final class YamlNodeConfig {
		public String appName;
		public String nodeId;
		public String endPoint;
	}

	public static final class YamlBootConfig {
		public YamlParamsConfig params;
		public List<YamlPluginConfig> plugins = new ArrayList<>();
	}

	public static class YamlModuleConfig {
		public List<YamlFeatureConfig> features;
		public List<YamlFeatureConfig> featuresConfig;
		public List<String> __flags__ = new ArrayList<>();
		public List<YamlPluginConfig> plugins = new ArrayList<>();
	}

	public static class YamlFeatureConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = -2681657188040880067L;
		//nothing more
	}

	public static class YamlPluginConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = 7801652174493689560L;
		//nothing more
	}

	public static class YamlParamsConfig extends LinkedHashMap<String, String> {
		private static final long serialVersionUID = -3551104464145560067L;
		// nothing more
	}

	public static class YamlInitializerConfig extends LinkedHashMap<String, Map<String, Object>> {
		private static final long serialVersionUID = -8925834051505358263L;
		// nothing more
	}
}
