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
package io.vertigo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.vertigo.app.App;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.Home;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.component.di.DIInjector;

/**
 * Classe parente de tous les TNR associés à vertigo.
 *
 * @author jmforhan
 */
public abstract class AbstractTestCaseJU5 {

	protected final App getApp() {
		return Home.getApp();
	}

	/**
	 * Méthode ne faisant rien.
	 *
	 * @param o object
	 */
	protected static void nop(final Object o) {
		// rien
	}

	@BeforeEach
	public final void setUp() throws Exception {
		openApp();
		doSetUp();
	}

	/**
	 * Initialisation du test pour implé spécifique.
	 *
	 * @throws Exception Erreur
	 */
	protected void doSetUp() throws Exception {
		// pour implé spécifique
	}

	@AfterEach
	public final void tearDown() throws Exception {
		try {
			doTearDown();
		} finally {
			closeApp();
		}
		doAfterTearDown();
	}

	private void openApp() {
		final App app = new AutoCloseableApp(buildNodeConfig());
		DIInjector.injectMembers(this, app.getComponentSpace());
	}

	private static void closeApp() {
		((AutoCloseableApp) Home.getApp()).close();
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 *
	 * @throws Exception Erreur
	 */
	protected void doTearDown() throws Exception {
		// pour implé spécifique
	}

	/**
	 * Finalisation du test pour implé spécifique après le tear down.
	 *
	 * @throws Exception Erreur
	 */
	protected void doAfterTearDown() {
		// pour implé spécifique
	}

	/**
	 * Configuration des tests.
	 * @return App config
	 */
	protected abstract NodeConfig buildNodeConfig();

}
