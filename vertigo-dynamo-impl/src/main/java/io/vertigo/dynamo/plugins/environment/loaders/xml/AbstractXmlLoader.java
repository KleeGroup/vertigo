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
package io.vertigo.dynamo.plugins.environment.loaders.xml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionBuilder;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.loaders.Loader;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;

/**
 * Parser d'un fichier powerAMC/OOM ou EA/XMI.
 *
 * @author pchretien
 */
public abstract class AbstractXmlLoader implements Loader {
	private static final int MAX_COLUMN_LENGTH = 30;
	private static final Logger LOGGER = LogManager.getLogger(AbstractXmlLoader.class);

	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private final ResourceManager resourceManager;
	private final boolean constFieldNameInSource;

	/**
	 * Constructor.
	 * @param constFieldNameInSource FieldName in file is in CONST_CASE instead of camelCase
	 * @param resourceManager the vertigo resourceManager
	 */
	public AbstractXmlLoader(final boolean constFieldNameInSource, final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//-----
		this.resourceManager = resourceManager;
		this.constFieldNameInSource = constFieldNameInSource;
	}

	/** {@inheritDoc} */
	@Override
	public final void load(final String resourcePath, final DslDefinitionRepository dslDefinitionRepository) {
		final URL xmiFileURL = resourceManager.resolve(resourcePath);

		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			final SAXParser saxParser = factory.newSAXParser();
			try (final InputStream is = xmiFileURL.openStream()) {
				saxParser.parse(is, getHandler());
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "erreur lors de la lecture du fichier xmi : {0}", xmiFileURL);
		}
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dslDefinitionRepository);
		//-----

		for (final XmlClass clazz : getClasses()) {
			dslDefinitionRepository.addDefinition(toDynamicDefinition(clazz));
		}

		for (final XmlAssociation association : getAssociations()) {
			dslDefinitionRepository.addDefinition(toDynamicDefinition(association, dslDefinitionRepository));
		}
	}

	protected abstract DefaultHandler getHandler();

	/**
	 * Récupération des classes déclarées.
	 * @return Liste des classes
	 */
	protected abstract List<XmlClass> getClasses();

	/**
	 * Récupération des associations déclarées dans l'OOM.
	 * @return Liste des associations
	 */
	protected abstract List<XmlAssociation> getAssociations();

	protected final boolean isConstFieldNameInSource() {
		return constFieldNameInSource;
	}

	private static DslDefinition toDynamicDefinition(final XmlClass clazz) {
		final DslEntity dtDefinitionEntity = DomainGrammar.DT_DEFINITION_ENTITY;
		final DslDefinitionBuilder dtDefinitionBuilder = DslDefinition.builder(getDtDefinitionName(clazz.getCode()), dtDefinitionEntity)
				.withPackageName(clazz.getPackageName())
				//Par défaut les DT lues depuis le OOM/XMI sont persistantes.
				.addPropertyValue(KspProperty.STEREOTYPE, clazz.getStereotype());

		for (final XmlAttribute attribute : clazz.getKeyAttributes()) {
			final DslDefinition dtField = toDynamicDefinition(attribute);
			dtDefinitionBuilder.addChildDefinition(DomainGrammar.ID_FIELD, dtField);
		}
		for (final XmlAttribute tagAttribute : clazz.getFieldAttributes()) {
			final DslDefinition dtField = toDynamicDefinition(tagAttribute);
			dtDefinitionBuilder.addChildDefinition(DomainGrammar.DATA_FIELD, dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private static DslDefinition toDynamicDefinition(final XmlAttribute attribute) {
		final DslEntity dtFieldEntity = DomainGrammar.DT_DATA_FIELD_ENTITY;

		return DslDefinition.builder(attribute.getCode(), dtFieldEntity)
				.addPropertyValue(KspProperty.LABEL, attribute.getLabel())
				.addPropertyValue(KspProperty.PERSISTENT, attribute.isPersistent())
				.addPropertyValue(KspProperty.REQUIRED, attribute.isNotNull())
				.addDefinitionLink("domain", attribute.getDomain())
				.build();
	}

	private static DslDefinition toDynamicDefinition(final XmlAssociation association, final DslDefinitionRepository dynamicModelrepository) {
		final DslEntity associationEntity = DomainGrammar.ASSOCIATION_ENTITY;
		final DslEntity associationNNEntity = DomainGrammar.ASSOCIATION_NN_ENTITY;

		//On regarde si on est dans le cas d'une association simple ou multiple
		final boolean isAssociationNN = AssociationUtil.isMultiple(association.getMultiplicityA()) && AssociationUtil.isMultiple(association.getMultiplicityB());
		final DslEntity dynamicMetaDefinition;
		if (isAssociationNN) {
			dynamicMetaDefinition = associationNNEntity;
		} else {
			dynamicMetaDefinition = associationEntity;
		}

		final String associationDefinitionName = (isAssociationNN ? "Ann" : "A") + association.getCode();

		//On crée l'association
		final DslDefinitionBuilder associationDefinitionBuilder = DslDefinition.builder(associationDefinitionName, dynamicMetaDefinition)
				.withPackageName(association.getPackageName())
				.addPropertyValue(KspProperty.NAVIGABILITY_A, association.isNavigableA())
				.addPropertyValue(KspProperty.NAVIGABILITY_B, association.isNavigableB())
				//---
				.addPropertyValue(KspProperty.LABEL_A, association.getRoleLabelA())
				//On transforme en CODE ce qui est écrit en toutes lettres.
				.addPropertyValue(KspProperty.ROLE_A, XmlUtil.french2Java(association.getRoleLabelA()))
				.addPropertyValue(KspProperty.LABEL_B, association.getRoleLabelB())
				.addPropertyValue(KspProperty.ROLE_B, XmlUtil.french2Java(association.getRoleLabelB()))
				//---
				.addDefinitionLink("dtDefinitionA", getDtDefinitionName(association.getCodeA()))
				.addDefinitionLink("dtDefinitionB", getDtDefinitionName(association.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = association.getCode();
			associationDefinitionBuilder.addPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code= {}", association.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code= {}", association.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ;
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder
					.addPropertyValue(KspProperty.MULTIPLICITY_A, association.getMultiplicityA())
					.addPropertyValue(KspProperty.MULTIPLICITY_B, association.getMultiplicityB())
					.addPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(association, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private static String buildFkFieldName(final XmlAssociation association, final DslDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destiné à renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : où le nom de la FK est redéfini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DslDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionName(association.getCodeA()));
		final DslDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionName(association.getCodeB()));

		final DslDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(association.getMultiplicityA(), association.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DslDefinition> primaryKeys = foreignDefinition.getChildDefinitions(DomainGrammar.ID_FIELD);
		if (primaryKeys.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' aucune clé primaire sur la définition '" + foreignDefinition.getName() + "'");
		}
		if (primaryKeys.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' clé multiple non géré sur '" + foreignDefinition.getName() + "'");
		}
		if (dtDefinitionA.getName().equals(dtDefinitionB.getName()) && association.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' le nom de la clé est obligatoire (AutoJointure) '"
					+ foreignDefinition.getName()
					+ "'. Ce nom est déduit du code l'association, le code doit être composé ainsi : {Trigramme Table1}{Trigramme Table2}{Code association}."
					+ " Par exemple : DosUtiEmmeteur, DosUtiDestinataire, DosDosParent, ...");
		}

		//On récupère le nom de LA clé primaire .
		final String pkFieldName = primaryKeys.get(0).getName();

		//Par défaut le nom de la clé étrangère est constituée de la clé primaire référencée.
		String fkFieldName = pkFieldName;

		//Si l'association possède une nom défini par l'utilisateur, alors on l'ajoute à la FK avec un séparateur.
		if (association.getCodeName() != null) {
			//On construit le nom de la clé étrangère.
			fkFieldName = fkFieldName + StringUtil.first2UpperCase(association.getCodeName());
		}

		//On raccourci le nom de la clé étrangère.
		if (fkFieldName.length() > MAX_COLUMN_LENGTH) { // 30 est le max de dynamo (et de Oracle)
			fkFieldName = fkFieldName.substring(0, MAX_COLUMN_LENGTH);
			while (fkFieldName.endsWith("_")) {
				fkFieldName = fkFieldName.substring(0, fkFieldName.length() - 1);
			}

		}
		LOGGER.trace(KspProperty.FK_FIELD_NAME + "= {}", fkFieldName);
		//-----
		Assertion.checkNotNull(fkFieldName, "La clé primaire n''a pas pu être définie pour l'association '{0}'", association.getCode());
		return fkFieldName;
	}

	private static String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + code;
	}

}
