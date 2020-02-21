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

import java.time.ZoneId;
import java.util.Locale;
import java.util.function.Supplier;

import io.vertigo.core.component.Manager;

/**
 * Toute application gérée par kapser est multilingue ou plus précisémment multidictionnaires.
 *
 * Il est possible de gérer des ressources externalisées dans des dictionnaires.
 *
 * Toute ressource est identifiée par une clé :  @see MessageKey
 * Pour un composant donné, la liste des clés est implémentée idéalement sous la forme d'une enum.
 * Un fichier de ressource, appelé dictionnaire est associée à la liste des clés.
 *
 * Si le libellé n'est pas trouvé dans une langue, on renvoie un message "panic", en précisant la langue demandée
 * de plus on loggue un warning.
 *
 * Exemple message panic :
 * MessageText(null,messageKey.TOTO) en 'fr_FR' : <<fr:TOTO>>
 * MessageText(null,messageKey.TOTO) en 'en' : <<en:TOTO>>
 *
 * Un libellé peut être paramétré.
 *
 * @see MessageText permet de créer des libellés connecté au dictionnaire.
 *
 *
 * @author pchretien
 */
public interface LocaleManager extends Manager {
	/**
	 * Enregistre une stratégie de choix de langue.
	 * @param localeProvider Définit la langue par défaut de façon contextuelle
	 */
	void registerLocaleProvider(LocaleProvider localeProvider);

	/**
	 * Enregistre une stratégie de choix de time zone.
	 * @param zoneProvider Définit la time zone par défaut de façon contextuelle
	 */
	void registerZoneProvider(Supplier<ZoneId> zoneProvider);

	/**
	 * Ajout d'un dictionnaire de ressources.
	 * Toutes les ressources identifiées par une clé doivent être présente dans le fichier properties.
	 * Cette méthode est non synchronisée etdoit être appelée lors du démarrage de l'application.
	 * @param baseName Nom et chemin du fichier properties
	 * @param enums Enumération (enum) de contrôle des ressources géréees
	 */
	void add(String baseName, MessageKey[] enums);

	/**
	 * Surcharge d'un dictionnaire de ressources.
	 * Cette méthode est non synchronisée et doit être appelée lors du démarrage de l'application.
	 * Il est possible de ne surcharger qu'une propriété.
	 * Il est possible de ne renseigner qu'un des dictionnaire (et donc de ne pas renseigner tous les bundles).
	 * @param baseName Nom et chemin du fichier properties
	 * @param enums Enumération (enum) de contrôle des ressources géréees
	 */
	void override(String baseName, MessageKey[] enums);

	/**
	 * Retourne le libellé non formatté d'un message identifié par sa clé.
	 * Retourne null si le message n'est pas trouvé
	 *
	 * @param messageKey clé du message .
	 * @param locale Locale
	 * @return Message non formatté dans la langue de la locale.
	  */
	String getMessage(MessageKey messageKey, Locale locale);

	/**
	 * Retourne la locale courante.
	 * C'est à dire correspondant à l'utilisateur courant si il y en a un,
	 * sinon correspond à la locale de l'application.
	 * @return Locale courante
	 */
	Locale getCurrentLocale();

	/**
	 * Retourne la time zone courante.
	 * C'est à dire correspondant à l'utilisateur courant si il y en a un,
	 * sinon correspond à la time zone de l'application.
	 * @return Zone courante
	 */
	ZoneId getCurrentZoneId();
}
