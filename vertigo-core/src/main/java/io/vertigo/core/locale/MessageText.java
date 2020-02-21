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
package io.vertigo.core.locale;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Texte pouvant être externalisé dans un fichier de ressources,
 * en fonction du paramétrage de l'application.
 * Si le libelle n'est pas trouvé, l'affichage est
 * @author npiedeloup, pchretien
 */
public final class MessageText implements Serializable {
	private static final long serialVersionUID = 1L;
	/**Clé du libellé dans le dictionnaire. */
	private final MessageKey key;
	/**Libellé non formatté. */
	private final String defaultMsg;
	/**paramètres permettant de formatter le libellé. */
	private final Serializable[] params;

	/**
	 * static Builder of a messageText by its key.
	 * @param key Clé de la ressource
	 * @return the messageText
	 */
	public static MessageText of(final MessageKey key, final Serializable... params) {
		Assertion.checkNotNull(key, "the message key is required");
		//---
		return new MessageText(null, key, params);
	}

	/**
	 * static Builder of a messageText by its default message.
	 * @param msg Message par défaut (non formatté) de la ressource
	 * @return the messageText
	 */
	public static MessageText of(final String msg, final Serializable... params) {
		Assertion.checkArgNotEmpty(msg, "the message is required");
		//---
		return new MessageText(msg, null, params);
	}

	/**
	 * static Builder of a messageText by its default message.
	 * @param defaultMsg Message par défaut (non formatté) de la ressource
	 * @return the messageText
	 */
	public static MessageText ofDefaultMsg(final String defaultMsg, final MessageKey key, final Serializable... params) {
		Assertion.checkArgNotEmpty(defaultMsg, "the default message is required");
		Assertion.checkNotNull(key, "the message key is required");
		//---
		return new MessageText(defaultMsg, key, params);
	}

	/**
	 * Constructor.
	 * La clé et/ou le message par défaut doit être non null.
	 *
	 * @param defaultMsg Message par défaut (non formatté) de la ressource
	 * @param key Clé de la ressource
	 * @param params paramètres de la ressource
	 */
	private MessageText(final String defaultMsg, final MessageKey key, final Serializable... params) {
		Assertion.checkNotNull(params);
		Assertion.checkArgument(defaultMsg != null || key != null, "key or msg must be defined");
		//---
		this.key = key;
		this.defaultMsg = defaultMsg;
		this.params = params;
	}

	/**
	 * Constructor.
	 *
	 * @param key Clé de la ressource
	 * @param params paramètres de la ressource
	 */
	private MessageText(final MessageKey key, final Serializable... params) {
		this(null, key, params);
	}

	/**
	 * @return paramètres du message
	 */
	private Object[] getParams() {
		return params;
	}

	/**
	 * Formatte un message avec des paramètres.
	 * Ne lance aucune exception !!
	 * @return Message formatté.
	 */
	public String getDisplay() {
		/*
		 * Cette méthode doit toujours remonter un message.
		 * Si LocaleManager n'est pas enregistré ou génére une exception
		 * alors on se contente de retourner la clé du message.
		 */
		Locale locale = null;
		String msg = null;
		if (key != null) {
			//On ne recherche le dictionnaire (géré par localeManager) que si il y a une clé.
			final LocaleManager localeManager;
			try {
				//Il est nécessaire que LocaleManager soit enregistré.
				//Si pas d'utilisateur on prend la première langue déclarée.
				localeManager = Home.getApp().getComponentSpace().resolve(LocaleManager.class);
				locale = localeManager.getCurrentLocale();
				msg = localeManager.getMessage(key, locale);
			} catch (final Exception e) {
				//Si pas de locale msg est null et on va récupérer s'il existe le message par défaut.
			}
		}

		//Si pas de clé on recherche le libellé par défaut.
		if (msg == null) {
			msg = defaultMsg;
		}
		if (msg != null) {
			//On passe toujours dans le StringUtil.format pour unifier.
			return StringUtil.format(msg, getParams());
		}
		//On a rien trouvé on renvoit ce que l'on peut. (locale peut être null)
		return getPanicMessage(locale);
	}

	private String getPanicMessage(final Locale locale) {
		return new StringBuilder()
				.append("<<")
				.append(locale != null ? locale.getLanguage() : "xx")
				.append(":")
				.append(defaultMsg != null ? defaultMsg : key.name() + (params.length == 0 ? "" : Arrays.toString(params)))
				.append(">>")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getPanicMessage(null);
	}
}
