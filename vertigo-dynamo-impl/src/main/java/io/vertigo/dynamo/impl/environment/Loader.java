package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;

/**
 * Chargement de l'environnement.
 * @author pchretien
 * @version $Id: Loader.java,v 1.2 2013/07/29 11:50:25 pchretien Exp $
 */
public interface Loader {

	/**
	 * Parsing des définitions pour un fichier (oom, kpr ou ksp)
	 * défini par une url (sur système de fichier ou classpath)
	 * et selon la grammaire en argument.
	 * @param dynamicModelRepository DynamicModelRepository
	 * @throws LoaderException Exception lors du chargement
	 */
	void load(DynamicDefinitionRepository dynamicModelRepository) throws LoaderException;
}