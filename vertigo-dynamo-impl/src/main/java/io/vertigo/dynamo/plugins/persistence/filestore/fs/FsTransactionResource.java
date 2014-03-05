/**
 * 
 */
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

import io.vertigo.dynamo.transaction.KTransactionResource;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Classe de ressource, gérant la transaction des fichiers.
 * 
 * @author skerdudou
 * @version $Id: FsTransactionResource.java,v 1.2 2014/01/20 17:49:32 pchretien Exp $
 */
public final class FsTransactionResource implements KTransactionResource {

	private static final Logger LOG = Logger.getLogger(FsTransactionResource.class.getName());
	private final List<FileAction> fileActionList = new ArrayList<>();

	/** {@inheritDoc} */
	public void commit() throws Exception {
		Exception firstException = null;
		// on effectue les actions, on essaie d'en faire le maximum quelque soit les erreurs
		for (final FileAction fileAction : fileActionList) {
			try {
				fileAction.process();
			} catch (final Exception e) {
				LOG.fatal(e);
				if (firstException == null) {
					firstException = e;
				}
			}
		}

		// on retourne a l'utilisateur la première exception levée
		if (firstException != null) {
			throw firstException;
		}
	}

	/** {@inheritDoc} */
	public void rollback() {
		// RAF
	}

	/** {@inheritDoc} */
	public void release() {
		for (final FileAction fileAction : fileActionList) {
			fileAction.clean();
		}
		fileActionList.clear();
	}

	/**
	 * Sauvegarde du fichier au commit.
	 * 
	 * @param inputStream l'inputStream du fichier
	 * @param path le chemin de destination du fichier
	 */
	void saveFile(final InputStream inputStream, final String path) {
		fileActionList.add(new FileActionSave(inputStream, path));
	}

	/**
	 * Suppression du fichier au commit. Si on avait des insertions mémorisées sur ce fichier (cas uniquement pour TNR),
	 * on les retire et on ne met pas la suppression dans la liste des opérations à faire.
	 * 
	 * @param path le chemin de destination du fichier
	 */
	void deleteFile(final String path) {
		final File file = new File(path);
		final String absPath = file.getAbsolutePath();
		boolean found = false;
		for (int i = fileActionList.size() - 1; i >= 0; i--) {
			final FileAction act = fileActionList.get(i);
			if (act instanceof FileActionSave && absPath.equals(act.getAbsolutePath())) {
				found = true;
				fileActionList.remove(i);
			}
		}
		if (!found) {
			fileActionList.add(new FileActionDelete(path));
		}
	}
}