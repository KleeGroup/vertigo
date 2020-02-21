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
package io.vertigo.vega.impl.servlet.filter;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Implémentation de ServletInputStream qui fonctionne avec le CompressionServletRequestWrapper.
 * Il est suppos� qu'il est utilisé avec un flux compressé (Content-Encoding = gzip).
 * @author Emeric Vernat
 */
class CompressionRequestStream extends FilterServletInputStream {
	private final HttpServletRequest request;

	/**
	 * Construit un servlet input stream associé avec la request sp�cifi�e.
	 * @param request javax.servlet.http.HttpServletRequest
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	CompressionRequestStream(final HttpServletRequest request) throws IOException {
		super(new GZIPInputStream(request.getInputStream()));
		this.request = request;
	}

	/**
	 * Ferme cet input stream.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void close() throws IOException {
		super.close();
		request.getInputStream().close();
	}

	/**
	 * Teste si cet input stream supporte les m�thodes <code>mark</code> et <code>reset</code>.
	 * @return boolean
	 * @see java.io.InputStream#mark(int)
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public boolean markSupported() {
		return false; // Assume that mark is NO good for a gzipInputStream
	}
}
