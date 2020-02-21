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
package io.vertigo.dynamo.plugins.environment.registries.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.ComputedExpression;
import io.vertigo.dynamo.domain.metamodel.ConstraintDefinition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DomainBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.domain.metamodel.Properties;
import io.vertigo.dynamo.domain.metamodel.PropertiesBuilder;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DynamicRegistry;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.StringUtil;

/**
 * @author pchretien
 */
public final class DomainDynamicRegistry implements DynamicRegistry {
	private static final Logger LOGGER = LogManager.getLogger(DomainDynamicRegistry.class);
	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private final Map<String, DtDefinitionBuilder> dtDefinitionBuilders = new HashMap<>();

	@Override
	public DslGrammar getGrammar() {
		return new DomainGrammar();
	}

	/** {@inheritDoc} */
	@Override
	public DefinitionSupplier supplyDefinition(final DslDefinition dslDefinition) {
		return (definitionSpace) -> createDefinition(definitionSpace, dslDefinition);
	}

	private Definition createDefinition(final DefinitionSpace definitionSpace, final DslDefinition dslDefinition) {

		final DslEntity dslEntity = dslDefinition.getEntity();
		if (dslEntity.equals(DomainGrammar.CONSTRAINT_ENTITY)) {
			return createConstraint(dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.FORMATTER_ENTITY)) {
			return createFormatter(dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.DOMAIN_ENTITY)) {
			return createDomain(definitionSpace, dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.DT_DEFINITION_ENTITY)) {
			return createDtDefinition(definitionSpace, dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.FRAGMENT_ENTITY)) {
			return createFragmentDtDefinition(definitionSpace, dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.ASSOCIATION_ENTITY)) {
			return createAssociationSimpleDefinition(definitionSpace, dslDefinition);
		} else if (dslEntity.equals(DomainGrammar.ASSOCIATION_NN_ENTITY)) {
			return createAssociationNNDefinition(definitionSpace, dslDefinition);
		}
		throw new IllegalStateException("The type of definition" + dslDefinition + " is not managed by me");
	}

	/**
	 * Enregistrement de contrainte
	 *
	 * @param xconstraint Définition de contrainte
	 * @return DefinitionStandard Définition typée créée.
	 */
	private static ConstraintDefinition createConstraint(final DslDefinition xconstraint) {
		//On transforme la liste des paramètres (Liste de String) sous forme de tableau de String pour éviter
		//le sous typage de List et pour se rapprocher de la syntaxe connue de Main.
		final String name = xconstraint.getName();
		final String args = (String) xconstraint.getPropertyValue(KspProperty.ARGS);
		final String msg = (String) xconstraint.getPropertyValue(KspProperty.MSG);
		final String className = (String) xconstraint.getPropertyValue(KspProperty.CLASS_NAME);
		return new ConstraintDefinition(name, className, msg, args);
	}

	private static FormatterDefinition createFormatter(final DslDefinition xformatter) {
		final String name = xformatter.getName();
		final String args = (String) xformatter.getPropertyValue(KspProperty.ARGS);
		final String className = (String) xformatter.getPropertyValue(KspProperty.CLASS_NAME);
		return new FormatterDefinition(name, className, args);
	}

	private static Domain createDomain(final DefinitionSpace definitionSpace, final DslDefinition xdomain) {
		final String domainName = xdomain.getName();
		final List<String> constraintNames = xdomain.getDefinitionLinkNames("constraint");
		final String type = xdomain.getDefinitionLinkName("dataType");
		final Boolean multiple = (Boolean) xdomain.getPropertyValue(KspProperty.MULTIPLE);
		final Properties properties = extractProperties(xdomain);
		final DomainBuilder domainBuilder;
		if ("DtObject".equals(type)) {
			domainBuilder = Domain.builder(domainName, properties.getValue(DtProperty.TYPE), false);
		} else if ("DtList".equals(type)) {
			domainBuilder = Domain.builder(domainName, properties.getValue(DtProperty.TYPE), true);
		} else {
			final DataType dataType = DataType.valueOf(type);
			domainBuilder = Domain.builder(domainName, dataType, multiple == null ? false : multiple);
			//only primitive can have a formatter
			final boolean hasFormatter = !xdomain.getDefinitionLinkNames("formatter").isEmpty();
			if (hasFormatter) {
				final String formatterName = xdomain.getDefinitionLinkName("formatter");
				final FormatterDefinition formatterDefinition = definitionSpace.resolve(formatterName, FormatterDefinition.class);
				//---
				domainBuilder.withFormatter(formatterDefinition);
			}
		}
		return domainBuilder
				.withConstraints(createConstraints(definitionSpace, constraintNames))
				.withProperties(properties)
				.build();
	}

	private static DtDefinition createFragmentDtDefinition(final DefinitionSpace definitionSpace, final DslDefinition xdtDefinition) {
		final DtDefinition from = definitionSpace.resolve(xdtDefinition.getDefinitionLinkName("from"), DtDefinition.class);

		final String sortFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.SORT_FIELD);
		final String displayFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.DISPLAY_FIELD);
		final String handleFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.HANDLE_FIELD);

		//0. clones characteristics
		final DtDefinitionBuilder dtDefinitionBuilder = DtDefinition.builder(xdtDefinition.getName())
				.withFragment(from)
				.withPackageName(xdtDefinition.getPackageName())
				.withDataSpace(from.getDataSpace())
				.withPackageName(from.getPackageName())
				.withSortField(sortFieldName)
				.withDisplayField(displayFieldName)
				.withHandleField(handleFieldName);

		//1. adds aliases
		for (final DslDefinition alias : xdtDefinition.getChildDefinitions("alias")) {
			final DtField aliasDtField = from.getField(alias.getName());

			//--- REQUIRED
			final Boolean overiddenRequired = (Boolean) alias.getPropertyValue(KspProperty.REQUIRED);
			final boolean required = overiddenRequired != null ? overiddenRequired : aliasDtField.isRequired();

			//--- LABEL
			final String overiddenLabel = (String) alias.getPropertyValue(KspProperty.LABEL);
			final String label = overiddenLabel != null ? overiddenLabel : aliasDtField.getLabel().getDisplay();

			dtDefinitionBuilder.addDataField(
					aliasDtField.getName(),
					label,
					aliasDtField.getDomain(),
					required,
					aliasDtField.isPersistent());
		}

		//2. adds data and computed fields
		//Déclaration des champs du DT
		final List<DslDefinition> fields = xdtDefinition.getChildDefinitions(DomainGrammar.DATA_FIELD);
		populateDataDtField(definitionSpace, dtDefinitionBuilder, fields);

		//Déclaration des champs calculés
		final List<DslDefinition> computedFields = xdtDefinition.getChildDefinitions(DomainGrammar.COMPUTED_FIELD);
		populateComputedDtField(definitionSpace, dtDefinitionBuilder, computedFields);

		final DtDefinition dtDefinition = dtDefinitionBuilder
				.build();

		//0. adds ID field -->>> Should be first, but needs an already build DtDefinition
		if (from.getIdField().isPresent()) {
			final DtField idField = from.getIdField().get();
			dtDefinitionBuilder.addForeignKey(
					idField.getName(),
					idField.getLabel().getDisplay(),
					idField.getDomain(),
					true,
					from.getName());
		}

		return dtDefinition;
	}

	/**
	 * @param xdtDefinition Définition de DT
	 */
	private DtDefinition createDtDefinition(final DefinitionSpace definitionSpace, final DslDefinition xdtDefinition) {
		//Déclaration de la définition
		final String sortFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.SORT_FIELD);
		final String displayFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.DISPLAY_FIELD);
		final String handleFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.HANDLE_FIELD);
		//-----
		final String tmpStereotype = (String) xdtDefinition.getPropertyValue(KspProperty.STEREOTYPE);
		//Si Stereotype est non renseigné on suppose que la définition est DtStereotype.Data.
		final DtStereotype stereotype = tmpStereotype != null ? DtStereotype.valueOf(tmpStereotype) : null;
		//-----
		final String dataSpace = (String) xdtDefinition.getPropertyValue(KspProperty.DATA_SPACE);
		//-----
		final String fragmentOf = (String) xdtDefinition.getPropertyValue(KspProperty.FRAGMENT_OF);
		//-----
		//-----
		final String dtDefinitionName = xdtDefinition.getName();
		final DtDefinitionBuilder dtDefinitionBuilder = DtDefinition.builder(dtDefinitionName)
				.withPackageName(xdtDefinition.getPackageName())
				.withDataSpace(dataSpace)
				.withSortField(sortFieldName)
				.withDisplayField(displayFieldName)
				.withHandleField(handleFieldName);

		if (stereotype != null) {
			dtDefinitionBuilder.withStereoType(stereotype);
		}
		if (!StringUtil.isEmpty(fragmentOf)) {
			dtDefinitionBuilder.withFragment(definitionSpace.resolve(fragmentOf, DtDefinition.class));
		}

		//On enregistre les Builder pour pouvoir les mettre à jour sur les associations.
		Assertion.checkArgument(!dtDefinitionBuilders.containsKey(dtDefinitionName), "Definition '{0}' already registered", dtDefinitionName);
		dtDefinitionBuilders.put(dtDefinitionName, dtDefinitionBuilder);

		//Déclaration de la clé primaire
		final List<DslDefinition> keys = xdtDefinition.getChildDefinitions(DomainGrammar.ID_FIELD);
		populateIdDtField(definitionSpace, dtDefinitionBuilder, keys);

		//Déclaration des champs du DT
		final List<DslDefinition> fields = xdtDefinition.getChildDefinitions(DomainGrammar.DATA_FIELD);
		populateDataDtField(definitionSpace, dtDefinitionBuilder, fields);

		//Déclaration des champs calculés
		final List<DslDefinition> computedFields = xdtDefinition.getChildDefinitions(DomainGrammar.COMPUTED_FIELD);
		populateComputedDtField(definitionSpace, dtDefinitionBuilder, computedFields);

		return dtDefinitionBuilder.build();
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private static void populateIdDtField(
			final DefinitionSpace definitionSpace,
			final DtDefinitionBuilder dtDefinitionBuilder,
			final List<DslDefinition> fields) {

		for (final DslDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionLinkName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final String fieldName = field.getName();
			//-----
			dtDefinitionBuilder.addIdField(fieldName, label, domain);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private static void populateDataDtField(
			final DefinitionSpace definitionSpace,
			final DtDefinitionBuilder dtDefinitionBuilder,
			final List<DslDefinition> fields) {

		for (final DslDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionLinkName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final boolean notNull = (Boolean) field.getPropertyValue(KspProperty.REQUIRED);
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.REQUIRED), "Not null est une propriété obligatoire.");
			//--
			final Boolean tmpPersistent = (Boolean) field.getPropertyValue(KspProperty.PERSISTENT);
			//Si PERSISTENT est non renseigné on suppose que le champ est à priori persistant .
			final boolean persistent = tmpPersistent == null || tmpPersistent;
			//--
			final String fieldName = field.getName();
			//-----
			dtDefinitionBuilder.addDataField(fieldName, label, domain, notNull, persistent);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private static void populateComputedDtField(
			final DefinitionSpace definitionSpace,
			final DtDefinitionBuilder dtDefinitionBuilder,
			final List<DslDefinition> fields) {

		for (final DslDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionLinkName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//---
			final String expression = (String) field.getPropertyValue(KspProperty.EXPRESSION);
			final ComputedExpression computedExpression = new ComputedExpression(expression);
			//--
			final String fieldName = field.getName();

			dtDefinitionBuilder.addComputedField(fieldName, label, domain, computedExpression);
		}
	}

	private static AssociationNNDefinition createAssociationNNDefinition(final DefinitionSpace definitionSpace, final DslDefinition xassociation) {
		final String tableName = (String) xassociation.getPropertyValue(KspProperty.TABLE_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionLinkName("dtDefinitionA"), DtDefinition.class);
		final boolean navigabilityA = (Boolean) xassociation.getPropertyValue(KspProperty.NAVIGABILITY_A);
		final String roleA = (String) xassociation.getPropertyValue(KspProperty.ROLE_A);
		final String labelA = (String) xassociation.getPropertyValue(KspProperty.LABEL_A);

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionLinkName("dtDefinitionB"), DtDefinition.class);
		final boolean navigabilityB = (Boolean) xassociation.getPropertyValue(KspProperty.NAVIGABILITY_B);
		final String roleB = (String) xassociation.getPropertyValue(KspProperty.ROLE_B);
		final String labelB = (String) xassociation.getPropertyValue(KspProperty.LABEL_B);

		final AssociationNode associationNodeA = new AssociationNode(dtDefinitionA, navigabilityA, roleA, labelA, true, false);
		final AssociationNode associationNodeB = new AssociationNode(dtDefinitionB, navigabilityB, roleB, labelB, true, false);
		return new AssociationNNDefinition(xassociation.getName(), tableName, associationNodeA, associationNodeB);
	}

	// méthode permettant de créer une liste de contraintes à partir d'une liste de noms de contrainte
	private static List<ConstraintDefinition> createConstraints(final DefinitionSpace definitionSpace, final List<String> constraintNames) {
		return constraintNames.stream()
				.map(constraintName -> definitionSpace.resolve(constraintName, ConstraintDefinition.class))
				.collect(Collectors.toList());
	}

	private AssociationSimpleDefinition createAssociationSimpleDefinition(final DefinitionSpace definitionSpace, final DslDefinition xassociation) {

		final String associationType = (String) xassociation.getPropertyValue("type");

		final String multiplicityA;
		final String multiplicityB;
		final Boolean navigabilityA;
		final Boolean navigabilityB;
		if (associationType != null) {
			switch (associationType) {
				case "*>1":
					multiplicityA = "0..*";
					multiplicityB = "1..1";
					navigabilityA = false;
					navigabilityB = true;
					break;
				case "*>?":
					multiplicityA = "0..*";
					multiplicityB = "0..1";
					navigabilityA = false;
					navigabilityB = true;
					break;
				case "*>*":
					multiplicityA = "0..*";
					multiplicityB = "0..*";
					navigabilityA = false;
					navigabilityB = true;
					break;
				default:
					throw new VSystemException("type of asssociation not supported : '{0}', available types are : '{1}'" + associationType, " *>1 , *>? , *>* ");
			}
		} else {
			multiplicityA = (String) xassociation.getPropertyValue(KspProperty.MULTIPLICITY_A);
			navigabilityA = (Boolean) xassociation.getPropertyValue(KspProperty.NAVIGABILITY_A);
			multiplicityB = (String) xassociation.getPropertyValue(KspProperty.MULTIPLICITY_B);
			navigabilityB = (Boolean) xassociation.getPropertyValue(KspProperty.NAVIGABILITY_B);
			//---
			Assertion.checkNotNull(multiplicityA);
			Assertion.checkNotNull(navigabilityA);
			Assertion.checkNotNull(multiplicityB);
			Assertion.checkNotNull(navigabilityB);
		}
		// Vérification que l'on est bien dans le cas d'une association simple de type 1-n
		if (AssociationUtil.isMultiple(multiplicityB) && AssociationUtil.isMultiple(multiplicityA)) {
			//Relation n-n
			throw new IllegalArgumentException("Utiliser la déclaration AssociationNN");
		}
		if (!AssociationUtil.isMultiple(multiplicityB) && !AssociationUtil.isMultiple(multiplicityA)) {
			//Relation 1-1
			throw new IllegalArgumentException("Les associations 1-1 sont interdites");
		}

		final String fkFieldName = (String) xassociation.getPropertyValue(KspProperty.FK_FIELD_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionLinkName("dtDefinitionA"), DtDefinition.class);
		final String roleAOpt = (String) xassociation.getPropertyValue(KspProperty.ROLE_A);
		final String roleA = roleAOpt != null ? roleAOpt : dtDefinitionA.getLocalName();
		final String labelAOpt = (String) xassociation.getPropertyValue(KspProperty.LABEL_A);
		final String labelA = labelAOpt != null ? labelAOpt : dtDefinitionA.getLocalName();

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionLinkName("dtDefinitionB"), DtDefinition.class);
		final String roleBOpt = (String) xassociation.getPropertyValue(KspProperty.ROLE_B);
		final String roleB = roleBOpt != null ? roleBOpt : dtDefinitionB.getLocalName();
		final String labelB = (String) xassociation.getPropertyValue(KspProperty.LABEL_B);

		final AssociationNode associationNodeA = new AssociationNode(dtDefinitionA, navigabilityA, roleA, labelA, AssociationUtil.isMultiple(multiplicityA), AssociationUtil.isNotNull(multiplicityA));
		final AssociationNode associationNodeB = new AssociationNode(dtDefinitionB, navigabilityB, roleB, labelB, AssociationUtil.isMultiple(multiplicityB), AssociationUtil.isNotNull(multiplicityB));

		final AssociationSimpleDefinition associationSimpleDefinition = new AssociationSimpleDefinition(xassociation.getName(), fkFieldName, associationNodeA, associationNodeB);

		final AssociationNode primaryAssociationNode = associationSimpleDefinition.getPrimaryAssociationNode();
		final AssociationNode foreignAssociationNode = associationSimpleDefinition.getForeignAssociationNode();

		final DtDefinition fkDefinition = primaryAssociationNode.getDtDefinition();

		LOGGER.trace("{} : ajout d'une FK [{}] sur la table '{}'", xassociation.getName(), fkFieldName, foreignAssociationNode.getDtDefinition().getName());

		final String label = primaryAssociationNode.getLabel();
		dtDefinitionBuilders.get(foreignAssociationNode.getDtDefinition().getName())
				.addForeignKey(fkFieldName, label, fkDefinition.getIdField().get().getDomain(), primaryAssociationNode.isNotNull(), fkDefinition.getName());
		//On estime qu'une FK n'est ni une colonne de tri ni un champ d'affichage

		return associationSimpleDefinition;
	}

	/**
	 * Extrait le PropertyContainer<DtProperty> d'une DynamicDefinition.
	 * Associe les DtProperty et les KspProperty par leur nom.
	 * @param dslDefinition Definition
	 * @return Container des propriétés
	 */
	private static Properties extractProperties(final DslDefinition dslDefinition) {
		final PropertiesBuilder propertiesBuilder = Properties.builder();

		//On associe les propriétés Dt et Ksp par leur nom.
		for (final String entityPropertyName : dslDefinition.getPropertyNames()) {
			final Property property = DtProperty.valueOf(entityPropertyName);
			propertiesBuilder.addValue(property, dslDefinition.getPropertyValue(entityPropertyName));
		}
		return propertiesBuilder.build();
	}

	/** {@inheritDoc} */
	@Override
	public List<DslDefinition> onNewDefinition(final DslDefinition dslDefinition) {
		if (DomainGrammar.DT_DEFINITION_ENTITY.equals(dslDefinition.getEntity())
				|| DomainGrammar.FRAGMENT_ENTITY.equals(dslDefinition.getEntity())) {
			//Dans le cas des DT on ajoute les domaines
			return new ListBuilder<DslDefinition>()
					.add(createDTODomain(dslDefinition.getName(), dslDefinition.getPackageName()))
					.add(createDTCDomain(dslDefinition.getName(), dslDefinition.getPackageName()))
					.build();
		}
		return Collections.emptyList();
	}

	/*
	 * Construction des deux domaines relatif à un DT : DO_DT_XXX_DTO et DO_DT_XXX_DTC
	 */
	private static DslDefinition createDTODomain(final String definitionName, final String packageName) {
		//C'est le constructeur de DtDomainStandard qui vérifie la cohérence des données passées.
		//Notamment la validité de la liste des contraintes et la nullité du formatter

		final DslEntity metaDefinitionDomain = DomainGrammar.DOMAIN_ENTITY;

		return DslDefinition.builder(DOMAIN_PREFIX + definitionName + "Dto", metaDefinitionDomain)
				.withPackageName(packageName)
				.addDefinitionLink("dataType", "DtObject")
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.addPropertyValue(KspProperty.TYPE, definitionName)
				.build();
	}

	private static DslDefinition createDTCDomain(final String definitionName, final String packageName) {
		//C'est le constructeur de DtDomainStandard qui vérifie la cohérence des données passées.
		//Notamment la validité de la liste des contraintes et la nullité du formatter

		final DslEntity metaDefinitionDomain = DomainGrammar.DOMAIN_ENTITY;

		//On fait la même chose avec DTC

		return DslDefinition.builder(DOMAIN_PREFIX + definitionName + "Dtc", metaDefinitionDomain)
				.withPackageName(packageName)
				.addDefinitionLink("dataType", "DtList")
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.addPropertyValue(KspProperty.TYPE, definitionName)
				.build();
	}
}
