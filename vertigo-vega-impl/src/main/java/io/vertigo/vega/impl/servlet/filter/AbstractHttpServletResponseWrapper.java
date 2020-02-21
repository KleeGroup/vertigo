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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;

/**
 * Implémentation de HttpServletResponseWrapper pour éviter warnings à la compilation.
 * @author Emeric Vernat
 */
public abstract class AbstractHttpServletResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper implements AutoCloseable {
	private ServletOutputStream stream;
	private PrintWriter writer;
	private final HttpServletResponse response;
	private int status;

	/**
	 * Constructeur.
	 * @param response javax.servlet.HttpServletResponse
	 */
	protected AbstractHttpServletResponseWrapper(final HttpServletResponse response) {
		super(response);
		this.response = response;
	}

	protected final ServletOutputStream getStream() {
		return stream;
	}

	/** {@inheritDoc} */
	@Override
	public final void close() throws IOException {
		try {
			if (writer != null) {
				writer.close();
			} else if (stream != null) {
				stream.close();
			}
		} catch (final IOException e) {
			LogManager.getRootLogger().trace(e.getMessage(), e);
			//ignore IOException : streams are already send
		}
	}

	/**
	 * Surcharge de addHeader pour fixer le header même si la réponse est incluse (contrairement à tomcat).
	 * @param name String
	 * @param value String
	 */
	@Override
	public final void addHeader(final String name, final String value) {
		// nécessaire pour header gzip du filtre de compression
		if ("Content-Length".equals(name)) {
			setContentLength(Integer.parseInt(value));
			return;
		}
		response.addHeader(name, value);
	}

	/**
	 * Surcharge de setHeader pour fixer le header même si la réponse est incluse (contrairement à tomcat).
	 * @param name String
	 * @param value String
	 */
	@Override
	public final void setHeader(final String name, final String value) {
		if ("Content-Length".equals(name)) {
			setContentLength(Integer.parseInt(value));
			return;
		}
		response.setHeader(name, value);
	}

	/**
	 * Retourne le status définit par setStatus ou sendError.
	 * @return int
	 */
	@Override
	public final int getStatus() {
		return status;
	}

	/**
	 * Définit le status de la réponse http (SC_OK, SC_NOT_FOUND, SC_INTERNAL_SERVER_ERROR ...).
	 * @param status int
	 */
	@Override
	public final void setStatus(final int status) {
		super.setStatus(status);
		this.status = status;
	}

	/**
	 * Envoie une erreur comme réponse http (SC_OK, SC_NOT_FOUND, SC_INTERNAL_SERVER_ERROR ...).
	 * @param error int
	 * @throws IOException   Exception d'entrée/sortie
	 */
	@Override
	public final void sendError(final int error) throws IOException {
		super.sendError(error);
		status = error;
	}

	/**
	 * Crée et retourne un ServletOutputStream pour �crire le contenu dans la response associée.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	public abstract ServletOutputStream createOutputStream() throws IOException;

	/**
	 * Retourne le servlet output stream associé avec cette response.
	 * @return javax.servlet.ServletOutputStream
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public final ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");
		}

		if (stream == null) {
			stream = createOutputStream();
		}
		return stream;
	}

	/**
	 * Retourne le writer associé avec cette response.
	 * @return java.io.PrintWriter
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public final PrintWriter getWriter() throws IOException {
		if (writer == null) {
			if (stream != null) {
				throw new IllegalStateException("getOutputStream() has already been called for this response");
			}

			final ServletOutputStream outputStream = getOutputStream();
			final String charEnc = getResponse().getCharacterEncoding();
			// HttpServletResponse.getCharacterEncoding() shouldn't return null
			// according the spec, so feel free to remove that "if"
			final PrintWriter result;
			if (charEnc != null) {
				result = new PrintWriter(new OutputStreamWriter(outputStream, charEnc));
			} else {
				result = new PrintWriter(outputStream);
			}
			writer = result;
		}
		return writer;
	}

	/**
	 * Flushe le buffer et commite la response.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public final void flushBuffer() throws IOException {
		if (writer != null) { //NOPMD
			writer.flush();
		} else if (stream != null) {
			stream.flush();
		} else {
			super.flushBuffer();
		}
	}

	/**
	 * Définit la longueur du corps du contenu dans la réponse.
	 * Dans les servlets http, cette méthode définit le Content-Length dans les headers HTTP.
	 * @param length int
	 */
	@Override
	public void setContentLength(final int length) {
		response.setContentLength(length);
	}

	/**
	 * Définit le type du contenu dans la réponse.
	 * Dans les servlets http, cette méthode définit le Content-Type dans les headers HTTP.
	 * @param type String
	 */
	@Override
	public final void setContentType(final String type) {
		response.setContentType(type);
	}
}
