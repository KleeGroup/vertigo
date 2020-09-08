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
package io.vertigo.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.lang.WrappedException;

/**
 * Méthodes utilitaires pour manipuler les propriétés (getter/setter) des JavaBeans (ie tous les types d'objets).
 */
public final class BeanUtil {
	private static final int BEAN_INFOS_MAX_SIZE = 250;
	private static final Map<Class<?>, BeanInfo> BEAN_INFOS = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 */
	private BeanUtil() {
		//constructor is private
	}

	/**
	 * Retourne la valeur d'une propriété d'un bean
	 * (ex : "name" -> object.getName() ou "country.name" -> object.getCountry().getName()).
	 * @return java.lang.Object
	 * @param object java.lang.Object
	 * @param propertyName java.lang.String
	 */
	public static Object getValue(final Object object, final String propertyName) {
		Assertion.check()
				.isNotNull(object)
				.isNotNull(propertyName)
				.isTrue(propertyName.indexOf('.') == -1, "the dot notation is forbidden");
		//-----
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method readMethod = pd.getReadMethod();
		if (readMethod == null) {
			throw new VSystemException("no getter found for property '{0}' on class '{1}'", propertyName, object.getClass().getName());
		}
		return ClassUtil.invoke(object, readMethod);
	}

	/**
	 * Définit la valeur d'une propriété d'un bean
	 * (ex : "name" -> object.setName(value) ou "country.name" -> object.getCountry().setName(value)).
	 * @param object java.lang.Object
	 * @param propertyName java.lang.String
	 * @param value java.lang.Object
	 */
	public static void setValue(final Object object, final String propertyName, final Object value) {
		Assertion.check()
				.isNotNull(object)
				.isNotNull(propertyName)
				.isTrue(propertyName.indexOf('.') == -1, "the dot notation is forbidden");
		//-----
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method writeMethod = pd.getWriteMethod();
		if (writeMethod == null) {
			throw new VSystemException("no setter found for property '{0}' on class '{1}'", propertyName, object.getClass().getName());
		}
		ClassUtil.invoke(object, writeMethod, value);
	}

	/**
	 * Retourne le beanInfo d'une classe à partir du cache.
	 * @return java.beans.BeanInfo
	 * @param beanClass java.lang.Class
	 * @throws java.beans.IntrospectionException   Erreur dans l'introspection
	 */
	private static BeanInfo getBeanInfo(final Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = BEAN_INFOS.get(beanClass);
		if (beanInfo == null) {
			if (BEAN_INFOS.size() > BEAN_INFOS_MAX_SIZE) {
				BEAN_INFOS.clear();
				// pour éviter une fuite mémoire potentielle, par ex sur classes proxy
			}
			// On veut tout le BeanInfo sauf Object (pas la propriété de getClass())
			beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
			BEAN_INFOS.put(beanClass, beanInfo);
		}
		return beanInfo;
	}

	/**
	 * Retourne le PropertyDescriptor d'une propriété.
	 * @return java.beans.PropertyDescriptor
	 * @param propertyName java.lang.String
	 * @param beanClass java.lang.Class
	 */
	public static PropertyDescriptor getPropertyDescriptor(final String propertyName, final Class<?> beanClass) {
		// on pourrait faire new PropertyDescriptor(propertyName, beanClass)
		// mais si jamais il a été défini des BeanInfo pour certaines classes,
		//autant les utiliser.
		final PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
		for (final PropertyDescriptor propertyDescriptor : descriptors) {
			if (propertyName.equals(propertyDescriptor.getName())) {
				return propertyDescriptor;
			}
		}
		throw new VSystemException("No method found for property '{0}' on class '{1}'", propertyName, beanClass.getName());
	}

	private static PropertyDescriptor[] getPropertyDescriptors(final Class<?> beanClass) {
		try {
			return getBeanInfo(beanClass).getPropertyDescriptors();
		} catch (final IntrospectionException e) {
			throw WrappedException.wrap(e, "Erreur d'introspection des propriétés sur la classe {0}", beanClass);
		}
	}
}
