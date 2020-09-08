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
package io.vertigo.core.impl.locale;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.param.ParamValue;

/**
 * @author pchretien
 */
public final class LocaleManagerImpl implements LocaleManager {
	private static final Logger LOG = LogManager.getLogger(LocaleManagerImpl.class);

	/**
	 * Set des clés non trouvées pour ne pas les reloguer.
	 * On synchronise car il s'agit d'une ressource partagée modifiées par tous les threads.
	 */
	private final Set<String> notFoundKeys = Collections.synchronizedSet(new HashSet<String>());

	//Bundle pour la locale par défaut.
	private final Map<Locale, Map<String, String>> dictionaries = new HashMap<>();

	/** liste des locales gérées. */
	private final List<Locale> locales;

	/** Zone par défaut. */
	private final ZoneId defaultZoneId;

	/**
	 * Stratégie de choix de la langue.
	 * Si pas de stratégie ou pas de langue trouvée alors langue par défaut = première langue déclarée.
	 */
	private Supplier<Locale> localeSupplier;

	/**
	 * Stratégie de choix de la zone.
	 * Si pas de stratégie ou pas de zone trouvée alors zone par défaut.
	 */
	private Supplier<ZoneId> zoneSupplier;

	/**
	 * Constructor.
	 * Les exceptions sont toujours externalisées.
	 * Les libellés de champs ne le sont pas.
	 * La première Locale est celle utilisée si il n'y a aucun utilisateur déclaré (cas des batchs).
	 *
	 * On précise la liste des locales sous la forme d'une chaine de caractères
	 * exemples :
	 * Locale.french : 'fr'
	 * Locale.FRANCE + Locale.us : 'fr_FR,us'
	 * Une locale est définie par une langue{_Pays{_Variante}}
	 * @param locales Liste des locales gérées par l'application.
	 * @param defaultZoneId ZoneId par défaut utilisée par l'application.
	 */
	@Inject
	public LocaleManagerImpl(@ParamValue("locales") final String locales, @ParamValue("defaultZoneId") final Optional<String> defaultZoneId) {
		Assertion.check()
				.isNotBlank(locales)
				.isNotNull(defaultZoneId);
		//-----
		this.locales = createLocales(locales);
		this.defaultZoneId = createDefaultZoneId(defaultZoneId);
		//-----
		Assertion.check()
				.isNotNull(this.locales)
				.isFalse(this.locales.isEmpty(), "Il faut au moins déclarer une locale");
		//-----
		for (final Locale locale : this.locales) {
			dictionaries.put(locale, new HashMap<>());
		}
	}

	private static ZoneId createDefaultZoneId(final Optional<String> defaultZoneIdOpt) {
		return defaultZoneIdOpt
				.map(ZoneId::of)
				.orElseGet(ZoneId::systemDefault);
	}

	private static List<Locale> createLocales(final String locales) {
		return Stream.of(locales.split(","))
				.map(locale -> {
					final String[] loc = locale.trim().split("_");
					Assertion.check().isTrue(loc.length > 0, "Locale specifiée vide");
					final String country = loc.length > 1 ? loc[1] : "";
					final String variant = loc.length > 2 ? loc[2] : "";
					return new Locale(loc[0], country, variant);
				})
				.collect(Collectors.toUnmodifiableList());

	}

	/** {@inheritDoc} */
	@Override
	public void registerZoneSupplier(final Supplier<ZoneId> newZoneSupplier) {
		Assertion.check()
				.isNull(zoneSupplier, "zoneSupplier already registered")
				.isNotNull(newZoneSupplier);
		//---
		zoneSupplier = newZoneSupplier;
	}

	/** {@inheritDoc} */
	@Override
	public void registerLocaleSupplier(final Supplier<Locale> newLocaleSupplier) {
		Assertion.check()
				.isNull(localeSupplier, "localeSupplier already registered")
				.isNotNull(newLocaleSupplier);
		//---
		localeSupplier = newLocaleSupplier;
	}

	/** {@inheritDoc} */
	@Override
	public void add(final String baseName, final MessageKey[] enums) {
		add(baseName, enums, false);
	}

	/** {@inheritDoc} */
	@Override
	public void override(final String baseName, final MessageKey[] enums) {
		add(baseName, enums, true);
	}

	private void add(final String baseName, final MessageKey[] enums, final boolean override) {
		for (final Locale locale : locales) {
			//Pour chaque locale gérée on charge le dictionnaire correspondant
			final ResourceBundle resourceBundle;
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale);
			} catch (final MissingResourceException e) {
				if (override) {
					//Si on est en mode override on autorise des chargements partiels de dictionnaire
					continue;
				}
				throw WrappedException.wrap(e, "le dictionnaire pour la locale '{0}' n'est pas renseigné", locale);
			}
			//On a trouvé un dictionnaire
			check(resourceBundle, enums, override);
			load(locale, resourceBundle, override);
		}
	}

	private Map<String, String> getDictionary(final Locale locale) {
		Assertion.check().isTrue(dictionaries.containsKey(locale), "La locale {0} n'est pas gérée", locale);
		//---
		return dictionaries.get(locale);
	}

	private void load(final Locale locale, final ResourceBundle resourceBundle, final boolean override) {
		for (final String key : Collections.list(resourceBundle.getKeys())) {
			final String value = resourceBundle.getString(key);
			Assertion.check().isNotNull(value);
			final String oldValue = getDictionary(locale).put(key, value);
			if (!override) {
				Assertion.check().isNull(oldValue, "Valeur deja renseignée pour{0}", key);
			}
		}
	}

	private void check(final ResourceBundle resourceBundle, final MessageKey[] enums, final boolean override) {
		//============================================
		//==On vérifie que les listes sont complètes==
		//============================================
		final List<String> resourcesKeys = Arrays.stream(enums)
				.map(MessageKey::name)
				.collect(Collectors.toList());

		//1- Toutes les clés du fichier properties sont dans l'enum des resources
		for (final String key : Collections.list(resourceBundle.getKeys())) {
			if (!resourcesKeys.contains(key)) {
				throw new IllegalStateException("Une clé du fichier properties est inconnue : " + key);
			}
		}

		//2- Toutes les clés de l'enum sont dans le fichier properties
		if (!override) {
			for (final String resourceKey : resourcesKeys) {
				if (!resourceBundle.containsKey(resourceKey)) {
					onResourceNotFound(resourceKey);
					throw new IllegalStateException("Une ressource n'est pas déclarée dans le fichier properties : " + resourceKey);
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getMessage(final MessageKey messageKey, final Locale locale) {
		Assertion.check()
				.isNotNull(messageKey)
				.isNotNull(locale);
		//-----
		final String msg = getDictionary(locale).get(messageKey.name());
		//Cas anormal :  où la ressource n'est pas présente.
		if (msg == null) {
			//Cas anormal :  où la ressource n'est pas présente.
			onResourceNotFound(messageKey.name());
			return null;
		}
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public ZoneId getCurrentZoneId() {
		if (zoneSupplier != null && zoneSupplier.get() != null) {
			return zoneSupplier.get();
		}
		//If there is no user, we can pick the default zone.
		return defaultZoneId;
	}

	/** {@inheritDoc} */
	@Override
	public Locale getCurrentLocale() {
		if (localeSupplier != null && localeSupplier.get() != null) {
			final Locale currentLocale = localeSupplier.get();
			//We have to check if the currentLocale belongs to locales.
			if (!locales.contains(localeSupplier.get())) {
				LOG.error("CurrentLocale '{}' is not allowed, it must be in '{}'", currentLocale, locales);
				//So, we can pick the default language.
				return locales.get(0);
			}
			return currentLocale;
		}
		//If there is no user, we can pick the default language.
		return locales.get(0);
	}

	private void onResourceNotFound(final String key) {
		if (!notFoundKeys.contains(key)) {
			//	Si la ressource n'est pas présente alors
			//  - on loggue le fait que la ressource n'a pas été trouvée
			//  - on stocke la clé pour éviter de reloguer
			//et on continue les traitements
			logResourceNotFound(key, locales.size() > 1);
			notFoundKeys.add(key);
		}
	}

	/**
	 * Evènement remonté lorsqu'une ressource externalisée n'est pas trouvée.
	 * @param resource  Nom de la ressource externalisée non trouvée
	 * @param isMultiLocales Si appli multilingue
	 */
	private static void logResourceNotFound(final String resource, final boolean isMultiLocales) {
		if (isMultiLocales) {
			LOG.warn("Resource {} non trouvée", resource);
		} else {
			LOG.info("Resource {} non trouvée", resource);
		}
	}
}
