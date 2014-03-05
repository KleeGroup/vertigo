package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.kernel.lang.Option;

/**
 * Un objet est automatiquement géré par le broker.
 * Les méthodes de mises à jour lacent des erreurs utilisateurs et techniques.
 * Les méthodes d'accès aux données ne lancent que des erreurs techniques.
 *
 * @author  pchretien
 * @version $Id: Broker.java,v 1.4 2014/01/20 17:45:43 pchretien Exp $
 */
public interface Broker {
	/**
	 * @param <D> Type des objets de la collection
	 * @param dtDefinition Définition de DT
	 * @param criteria Criteria (null=aucun)
	 * @param maxRows Nombre max de lignes retournées (null=tous)
	 * @return DtList DTC
	 */
	<D extends DtObject> DtList<D> getList(final DtDefinition dtDefinition, final Criteria<D> criteria, Integer limit);

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * Nombre d'éléments présents dans le sysème de persistance.
	 * @param dtDefinition Définition de DT
	 * @return Nombre d'éléments. 
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Sauvegarde d'un objet.
	 * Création (insert) ou mise à jour (update) en fonction de son id.
	 * @param dto Objet à sauvegarder
	 */
	void save(DtObject dto);

	/**
	 * Sauvegarde d'un fichier.
	 *
	 * Si l'objet possède une URI  : mode modification
	 * Si l'objet ne possède pas d'URI : mode création
	 *
	 * @param fileInfo Fichier à sauvegarder (création ou modification)
	 */
	void save(FileInfo fileInfo);

	/**
	 * Sauvegarde d'un objet à l'identique, clé primaire incluse.
	 * Cette méthode est utile pour les transferts de données d'une base à une autre
	 * Création (insert) ou mise à jour (update) en fonction de l'existence dans la base cible.
	 * @param dto Objet à sauvegarder
	 */
	void merge(DtObject dto);

	/**
	 * Destruction d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	void delete(URI<? extends DtObject> uri);

	/**
	 * Suppression d'un fichier.
	 * @param uri URI du fichier à supprimmer
	 */
	void deleteFileInfo(URI<FileInfo> uri);

	//==========================================================================
	//=====================Accesseurs ==========================================
	//==========================================================================
	/**
	 * Récupération d'un objet persistant par son URI.
	 * Lorsque l'objet est en lecture seule il est possible d'accéder au objets partagés. (Liste de référence paér ex) 
	 * Si l'object n'existe pas l'option sera isEmpty.
	 *
	 * @param <D> Type de l'objet
	 * @param uri Uri de l'object
	 * @return Option de l'object récupéré NOT NUL
	 */
	<D extends DtObject> Option<D> getOption(final URI<D> uri);

	/**
	 * Récupération d'un objet persistant par son URI.
	 * Lorsque l'objet est en lecture seule il est possible d'accéder au objets partagés. (Liste de référence paér ex) 
	 * L'objet doit exister.
	 * Récupération d'un fichier par son URI.
	 *
	 * @param <D> Type de l'objet
	 * @param uri Uri de l'object
	 * @return Option de l'object récupéré NOT NUL
	 */
	<D extends DtObject> D get(final URI<D> uri);

	/**
	 * Récupération d'une liste identifiée par son URI.
	 *
	 * @param <D> Type des objets de la collection
	 * @param uri URI de la collection à récupérer
	 * @return DtList DTC
	 */
	<D extends DtObject> DtList<D> getList(final DtListURI uri);

	/**
	 * Récupération d'un fichier par son URI.
	 *
	 * @param uri FileURI du fichier à charger
	 * @return KFileInfo correspondant à l'URI fournie.
	 */
	FileInfo getFileInfo(final URI<FileInfo> uri);
}