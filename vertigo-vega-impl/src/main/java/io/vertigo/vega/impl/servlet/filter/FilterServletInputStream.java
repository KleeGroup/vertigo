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
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * Input stream pour un filtre implémentant ServletInputStream à partir d'un InputStream.
 * @author Emeric Vernat
 */
class FilterServletInputStream extends ServletInputStream {
	private final InputStream in;
	private boolean finished;

	/**
	 * Constructeur.
	 * @param in InputStream
	 */
	public FilterServletInputStream(final InputStream in) {
		super();
		this.in = in;
	}

	/**
	 * Constructeur.
	 * @param in ServletInputStream
	 */
	public FilterServletInputStream(final ServletInputStream in) {
		super();
		this.in = in;
	}

	/**
	 * Lit l'octet suivant dans l'input stream. L'octet est retourné en tant que type int dans l'intervalle 0 à 255,
	 * ou -1 si la fin du flux a été atteinte.
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public int read() throws IOException {
		final int result = in.read();
		finished = result == -1;
		return result;
	}

	/**
	 * Lit un certain nombre d'octets depuis l'input stream et stocke ces octets dans le tableau sp�cifi� en paramètre
	 * Le nombre d'octets lus est renvoyé en retour, ou -1 si la fin du flux a été atteinte.
	 * @param bytes bytes[]
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] bytes) throws IOException {
		final int result = in.read(bytes);
		finished = result == -1;
		return result;
	}

	/**
	 * Lit jusqu'à <code>len</code> octets de données depuis l'input stream dans un tableau de bytes en commençant
	 * à la position <code>off</code>.
	 * Le nombre d'octets effectivement lus est renvoyé en retour, ou -1 si la fin du flux a été atteinte.
	 * @param bytes bytes[]
	 * @param off int
	 * @param len int
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read(final byte[] bytes, final int off, final int len) throws IOException {
		final int result = in.read(bytes, off, len);
		finished = result == -1;
		return result;
	}

	/**
	 * Passe et élimine <code>count</code> octets de cet input stream.
	 * Le nombre d'octets effectivement passés est renvoyé en retour.
	 * @param count int
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public long skip(final long count) throws IOException {
		return in.skip(count);
	}

	/**
	 * Retourne le nombre d'octets qui peuvent être lus (ou passés) depuis cet input stream sans bloquage particulier
	 * de l'appelant suivant (ce thread ou un autre) d'une méthode sur cet input stream.
	 * (L'implémentation de la class InputStream retourne toujours 0.)
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public int available() throws IOException {
		return in.available();
	}

	/**
	 * Ferme cet input stream.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

	/**
	 * Marque la position courante dans cet input stream. Un appel à la méthode reset repositionnera ce flux
	 * à la position marqu�e.
	 * L'agument readLimit indique à cet inputStream d'accepter autant d'octets à lire avant que la position
	 * marqu�e ne devienne invalide.
	 * (Cette méthode devrait être synchronized mais le synchronized du InputStream suffit).
	 * @param readlimit int
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		in.mark(readlimit);
	}

	/**
	 * Repositionne le flux à la position pr�c�demment marqu�e.
	 * (Cette méthode devrait être synchronized mais celui du InputStream suffit).
	 * @throws  IOException  Si le flux n'a pas été marqué ou si la marque est devenue invalide
	 * @see java.io.InputStream#mark(int)
	 * @see java.io.IOException
	 */
	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	/**
	 * Teste si cet input stream supporte les m�thodes <code>mark</code> et <code>reset</code>.
	 * @return boolean
	 * @see java.io.InputStream#mark(int)
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	/**
	 * Lit l'input stream, une ligne à la fois.
	 * Le nombre d'octets lus est renvoyé en retour, ou -1 si la fin du flux a été atteinte.
	 * @param buf byte[]
	 * @param off int
	 * @param len int
	 * @return int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 * @see javax.servlet.ServletInputStream#readLine(byte[], int, int)
	 */
	@Override
	public int readLine(final byte[] buf, final int off, final int len) throws IOException {
		if (in instanceof ServletInputStream) {
			final ServletInputStream sIn = (ServletInputStream) in;
			final int result = sIn.readLine(buf, off, len);
			finished = result == -1;
			return result;
		}
		final int result = super.readLine(buf, off, len);
		finished = result == -1;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		if (in instanceof ServletInputStream) {
			final ServletInputStream sIn = (ServletInputStream) in;
			return sIn.isFinished();
		}
		return finished;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isReady() {
		if (in instanceof ServletInputStream) {
			final ServletInputStream sIn = (ServletInputStream) in;
			return sIn.isReady();
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void setReadListener(final ReadListener readListener) {
		throw new UnsupportedOperationException("Can't setWriteListener on this ResponseStream");
	}
}
