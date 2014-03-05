package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.metamodel.DefinitionReference;

/**
 * Définition de la structure d'un champ.
 * 
 * Un champ représente une donnée nommée et typée.
 * Un champ permet de valider des données en s'appuyant sur le caractère requis et sur la validation intrinsèque au domaine.
 * 
 * Un champ possède
 *   - un nom
 *   - un domaine métier 
 *   - un label (obligatoirement renseigné)
 *   - un caractère requis ou non 
 *   - un caractère persistent ou non
 *   - un index 
 * *
 * Un champ référence 
 * 	 - la définition qu'il enrichit (DtDefinition)
 * 	 - la définition qu'il relie (équivalent d'une Foreign Key) 	   
 *   
 * @author  fconstantin, pchretien , npiedeloup
 * @version $Id: DtField.java,v 1.7 2013/10/22 12:25:18 pchretien Exp $
 */
public final class DtField {
	public static final String PREFIX = "FLD_";

	/**
	 * Type des champs.
	 */
	public enum FieldType {
		/**
		 * Champ désignant la clé (primaire).
		 */
		PRIMARY_KEY,

		/**
		 * Champ représentant une donnée interne (ou attribut) de l'objet.
		 */
		DATA,

		/**
		 * Champ représentant une donnée externe i.e. une référence vers un autre objet.
		 */
		FOREIGN_KEY,

		/**
		 * Champ calculé.
		 */
		COMPUTED
	}

	/** Nom du champ. */
	private final String name;

	/** Indicateur du type. */
	private final FieldType type;

	/** Si le champ obligatoire. */
	private final boolean notNull;

	/** Domain.*/
	private final DefinitionReference<Domain> domainRef;

	/** Libellé du champ. */
	private final MessageText label;

	/** Si le champ est persistant. */
	private final boolean persistent;

	/** Cas des FK ; référence à une FK. */
	private final String fkDtDefinitionName;

	/** ComputedExpression des champs Computed. */
	private final ComputedExpression computedExpression;

	private final String id;

	private final boolean dynamic;
	@JsonExclude
	private final DataAccessor dataAccessor;
	private final boolean sort;
	private final boolean display;

	/**
	* Constructeur.
	* @param id ID du champ
	* @param fieldName Nom du champ
	* @param type Type du champ
	* @param domain Domaine du champ
	* @param label Label
	* @param notNull Si champ not null
	* @param persistent Si champ persistent
	* @param fkDtDefinitionName Nom de la DtDefinition de la FK (noNull si type=FK)
	* @param computedExpression Expression du computed (noNull si type=Computed)
	* @param dynamic Gestion des champs dynamiques
	*/
	DtField(final String id, final String fieldName, final FieldType type, //
			final Domain domain, final MessageText label, final boolean notNull,//
			final boolean persistent, final String fkDtDefinitionName, //
			final ComputedExpression computedExpression, final boolean dynamic, //
			final boolean sort, final boolean display) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(domain);
		Assertion.checkNotNull(type);
		//----------------------------------------------------------------------
		this.id = id;
		this.domainRef = new DefinitionReference<>(domain);
		this.type = type;
		this.notNull = notNull;
		//----------------------------------------------------------------------
		Assertion.checkNotNull(fieldName);
		Assertion.checkArgument(fieldName.length() <= 30, "Le Nom du champ {0} doit être inférieur à 30", fieldName);
		Assertion.checkArgument(fieldName.toUpperCase().equals(fieldName), "Le nom du champ {0} doit être en majuscules", fieldName);
		name = fieldName;
		//----------------------------------------------------------------------
		Assertion.checkNotNull(label);
		this.label = label;
		//-----------------------------------------------------------------------
		Assertion.checkArgument(!(getType() == FieldType.COMPUTED && persistent), "Un champ calculé n'est jamais persistant");
		this.persistent = persistent;
		//-----------------------------------------------------------------------
		if (getType() == FieldType.FOREIGN_KEY) {
			Assertion.checkNotNull(fkDtDefinitionName, "Le champ {0} de type clé étrangère doit référencer une définition ", fieldName);
		} else {
			Assertion.checkState(fkDtDefinitionName == null, "Le champ {0} n''est pas une clé étrangère", fieldName);
		}
		this.fkDtDefinitionName = fkDtDefinitionName;
		//-----------------------------------------------------------------------
		if (getType() == DtField.FieldType.COMPUTED) {
			Assertion.checkNotNull(computedExpression, "Le champ {0} de type Computed doit référencer une expression ", fieldName);
		} else {
			Assertion.checkState(computedExpression == null, "Le champ {0} n''est pas Computed", fieldName);
		}
		this.computedExpression = computedExpression;
		//------------------------------------------------------------------------
		this.dynamic = dynamic;
		this.sort = sort;
		this.display = display;
		//------------------------------------------------------------------------
		dataAccessor = new DataAccessor(this);
	}

	/**
	 * @return Clé de la resource (i18n)
	 */
	public String getResourceKey() {
		return id;
	}

	/**
	 * Retourne le nom du champ.
	 * @return Nom du champ
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retourne si la propriété est non null
	 * (Obligatoire en entrée ou en sortie selon le paramètre inout).
	 *
	 * @return Si la propriété est non null
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * @return Type du champ.
	 */
	public FieldType getType() {
		return type;
	}

	/**
	 * Renvoie le domaine associé au Champ.
	 * Le domaine possède obligatoirement un formatter.
	 * @return Domaine (non null)
	 */
	public Domain getDomain() {
		return domainRef.get();
	}

	/**
	 * @return Libellé du champ.
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * Gestion de la persistance.
	 * @return Si le champ est persisté.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 *  @return DtDefinition de la ForeignKey (caractère obligatoire lié au type)
	 */
	//Todo changer le nom
	public DtDefinition getFkDtDefinition() {
		Assertion.checkNotNull(fkDtDefinitionName);
		//---------------------------------------------------------------------
		return Home.getDefinitionSpace().resolve(fkDtDefinitionName, DtDefinition.class);
	}

	/**
	 * Expression dans le cas d'un champ calculé.
	 *  @return ComputedExpression du champs calculé (caractère obligatoire lié au type)
	 */
	public ComputedExpression getComputedExpression() {
		Assertion.checkNotNull(computedExpression);
		//---------------------------------------------------------------------
		return computedExpression;
	}

	/**
	 * Permet d'accéder aux données.
	 * @return Accesseur des propriétés du dto. 
	 */
	public DataAccessor getDataAccessor() {
		return dataAccessor;
	}

	/**
	 * @return Si il s'agit d'un champ dynamique ou statique
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	public boolean isSort() {
		return sort;
	}

	public boolean isDisplay() {
		return display;
	}
}