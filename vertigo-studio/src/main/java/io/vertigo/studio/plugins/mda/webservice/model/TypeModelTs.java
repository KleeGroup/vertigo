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
package io.vertigo.studio.plugins.mda.webservice.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import io.vertigo.studio.plugins.mda.webservice.JsFileNameUtil;

/**
 * FreeMarker Model of Java Type
 * @author npiedeloup
 */
public class TypeModelTs {

	private boolean isObject = false;
	private boolean isList = false;
	private String jsGenericType;
	private String jsImportPath = null;

	/**
	 * Constructor.
	 * @param type Java Type
	 */
	public TypeModelTs(final Type type) {
		String typeValue;
		if (type instanceof ParameterizedType) {
			final ParameterizedType genericListType = (ParameterizedType) type;
			final Class<?> genericClass = (Class<?>) genericListType.getActualTypeArguments()[0];
			typeValue = genericClass.toString();
			isList = List.class.getTypeName().equals(genericListType.getTypeName().split("<")[0]);
		} else {
			typeValue = type.toString();
		}
		if (typeValue.contains(".domain.")) {
			final String[] packageSplitted = typeValue.split("\\.");
			jsImportPath = "../../../" + packageSplitted[packageSplitted.length - 2] + "/config/entity-definition-gen/";
		}
		typeValue = typeValue.replaceAll("class |interface ", "").replaceAll("(?<=^|<)[a-z]+\\.[a-z\\.]+", "");
		switch (typeValue) {
			case "String":
			case "Date":
				jsGenericType = "string";
				break;
			case "Long":
			case "long":
			case "Integer":
			case "int":
				jsGenericType = "number";
				break;
			case "boolean":
			case "Boolean":
				jsGenericType = "boolean";
				break;
			case "VFile":
			case "vfile":
				jsGenericType = "any";
				break;
			default:
				isObject = true;
				jsGenericType = typeValue;
				break;
		}
	}

	/**
	 * @return Ts Import declaration
	 */
	public String getImportDeclaration() {
		if (getImportPath() != null) {
			return "import { " + getJsGenericType() + " } from \"" + getImportPath() + getJsClassName() + "\"";
		}
		return null;
	}

	/**
	 * @return Js generic type
	 */
	public String getJsGenericType() {
		return jsGenericType;
	}

	/**
	 * @return Js className
	 */
	public String getJsClassName() {
		return JsFileNameUtil.convertCamelCaseToJsCase(getJsGenericType());
	}

	/**
	 * @return Js import path
	 */
	public String getImportPath() {
		return jsImportPath;
	}

	/**
	 * @return js Type
	 */
	public String getJsType() {
		return jsGenericType + (isList ? "[]" : "");
	}

	/**
	 * @return is object
	 */
	public boolean isObject() {
		return isObject;
	}

	/**
	 * @return is list
	 */
	public boolean isList() {
		return isList;
	}

	/**
	 * @return is void
	 */
	public boolean isVoid() {
		return "void".equals(jsGenericType);
	}
}
