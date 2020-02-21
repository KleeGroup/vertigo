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
package io.vertigo.studio.plugins.mda.task.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.vertigo.lang.VUserException;

/**
 * Vérifieur d'objets d'accès aux données.
 * @author sezratty
 *
 */
public class TaskTestDaoChecker {

	/**
	 * Appelle une action de DAO.
	 * On ne vérifie que la sémantique : SQL valide et modèle valide.
	 * Les erreurs liées aux données ne font pas tomber le test en erreur.
	 * @param action Action appelant le DAO à tester.
	 */
	public void semantics(final Runnable action) {
		try {
			action.run();
		} catch (final VUserException vue) {
			// Erreur utilisateur liées aux données
			// On considère que le test est OK.
		} catch (final NullPointerException npe) {
			if (isOutAttributeNullCase(npe)) {
				// Cas où la requête ne retourne aucune ligne : on soncidère que c'est lié aux données.
				return;
			}
			throw npe;
		} catch (final Exception e) {
			// TODO Attraper les erreurs liées aux données.
			// Autre erreur : on relance pour faire tomber le test en erreur.
			throw e;
		}
	}

	private boolean isOutAttributeNullCase(final NullPointerException npe) {

		/* Dans le cas d'une requête qui n'a pas retournée de ligne alors qu'on en attend une,
		 * on constate la stacktrace suivante :
		 *
		 * java.lang.NullPointerException: Attribut task CLASSIFICATION ne doit pas etre null (cf. paramétrage task)
		at io.vertigo.lang.Assertion.checkNotNull(Assertion.java:71)
		at io.vertigo.dynamo.task.metamodel.TaskAttribute.checkAttribute(TaskAttribute.java:102)
		at io.vertigo.dynamo.task.model.TaskResult.lambda$new$4(TaskResult.java:47)
		at java.util.Optional.ifPresent(Optional.java:159)
		at io.vertigo.dynamo.task.model.TaskResult.<init>(TaskResult.java:47)
		at io.vertigo.dynamo.task.model.TaskEngine.process(TaskEngine.java:58)
		at io.vertigo.dynamo.impl.task.TaskManagerImpl.doExecute(TaskManagerImpl.java:60)
		at io.vertigo.dynamo.impl.task.TaskManagerImpl.lambda$execute$25(TaskManagerImpl.java:55)
		at io.vertigo.commons.impl.analytics.AnalyticsManagerImpl.traceWithReturn(AnalyticsManagerImpl.java:88)
		at io.vertigo.dynamo.impl.task.TaskManagerImpl.execute(TaskManagerImpl.java:52)
		 *
		 * */
		//---
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		npe.printStackTrace(pw);
		final String st = sw.getBuffer().toString();
		//---
		return st.contains("ne doit pas etre null (cf. paramétrage task)") &&
				st.contains("at io.vertigo.dynamo.task.model.TaskResult.<init>");
	}

}
