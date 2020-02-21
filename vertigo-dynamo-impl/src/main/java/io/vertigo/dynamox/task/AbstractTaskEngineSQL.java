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
package io.vertigo.dynamox.task;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionResourceId;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.statement.SqlStatementBuilder;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.lang.Assertion;

/**
 * Fournit des méthodes de haut niveau pour les services de type SQL.<br>
 * Un Service SQL est composé de paramètre de type primitif, DTO ou DTC, en IN, OUT ou INOUT et d'une requête SQL
 * sous forme de texte.
 * La requête est parsée puis préparée pour replacer les paramètres dynamo  par des variables bindées.
 * Grammaire des requêtes :<br>
 * <code>#<parametre>#</code> : paramètre IN <br>
 * <code>%<parametre>%</code> : paramètre OUT <br>
 * <code>@<parametre>@</code> : paramètre INOUT <br>
 * où <parametre> est : <br>
 * "primitif"     : LON_IDENTIFIANT_ID ou DAT_DATE_SAISIE <br>
 * "champ de dto" : <nom_du_DTO>.<nom_du_champ> : DTO_PERSONNE.NOM ou encore DTO_PERSONNE.PER_ID <br>
 * "champ de dtc" : <nom_du_DTC>.<n°_de_ligne>.<nom_du_champ> : DTC_PERSONNE.2.NOM ou encore DTC_PERSONNE.0.PER_ID <br>
 *  <br>
 * Les DTO et DTC n'ont pas besoin d'être en OUT ou en INOUT pour être mutées. <br>
 *  <br>
 * Intérêt de gérer des paramètres DTC : il existe maintenant un moyen d'accéder aux champs
 * d'une DTC, qui peut être utilisé pour créer des ServiceProviderSQL pseudo-dynamiques (ajout de mots clefs
 * dans la requête SQL du KSP pour gérer des itérations sur DTC par ex). <br>
 *
 * Exemple de requête : <br>
 *
 * SELECT TOTO_ID, NOM  <br>
 * FROM TOTO  <br>
 * WHERE TOTO_ID = #LON_TOTO_ID# <br>
 * AND   NOM like #DTO_FILTRE.NOM#||'%' <br>
 * AND   TYPE_ID IN (#DTC_TYPE.0.TYPE_ID#,#DTC_TYPE.1.TYPE_ID#,#DTC_TYPE.2.TYPE_ID#) <br>
 *
 * De plus permet de créer du SQL dynamiquement interprété.
 * Les paramètres IN de la tache peuvent être invoqués pour construire
 * la requête SQL.
 * Exemple :
 * request = " Select *
 *       From PRODUIT
 *       <%if (dtoProduitCritere.getPrdLibelle()!=null) {%>
 *               Where PRD_LIBELLE like #DTO_PRODUIT_CRITERE.PRD_LIBELLE#||'%%'
 *       <%}%> order by <%=1%>";
 *
 * @author  pchretien, npiedeloup
 */
public abstract class AbstractTaskEngineSQL extends TaskEngine {
	/**
	 * Identifiant de ressource SQL par défaut.
	 */
	public static final VTransactionResourceId<SqlConnection> SQL_MAIN_RESOURCE_ID = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Sql-main");

	/**
	 * Nom de l'attribut recevant le nombre de lignes affectées par un Statement.
	 * Dans le cas des Batchs ce nombre correspond à la somme de toutes les lignes affectées par le batch.
	 */
	//Qui utilise ça ?? // peut on revenir à une forme explicite
	public static final String SQL_ROWCOUNT = "intSqlRowcount";

	private final ScriptManager scriptManager;
	private final VTransactionManager transactionManager;
	private final StoreManager storeManager;
	private final SqlDataBaseManager sqlDataBaseManager;

	/**
	 * Constructor.
	 * @param scriptManager Manager de traitment de scripts
	 */
	protected AbstractTaskEngineSQL(
			final ScriptManager scriptManager,
			final VTransactionManager transactionManager,
			final StoreManager storeManager,
			final SqlDataBaseManager sqlDataBaseManager) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(sqlDataBaseManager);
		//-----
		this.scriptManager = scriptManager;
		this.transactionManager = transactionManager;
		this.storeManager = storeManager;
		this.sqlDataBaseManager = sqlDataBaseManager;
	}

	/**
	 * Exécution de la requête.
	 * @param connection Connexion BDD
	 * @param statement Requête
	 * @return Nombre de lignes affectées (Insert/ Update / Delete)
	 * @throws SQLException Erreur sql
	 */
	protected abstract OptionalInt doExecute(
			final SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException;

	/** {@inheritDoc} */
	@Override
	public void execute() {
		final SqlConnection connection = obtainConnection();

		final SqlStatementBuilder statementBuilder = SqlStatement.builder(getSqlQuery().trim());
		setNamedParameters(statementBuilder);
		final SqlStatement sqlStatement = statementBuilder.build();
		try {
			//Execute le Statement JDBC.
			final OptionalInt sqlRowcountOpt = doExecute(sqlStatement, connection);
			//On positionne le nombre de lignes affectées.
			sqlRowcountOpt.ifPresent(this::setRowCount);
		} catch (final BatchUpdateException sqle) { //some exception embedded the usefull one
			// Gère les erreurs d'exécution Batch JDBC.
			throw handleSQLException(connection, sqle.getNextException(), sqlStatement.getSqlQuery());
		} catch (final SQLException sqle) {
			//Gère les erreurs d'exécution JDBC.
			throw handleSQLException(connection, sqle, sqlStatement.getSqlQuery());
		}

	}

	private void setRowCount(final int sqlRowcount) {
		getTaskDefinition().getOutAttributeOption().ifPresent(
				outTaskAttribute -> {
					if (SQL_ROWCOUNT.equals(outTaskAttribute.getName())) {
						setResult(sqlRowcount);
					}
				});
	}

	//-----
	/**
	 * Retourne la Query qui sera parsée
	 * Par défaut il s'agit de la request définie sur le service
	 * @return Chaine de configuration
	 */
	protected String getSqlQuery() {
		//On ajoute dans la requête SQL le nom de la tache utilisée
		return preProcessQuery(new StringBuilder()
				.append("/* TaskEngine : ")
				.append(getTaskDefinition().getName())
				.append(" */\n")
				.append(getTaskDefinition().getRequest())
				.toString());
	}

	/**
	 * Permet de créer du SQL dynamiquement interprété.
	 * Les paramètres IN de la tache peuvent être invoqués pour construire
	 * la requête SQL.
	 * Exemple :
	 * request = " Select *
	 *       From PRODUIT
	 *       <%if (dtoProduitCritere.getPrdLibelle()!=null) {%>
	 *               Where PRD_LIBELLE like #DTO_PRODUIT_CRITERE.PRD_LIBELLE#||'%%'
	 *       <%}%> order by <%=1%>";
	 * @param sqlQuery Requete à évaluer
	 * @return Requete évaluée
	 **/
	protected final String preProcessQuery(final String sqlQuery) {
		final Collection<TaskAttribute> inAttributes = getTaskDefinition().getInAttributes();
		final Map<TaskAttribute, Object> inTaskAttributes = new HashMap<>();
		for (final TaskAttribute taskAttribute : inAttributes) {
			inTaskAttributes.put(taskAttribute, getValue(taskAttribute.getName()));
		}
		//-----
		final ScriptPreProcessor scriptPreProcessor = new ScriptPreProcessor(scriptManager, inTaskAttributes, SeparatorType.CLASSIC);
		final TrimPreProcessor trimPreProcessor = new TrimPreProcessor(SeparatorType.BEGIN_SEPARATOR_CLASSIC, SeparatorType.END_SEPARATOR_CLASSIC);
		final WhereInPreProcessor whereInPreProcessor = new WhereInPreProcessor(inTaskAttributes);
		//--
		String sql = sqlQuery;
		sql = scriptPreProcessor.evaluate(sql);
		sql = trimPreProcessor.evaluate(sql);
		sql = whereInPreProcessor.evaluate(sql);
		return sql;
	}

	protected void setNamedParameters(final SqlStatementBuilder sqlStatementBuilder) {
		getTaskDefinition().getInAttributes()
				.forEach(taskInAttribute -> sqlStatementBuilder.bind(
						taskInAttribute.getName(),
						taskInAttribute.getDomain().getJavaClass(),
						getValue(taskInAttribute.getName())));
	}

	/**
	 * Retourne la connexion SQL de cette transaction en la demandant au pool de connexion si nécessaire.
	 * @return Connexion SQL
	 */
	private SqlConnection obtainConnection() {
		final VTransaction transaction = transactionManager.getCurrentTransaction();
		SqlConnection connection = transaction.getResource(getVTransactionResourceId());
		if (connection == null) {
			// On récupère une connexion du pool
			// Utilise le provider de connexion déclaré sur le Container.
			connection = getConnectionProvider().obtainConnection();
			transaction.addResource(getVTransactionResourceId(), connection);
		}
		return connection;
	}

	/**
	 * @return Id de la Ressource Connexion SQL dans la transaction
	 */
	protected VTransactionResourceId<SqlConnection> getVTransactionResourceId() {
		final String dataSpace = getTaskDefinition().getDataSpace();
		if (StoreManager.MAIN_DATA_SPACE_NAME.equals(dataSpace)) {
			return SQL_MAIN_RESOURCE_ID;
		}
		return new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Sql-" + dataSpace);
	}

	/**
	 * @return Manager de base de données
	 */
	protected final SqlDataBaseManager getDataBaseManager() {
		return sqlDataBaseManager;
	}

	/**
	 * Il est possible de surcharger la configuration SQL d'un service.
	 * @return Configuration SQL.
	 */
	protected SqlConnectionProvider getConnectionProvider() {
		final String dataSpace = getTaskDefinition().getDataSpace();
		final String connectionName = storeManager.getDataStoreConfig().getConnectionName(dataSpace);
		return getDataBaseManager().getConnectionProvider(connectionName);
	}

	/**
	 * Gestion centralisée des exceptions SQL.
	 * @param connection Connexion
	 * @param sqle Exception SQL
	 * @param statement Statement
	 */
	private static RuntimeException handleSQLException(final SqlConnection connection, final SQLException sqle, final String statementInfos) {
		return connection.getDataBase().getSqlExceptionHandler()
				.handleSQLException(sqle, statementInfos);
	}
}
