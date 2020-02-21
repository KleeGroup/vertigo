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
package io.vertigo.vega.webservice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * WebService Type helper.
 * @author npiedeloup
 */
public final class WebServiceTypeUtil {

	/**
	 * Private constructor for helper.
	 */
	private WebServiceTypeUtil() {
		//nothing
	}

	private static IllegalArgumentException createIllegalArgumentException(final Type testedType) {
		return new IllegalArgumentException("Parameters Type must be Class or ParameterizedType, unsupported type:" + testedType);
	}

	/**
	 * Equivalent to parentClass.isAssignableFrom(testedType);
	 * @param parentClass Parent Class
	 * @param testedType Type to test (must be a Class or ParameterizedType)
	 * @return Is testedType assignable from parentClass ?
	 */
	public static boolean isAssignableFrom(final Class<?> parentClass, final Type testedType) {
		if (testedType instanceof Class) {
			return parentClass.isAssignableFrom((Class<?>) testedType);
		} else if (testedType instanceof ParameterizedType) {
			return parentClass.isAssignableFrom((Class<?>) ((ParameterizedType) testedType).getRawType());
		}
		throw createIllegalArgumentException(testedType);
	}

	/**
	 * Check if testedType is ParameterizedType and it's parameter is assignable from innerClass;
	 * @param innerClass Inner Class
	 * @param testedType Type to test (must be a Class or ParameterizedType)
	 * @return Is testedType parameterized by innerClass ?
	 */
	public static boolean isParameterizedBy(final Class<?> innerClass, final Type testedType) {
		if (testedType instanceof Class) {
			return false;
		} else if (testedType instanceof ParameterizedType) {
			final Type[] typeArguments = ((ParameterizedType) testedType).getActualTypeArguments();
			return Arrays.stream(typeArguments)
					.anyMatch(typeArgument -> isAssignableFrom(innerClass, typeArgument));
		}
		throw createIllegalArgumentException(testedType);
	}

	/**
	 * Cast as Class;
	 * @param testedType Type to test (must be a Class or ParameterizedType)
	 * @return Is testedType assignable from parentClass ?
	 */
	public static Class<?> castAsClass(final Type testedType) {
		if (testedType instanceof Class) {
			return (Class<?>) testedType;
		} else if (testedType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) testedType).getRawType();
		}
		throw createIllegalArgumentException(testedType);
	}
}
